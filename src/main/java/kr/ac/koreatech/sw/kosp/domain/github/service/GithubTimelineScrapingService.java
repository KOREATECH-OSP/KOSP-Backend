package kr.ac.koreatech.sw.kosp.domain.github.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import kr.ac.koreatech.sw.kosp.domain.github.mongo.document.GithubTimelineData;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.document.GithubTimelineIssue;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.document.GithubTimelinePR;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.repository.GithubTimelineDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * GitHub Timeline 스크래핑 서비스
 * 
 * Reference: SKKU-OSP parse_user_update() (Line 98-239)
 * 
 * 기능:
 * 1. Timeline HTML 파싱
 * 2. Issue/PR 항목 추출
 * 3. Commit 개수 추출 (외부 값)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GithubTimelineScrapingService {
    
    private static final String GITHUB_URL = "https://github.com";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    
    private final GithubTimelineDataRepository timelineDataRepository;
    
    /**
     * Timeline 스크래핑
     * 
     * @param githubId GitHub 사용자 ID
     * @param fromDate 시작 날짜
     * @param toDate 종료 날짜
     * @return Timeline 데이터
     */
    public Mono<GithubTimelineData> scrapeTimeline(
        String githubId,
        LocalDate fromDate,
        LocalDate toDate
    ) {
        return Mono.fromCallable(() -> {
            log.info("Scraping timeline for {} from {} to {}", githubId, fromDate, toDate);
            
            // 이미 수집된 데이터 확인
            if (timelineDataRepository.existsByGithubIdAndFromDateAndToDate(githubId, fromDate, toDate)) {
                log.info("Timeline data already exists for {} ({} ~ {})", githubId, fromDate, toDate);
                return null;
            }
            
            // Timeline URL 생성
            String url = buildTimelineUrl(githubId, fromDate, toDate);
            
            // HTML 다운로드
            Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(30000)  // 30초 타임아웃
                .get();
            
            // 데이터 파싱
            List<GithubTimelineIssue> issues = new ArrayList<>();
            List<GithubTimelinePR> prs = new ArrayList<>();
            int commitsCount = 0;
            
            // Timeline 아이템 선택
            Elements timelineItems = doc.select(".TimelineItem-body");
            log.debug("Found {} timeline items", timelineItems.size());
            
            for (Element item : timelineItems) {
                try {
                    // Summary 추출
                    Element summary = item.selectFirst("summary");
                    if (summary == null) {
                        summary = item.selectFirst("h4");
                    }
                    
                    if (summary == null) {
                        log.trace("No summary found, skipping item");
                        continue;
                    }
                    
                    String summaryText = summary.text();
                    log.trace("Processing: {}", summaryText);
                    
                    // Issue 파싱
                    if (summaryText.contains("Created an issue") || 
                        summaryText.contains("Opened their first issue")) {
                        GithubTimelineIssue issue = parseIssue(item, githubId, fromDate);
                        if (issue != null) {
                            issues.add(issue);
                            log.debug("✅ Parsed issue: {}/{} #{}", 
                                issue.getOwnerId(), issue.getRepoName(), issue.getNumber());
                        }
                    }
                    
                    // PR 파싱
                    if (summaryText.contains("Created a pull request") || 
                        summaryText.contains("Opened their first pull request")) {
                        GithubTimelinePR pr = parsePR(item, githubId, fromDate);
                        if (pr != null) {
                            prs.add(pr);
                            log.debug("✅ Parsed PR: {}/{} #{}", 
                                pr.getOwnerId(), pr.getRepoName(), pr.getNumber());
                        }
                    }
                    
                    // Commit 개수 파싱
                    if (summaryText.contains("commit") || summaryText.contains("commits")) {
                        int count = parseCommitCount(item);
                        if (count > 0) {
                            commitsCount += count;
                            log.debug("✅ Parsed {} commits", count);
                        }
                    }
                    
                } catch (Exception e) {
                    log.warn("Failed to parse timeline item: {}", e.getMessage());
                }
            }
            
            // Timeline 데이터 생성
            GithubTimelineData data = GithubTimelineData.create(
                githubId,
                fromDate,
                toDate,
                issues,
                prs,
                commitsCount
            );
            
            // MongoDB 저장
            GithubTimelineData saved = timelineDataRepository.save(data);
            
            log.info("✅ Scraped timeline for {}: {} issues, {} PRs, {} commits",
                githubId, issues.size(), prs.size(), commitsCount);
            
            return saved;
            
        }).subscribeOn(Schedulers.boundedElastic())
          .onErrorResume(e -> {
              log.error("❌ Failed to scrape timeline for {}: {}", githubId, e.getMessage(), e);
              return Mono.error(e);
          });
    }
    
    /**
     * Issue 파싱
     * 
     * Reference: Line 132-147
     */
    private GithubTimelineIssue parseIssue(Element item, String githubId, LocalDate yearReference) {
        try {
            GithubTimelineIssue.GithubTimelineIssueBuilder builder = GithubTimelineIssue.builder()
                .githubId(githubId);
            
            // 저장소 정보 추출
            Element repoLink = item.selectFirst("h4 > a");
            if (repoLink == null) {
                log.warn("No repo link found for issue");
                return null;
            }
            
            String href = repoLink.attr("href");  // "/owner/repo"
            String[] parts = href.substring(1).split("/");
            if (parts.length < 2) {
                log.warn("Invalid repo href: {}", href);
                return null;
            }
            
            String ownerId = parts[0];
            String repoName = parts[1];
            builder.ownerId(ownerId)
                   .repoName(repoName)
                   .isOwnedRepo(ownerId.equals(githubId));
            
            // Issue 정보 추출
            Element issueLink = item.selectFirst("h3 > a");
            if (issueLink != null) {
                String title = issueLink.text();
                builder.title(title);
                
                String issueHref = issueLink.attr("href");  // "/owner/repo/issues/123"
                String numberStr = issueHref.substring(issueHref.lastIndexOf('/') + 1);
                try {
                    builder.number(Integer.parseInt(numberStr));
                } catch (NumberFormatException e) {
                    log.warn("Invalid issue number: {}", numberStr);
                }
            }
            
            // 날짜 추출
            Element timeElement = item.selectFirst("time");
            if (timeElement != null) {
                String dateText = timeElement.text();  // "Jan 15"
                LocalDate date = parseDate(dateText, yearReference);
                builder.date(date);
            }
            
            return builder.build();
            
        } catch (Exception e) {
            log.warn("Failed to parse issue: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * PR 파싱
     * 
     * Reference: Line 150-165
     */
    private GithubTimelinePR parsePR(Element item, String githubId, LocalDate yearReference) {
        try {
            GithubTimelinePR.GithubTimelinePRBuilder builder = GithubTimelinePR.builder()
                .githubId(githubId);
            
            // 저장소 정보 추출 (Issue와 동일)
            Element repoLink = item.selectFirst("h4 > a");
            if (repoLink == null) {
                log.warn("No repo link found for PR");
                return null;
            }
            
            String href = repoLink.attr("href");
            String[] parts = href.substring(1).split("/");
            if (parts.length < 2) {
                log.warn("Invalid repo href: {}", href);
                return null;
            }
            
            String ownerId = parts[0];
            String repoName = parts[1];
            builder.ownerId(ownerId)
                   .repoName(repoName)
                   .isOwnedRepo(ownerId.equals(githubId));
            
            // PR 정보 추출
            Element prLink = item.selectFirst("h3 > a");
            if (prLink != null) {
                String title = prLink.text();
                builder.title(title);
                
                String prHref = prLink.attr("href");  // "/owner/repo/pull/456"
                String numberStr = prHref.substring(prHref.lastIndexOf('/') + 1);
                try {
                    builder.number(Integer.parseInt(numberStr));
                } catch (NumberFormatException e) {
                    log.warn("Invalid PR number: {}", numberStr);
                }
            }
            
            // 날짜 추출
            Element timeElement = item.selectFirst("time");
            if (timeElement != null) {
                String dateText = timeElement.text();
                LocalDate date = parseDate(dateText, yearReference);
                builder.date(date);
            }
            
            return builder.build();
            
        } catch (Exception e) {
            log.warn("Failed to parse PR: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Commit 개수 파싱
     * 
     * Reference: Line 174
     */
    private int parseCommitCount(Element item) {
        try {
            Elements links = item.select("a");
            for (Element link : links) {
                String text = link.text();  // "5 commits" or "1 commit"
                if (text.contains("commit")) {
                    String[] parts = text.trim().split(" ");
                    if (parts.length > 0) {
                        try {
                            return Integer.parseInt(parts[0]);
                        } catch (NumberFormatException e) {
                            log.trace("Not a number: {}", parts[0]);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse commit count: {}", e.getMessage());
        }
        return 0;
    }
    
    /**
     * 날짜 파싱: "Jan 15" → LocalDate
     * 
     * Reference: Line 141-142
     */
    private LocalDate parseDate(String dateText, LocalDate yearReference) {
        try {
            // "Jan 15" 형식
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH);
            
            // 연도 없이 파싱
            String dateWithYear = dateText + " " + yearReference.getYear();
            DateTimeFormatter fullFormatter = DateTimeFormatter.ofPattern("MMM d yyyy", Locale.ENGLISH);
            
            return LocalDate.parse(dateWithYear, fullFormatter);
            
        } catch (DateTimeParseException e) {
            log.warn("Failed to parse date '{}', using reference date", dateText);
            return yearReference;
        }
    }
    
    /**
     * Timeline URL 생성
     */
    private String buildTimelineUrl(String githubId, LocalDate fromDate, LocalDate toDate) {
        return String.format("%s/%s/?tab=overview&from=%s&to=%s",
            GITHUB_URL,
            githubId,
            fromDate.format(DATE_FORMATTER),
            toDate.format(DATE_FORMATTER)
        );
    }
}
