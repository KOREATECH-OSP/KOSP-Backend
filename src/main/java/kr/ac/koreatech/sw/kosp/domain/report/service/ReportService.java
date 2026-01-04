package kr.ac.koreatech.sw.kosp.domain.report.service;

import kr.ac.koreatech.sw.kosp.domain.community.article.model.Article;
import kr.ac.koreatech.sw.kosp.domain.community.article.repository.ArticleRepository;
import kr.ac.koreatech.sw.kosp.domain.report.dto.request.ReportRequest;
import kr.ac.koreatech.sw.kosp.domain.report.model.Report;
import kr.ac.koreatech.sw.kosp.domain.report.model.enums.ReportTargetType;
import kr.ac.koreatech.sw.kosp.domain.report.repository.ReportRepository;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.global.exception.ExceptionMessage;
import kr.ac.koreatech.sw.kosp.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final ReportRepository reportRepository;
    private final ArticleRepository articleRepository;

    @Transactional
    public void reportArticle(User reporter, Long articleId, ReportRequest request) {
        Article article = articleRepository.findById(articleId)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.ARTICLE_NOT_FOUND));

        if (article.getAuthor().getId().equals(reporter.getId())) {
            throw new GlobalException(ExceptionMessage.SELF_REPORT_NOT_ALLOWED);
        }

        if (reportRepository.existsByReporterAndTargetTypeAndTargetId(reporter, ReportTargetType.ARTICLE, articleId)) {
            throw new GlobalException(ExceptionMessage.ALREADY_REPORTED);
        }

        Report report = Report.builder()
            .reporter(reporter)
            .targetType(ReportTargetType.ARTICLE)
            .targetId(articleId)
            .reason(request.reason())
            .description(request.description())
            .build();

        reportRepository.save(report);
    }
}
