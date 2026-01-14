package kr.ac.koreatech.sw.kosp.domain.github.service;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * GitHub HTML 스크래핑 서비스
 * 
 * API로 얻기 어렵거나 비효율적인 정보를 HTML에서 추출
 * 
 * Features:
 * - Feature 5: PR/Issue 총 개수
 * - Feature 6: Repository HTML Page Data
 * - Feature 8: Repository Dependencies
 * - Feature 9: User Achievements/Highlights
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GithubHtmlScrapingService {
    
    private static final String GITHUB_URL = "https://github.com";
    
    /**
     * PR/Issue 총 개수 스크래핑
     * 
     * Reference: SKKU-OSP parse_repo_pr, parse_repo_issue (Line 412-429)
     * 
     * Feature 5: HTML PR/Issue 총 개수
     */
    public Mono<Map<String, Integer>> scrapePRIssueCounts(String repoOwner, String repoName) {
        return Mono.fromCallable(() -> {
            String repoPath = repoOwner + "/" + repoName;
            log.info("Scraping PR/Issue counts for {}", repoPath);
            
            Document prDoc;
            Document issueDoc;
            Map<String, Integer> result = new HashMap<>();
            
            /*
             * 1. Pull Requests Scraping
             * URL: /{owner}/{repo}/pulls
             * Selector: a[href*="is%3Aopen"][href*="is%3Apr"], a[href*="is%3Aclosed"][href*="is%3Apr"]
             */
            try {
                prDoc = Jsoup.connect(GITHUB_URL + "/" + repoPath + "/pulls")
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                        .get();
                
                // Open PRs
                Element openPrLink = prDoc.selectFirst("a[href*='is%3Aopen'][href*='is%3Apr']");
                int openPrs = openPrLink != null ? parseCount(openPrLink.text()) : 0;
                
                // Closed PRs
                Element closedPrLink = prDoc.selectFirst("a[href*='is%3Aclosed'][href*='is%3Apr']");
                int closedPrs = closedPrLink != null ? parseCount(closedPrLink.text()) : 0;
                
                result.put("openPRs", openPrs);
                result.put("closedPRs", closedPrs);
                result.put("totalPRs", openPrs + closedPrs);
                
            } catch (Exception e) {
                log.warn("Failed to scrape PR counts: {}", e.getMessage());
            }
            
            /*
             * 2. Issues Scraping
             * Feature: Issues (Open/Closed)
             * URL: /{owner}/{repo}/issues
             * Strategy:
             *   - Try scraping from the main issues page first (legacy UI selectors & new UI)
             *   - Fallback: Fetch specific search queries for Open/Closed if not found
             */
            try {
                issueDoc = Jsoup.connect(GITHUB_URL + "/" + repoPath + "/issues")
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                        .header("Accept-Language", "en-US,en;q=0.9")
                        .cookie("logged_in", "no")
                        .get();
                
                boolean foundOpen = false;
                boolean foundClosed = false;
                
                // --- Strategy 1: New UI (React/Turbo) Selectors ---
                // Open: a[href*="state%3Aopen"] span[data-variant="secondary"] or "issues-repo-tab-count"
                Element openIssueLink = issueDoc.selectFirst("a[href*='state%3Aopen'] span[data-variant='secondary']");
                if (openIssueLink != null) {
                    result.put("openIssues", parseCount(openIssueLink.text()));
                    foundOpen = true;
                } else {
                    // Try the repo tab counter
                    Element tabCounter = issueDoc.selectFirst("#issues-repo-tab-count");
                    if (tabCounter != null && tabCounter.hasAttr("title")) {
                        result.put("openIssues", parseCount(tabCounter.attr("title")));
                        foundOpen = true;
                    }
                }

                // Closed: a[href*="state%3Aclosed"] span[data-variant="secondary"]
                Element closedIssueLink = issueDoc.selectFirst("a[href*='state%3Aclosed'] span[data-variant='secondary']");
                if (closedIssueLink != null) {
                    result.put("closedIssues", parseCount(closedIssueLink.text()));
                    foundClosed = true;
                }

                // --- Strategy 2: Fallback (Old UI / Text based) ---
                if (!foundOpen) {
                    Elements links = issueDoc.select("a");
                    for (Element link : links) {
                        // Look for "123 Open" or similar
                        if (link.text().contains("Open") && link.attr("href").endsWith("/issues")) {
                            int count = parseCount(link.text());
                            if (count > 0) {
                                result.put("openIssues", count);
                                foundOpen = true;
                                break;
                            }
                        }
                    }
                }
                
                if (!foundClosed) {
                    Elements links = issueDoc.select("a");
                    for (Element link : links) {
                        // Look for "123 Closed" or similar
                        if (link.text().contains("Closed") && link.attr("href").contains("state%3Aclosed")) {
                            int count = parseCount(link.text());
                            if (count > 0) {
                                result.put("closedIssues", count);
                                foundClosed = true;
                                break;
                            }
                        }
                    }
                }

                // --- Strategy 3: Explicit Search Fallback (Robust against React/Hidden counts) ---
                if (!foundOpen) {
                    int openCount = fetchIssueCount(repoPath, "open");
                    if (openCount >= 0) {
                        result.put("openIssues", openCount);
                        foundOpen = true;
                        log.info("Recovered Open Issues via search query: {}", openCount);
                    }
                }
                
                if (!foundClosed) {
                    int closedCount = fetchIssueCount(repoPath, "closed");
                    if (closedCount >= 0) {
                        result.put("closedIssues", closedCount);
                        foundClosed = true;
                        log.info("Recovered Closed Issues via search query: {}", closedCount);
                    }
                }

                if (!foundOpen || !foundClosed) {
                    log.error("Scraping Incomplete even after fallback. Open Found: {}, Closed Found: {}", foundOpen, foundClosed);
                }
                
                // Calculate Total
                int openIssues = result.getOrDefault("openIssues", 0);
                int closedIssues = result.getOrDefault("closedIssues", 0);
                result.put("totalIssues", openIssues + closedIssues);

            } catch (Exception e) {
                log.warn("Failed to scrape Issue counts: {}", e.getMessage());
            }
            
            log.info("✅ Scraped counts for {}: {} PRs, {} Issues (Open: {}, Closed: {})",
                repoPath, 
                result.getOrDefault("totalPRs", 0),
                result.getOrDefault("totalIssues", 0),
                result.getOrDefault("openIssues", 0),
                result.getOrDefault("closedIssues", 0));
            
            return result;
            
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private int fetchIssueCount(String repoPath, String state) {
        try {
            // state: "open" or "closed"
            // Note: sort=created-desc helps to get a consistent fresh view
            String searchUrl = GITHUB_URL + "/" + repoPath + "/issues?q=is%3Aissue+state%3A" + state + "&sort=created-desc";
            Document doc = Jsoup.connect(searchUrl)
                    .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .cookie("logged_in", "no")
                    .timeout(10000)
                    .get();

            // Strategy 1: Parse from React Embedded Data (JSON)
            String html = doc.html();
            // Look for "issueCount":1234
            java.util.regex.Pattern jsonPattern = java.util.regex.Pattern.compile("\"issueCount\":(\\d+)");
            java.util.regex.Matcher jsonMatcher = jsonPattern.matcher(html);
            if (jsonMatcher.find()) {
                return Integer.parseInt(jsonMatcher.group(1));
            }

            // Strategy 2: Parse from Legacy Search Header (e.g. "2,146 results")
            Elements headings = doc.select("h3");
            for (Element h : headings) {
               if (h.text().contains("results") && h.text().matches(".*[0-9,].*")) {
                   return parseCount(h.text());
               }
            }
            
            // Strategy 3: Look for state link count
            String capitalizedState = state.substring(0, 1).toUpperCase() + state.substring(1);
            Elements links = doc.select("a");
            for (Element link : links) {
                if (link.text().contains(capitalizedState) && link.text().matches(".*[0-9,]+.*")) {
                    return parseCount(link.text());
                }
            }

            log.warn("Could not find issue count for state '{}' in {}", state, repoPath);
            return -1;

        } catch (Exception e) {
            log.error("Failed to fetch issue count fallback for {} state {}", repoPath, state, e);
            return -1;
        }
    }
    
    /**
     * Repository 페이지 데이터 스크래핑
     * 
     * Reference: SKKU-OSP parse_repo_page (Line 360-389)
     * 
     * Feature 6: Repository HTML Page Data
     */
    public Mono<Map<String, Object>> scrapeRepoPage(String owner, String repo) {
        return Mono.fromCallable(() -> {
            log.info("Scraping repo page for {}/{}", owner, repo);
            
            Document doc = Jsoup.connect(
                String.format("%s/%s/%s", GITHUB_URL, owner, repo)
            )
            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
            .timeout(30000)
            .get();
            
            Map<String, Object> result = new HashMap<>();
            
            // Commits count - Updated selector: a[href*="/commits/"] span
            try {
                Element commitsLink = doc.selectFirst("a[href*='/commits/']");
                if (commitsLink != null) {
                    Element span = commitsLink.selectFirst("span");
                    if (span != null) {
                        String text = span.text().replace(",", "").replace("Commits", "").trim();
                        result.put("commitsCount", Integer.parseInt(text));
                        log.debug("Commits count: {}", text);
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to parse commits count: {}", e.getMessage());
            }
            
            // Contributors count
            try {
                Element contributorLink = doc.selectFirst(
                    String.format("a[href='/%s/%s/graphs/contributors']", owner, repo)
                );
                if (contributorLink != null) {
                    Element counter = contributorLink.selectFirst("span.Counter");
                    if (counter != null) {
                        String text = counter.text().replace(",", "");
                        result.put("contributorsCount", Integer.parseInt(text));
                        log.debug("Contributors count: {}", text);
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to parse contributors count: {}", e.getMessage());
            }
            
            // README 존재
            try {
                boolean hasReadme = doc.selectFirst("div#readme") != null;
                result.put("hasReadme", hasReadme);
                log.debug("Has README: {}", hasReadme);
            } catch (Exception e) {
                log.warn("Failed to check README: {}", e.getMessage());
            }
            
            // Release 정보
            try {
                Element releaseLink = doc.selectFirst(
                    String.format("a[href='/%s/%s/releases']", owner, repo)
                );
                if (releaseLink != null) {
                    Element counter = releaseLink.selectFirst("span.Counter");
                    if (counter != null) {
                        String text = counter.text().replace(",", "");
                        result.put("releaseCount", Integer.parseInt(text));
                    }
                    
                    Element versionElement = releaseLink.selectFirst("a > div span");
                    if (versionElement != null) {
                        String version = versionElement.text();
                        if (version.length() > 45) {
                            version = version.substring(0, 45);
                        }
                        result.put("latestRelease", version);
                        log.debug("Latest release: {}", version);
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to parse release info: {}", e.getMessage());
            }
            
            log.info("✅ Scraped repo page for {}/{}", owner, repo);
            return result;
            
        }).subscribeOn(Schedulers.boundedElastic());
    }
    
    /**
     * Repository Dependencies 스크래핑
     * 
     * Reference: SKKU-OSP parse_repo_dependencies (Line 492-499)
     * 
     * Feature 8: Repository Dependencies
     */
    public Mono<Integer> scrapeDependencies(String owner, String repo) {
        return Mono.fromCallable(() -> {
            log.info("Scraping dependencies for {}/{}", owner, repo);
            
            try {
                Document doc = Jsoup.connect(
                    String.format("%s/%s/%s/network/dependencies", GITHUB_URL, owner, repo)
                )
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(30000)
                .get();
                
                int maxDeps = 0;
                Elements counters = doc.select(".Box .Counter");
                
                for (Element counter : counters) {
                    try {
                        int count = Integer.parseInt(counter.text().replace(",", ""));
                        maxDeps = Math.max(maxDeps, count);
                        log.debug("Found dependency count: {}", count);
                    } catch (NumberFormatException e) {
                        log.trace("Not a number: {}", counter.text());
                    }
                }
                
                log.info("✅ Scraped {} dependencies for {}/{}", maxDeps, owner, repo);
                return maxDeps;
                
            } catch (Exception e) {
                log.warn("Failed to scrape dependencies for {}/{}: {}", owner, repo, e.getMessage());
                return 0;
            }
            
        }).subscribeOn(Schedulers.boundedElastic());
    }
    
    /**
     * User Profile (Achievements/Highlights) 스크래핑
     * 
     * Reference: SKKU-OSP parse_user_page (Line 254-271)
     * 
     * Feature 9: User Achievements/Highlights
     */
    public Mono<Map<String, String>> scrapeUserProfile(String githubId) {
        return Mono.fromCallable(() -> {
            log.info("Scraping user profile for {}", githubId);
            
            Document doc = Jsoup.connect(String.format("%s/%s", GITHUB_URL, githubId))
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(30000)
                .get();
            
            Map<String, String> result = new HashMap<>();
            
            // h2.h4.mb-2 태그의 부모 요소들 찾기
            Elements headers = doc.select("h2.h4.mb-2");
            
            for (Element header : headers) {
                String headerText = header.text();
                Element parent = header.parent();
                
                if (parent == null) continue;
                
                // Achievements 파싱
                if ("Achievements".equals(headerText)) {
                    Elements images = parent.select("img");
                    if (!images.isEmpty()) {
                        String achievements = images.stream()
                            .map(img -> img.attr("alt"))
                            .filter(alt -> !alt.isEmpty())
                            .collect(Collectors.joining(", "));
                        
                        if (!achievements.isEmpty()) {
                            result.put("achievements", achievements);
                            log.debug("Achievements: {}", achievements);
                        }
                    }
                }
                
                // Highlights 파싱
                if ("Highlights".equals(headerText)) {
                    Elements items = parent.select("li");
                    if (!items.isEmpty()) {
                        String highlights = items.stream()
                            .map(Element::text)
                            .map(String::strip)
                            .filter(text -> !text.isEmpty())
                            .collect(Collectors.joining(", "));
                        
                        if (!highlights.isEmpty()) {
                            result.put("highlights", highlights);
                            log.debug("Highlights: {}", highlights);
                        }
                    }
                }
            }
            
            log.info("✅ Scraped user profile for {}: {} achievements, {} highlights",
                githubId,
                result.containsKey("achievements") ? "has" : "no",
                result.containsKey("highlights") ? "has" : "no");
            
            return result;
            
        }).subscribeOn(Schedulers.boundedElastic())
          .onErrorResume(e -> {
              log.warn("Failed to scrape user profile for {}: {}", githubId, e.getMessage());
              return Mono.just(Map.of());
          });
    }
    
    /**
     * 텍스트에서 숫자 추출: "123 Open" → 123
     */
    /**
     * 텍스트에서 숫자 추출: "123 Open" or "Open 123" → 123
     */
    private int parseCount(String text) {
        try {
            // 숫자와 쉼표를 제외한 모든 문자 제거 (단, 단순 제거는 위험할 수 있음)
            // Regex로 첫 번째 숫자 그룹 찾기
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d[\\d,]*)");
            java.util.regex.Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                String numberStr = matcher.group(1).replace(",", "");
                return Integer.parseInt(numberStr);
            }
            return 0;
        } catch (Exception e) {
            log.warn("Failed to parse count from: {}", text);
            return 0;
        }
    }
}
