package io.swkoreatech.kosp.domain.report.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.swkoreatech.kosp.domain.community.article.model.Article;
import io.swkoreatech.kosp.domain.community.article.repository.ArticleRepository;
import io.swkoreatech.kosp.domain.notification.event.NotificationEvent;
import io.swkoreatech.kosp.domain.notification.model.NotificationType;
import io.swkoreatech.kosp.domain.report.dto.request.ReportRequest;
import io.swkoreatech.kosp.domain.report.model.Report;
import io.swkoreatech.kosp.domain.report.model.enums.ReportTargetType;
import io.swkoreatech.kosp.domain.report.repository.ReportRepository;
import io.swkoreatech.kosp.domain.user.model.User;
import io.swkoreatech.kosp.global.exception.ExceptionMessage;
import io.swkoreatech.kosp.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final ReportRepository reportRepository;
    private final ArticleRepository articleRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void reportArticle(User reporter, Long articleId, ReportRequest request) {
        Article article = findArticle(articleId);
        validateReportRequest(reporter, article, articleId);

        saveReport(reporter, articleId, request);
        publishNotification(article, articleId);
    }

    private Article findArticle(Long articleId) {
        return articleRepository.findById(articleId)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.ARTICLE_NOT_FOUND));
    }

    private void validateReportRequest(User reporter, Article article, Long articleId) {
        validateNotSelfReport(reporter, article);
        validateNotDuplicateReport(reporter, articleId);
    }

    private void validateNotSelfReport(User reporter, Article article) {
        if (article.getAuthor().getId().equals(reporter.getId())) {
            throw new GlobalException(ExceptionMessage.SELF_REPORT_NOT_ALLOWED);
        }
    }

    private void validateNotDuplicateReport(User reporter, Long articleId) {
        if (reportRepository.existsByReporterAndTargetTypeAndTargetId(reporter, ReportTargetType.ARTICLE, articleId)) {
            throw new GlobalException(ExceptionMessage.ALREADY_REPORTED);
        }
    }

    private void saveReport(User reporter, Long articleId, ReportRequest request) {
        Report report = Report.builder()
            .reporter(reporter)
            .targetType(ReportTargetType.ARTICLE)
            .targetId(articleId)
            .reason(request.reason())
            .description(request.description())
            .build();

        reportRepository.save(report);
    }

    private void publishNotification(Article article, Long articleId) {
        Long authorId = article.getAuthor().getId();
        Map<String, Object> payload = Map.of("articleId", articleId);
        
        eventPublisher.publishEvent(
            NotificationEvent.of(authorId, NotificationType.ARTICLE_REPORTED, payload)
        );
    }
}
