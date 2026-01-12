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
            log.info("Scraping PR/Issue counts for {}/{}", repoOwner, repoName);
            
            Map<String, Integer> result = new HashMap<>();
            
            try {
                // PR 개수 스크래핑
                Document prDoc = Jsoup.connect(
                    String.format("%s/%s/%s/pulls", GITHUB_URL, repoOwner, repoName)
                )
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(30000)
                .get();
                
                Element prParent = prDoc.selectFirst("a[data-ga-click*='Pull Requests']");
                if (prParent != null) {
                    prParent = prParent.parent();
                    Elements prLinks = prParent.select("a");
                    
                    if (prLinks.size() >= 2) {
                        int openPRs = parseCount(prLinks.get(0).text());
                        int closedPRs = parseCount(prLinks.get(1).text());
                        
                        result.put("totalPRs", openPRs + closedPRs);
                        result.put("openPRs", openPRs);
                        result.put("closedPRs", closedPRs);
                        
                        log.debug("PR counts: {} open, {} closed", openPRs, closedPRs);
                    }
                }
                
            } catch (Exception e) {
                log.warn("Failed to scrape PR counts: {}", e.getMessage());
            }
            
            try {
                // Issue 개수 스크래핑
                Document issueDoc = Jsoup.connect(
                    String.format("%s/%s/%s/issues", GITHUB_URL, repoOwner, repoName)
                )
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(30000)
                .get();
                
                Element issueParent = issueDoc.selectFirst("a[data-ga-click*='Issues']");
                if (issueParent != null) {
                    issueParent = issueParent.parent();
                    Elements issueLinks = issueParent.select("a");
                    
                    if (issueLinks.size() >= 2) {
                        int openIssues = parseCount(issueLinks.get(0).text());
                        int closedIssues = parseCount(issueLinks.get(1).text());
                        
                        result.put("totalIssues", openIssues + closedIssues);
                        result.put("openIssues", openIssues);
                        result.put("closedIssues", closedIssues);
                        
                        log.debug("Issue counts: {} open, {} closed", openIssues, closedIssues);
                    }
                }
                
            } catch (Exception e) {
                log.warn("Failed to scrape Issue counts: {}", e.getMessage());
            }
            
            log.info("✅ Scraped counts for {}/{}: {} PRs, {} Issues",
                repoOwner, repoName, 
                result.getOrDefault("totalPRs", 0),
                result.getOrDefault("totalIssues", 0));
            
            return result;
            
        }).subscribeOn(Schedulers.boundedElastic());
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
            
            // Commits count
            try {
                Element commitsElement = doc.selectFirst("div.Box-header strong");
                if (commitsElement != null) {
                    String text = commitsElement.text().replace(",", "");
                    result.put("commitsCount", Integer.parseInt(text));
                    log.debug("Commits count: {}", text);
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
    private int parseCount(String text) {
        try {
            String[] parts = text.trim().replace(",", "").split(" ");
            return Integer.parseInt(parts[0]);
        } catch (Exception e) {
            log.warn("Failed to parse count from: {}", text);
            return 0;
        }
    }
}
