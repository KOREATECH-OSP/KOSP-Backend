package io.swkoreatech.kosp.domain.report.repository;

import java.util.List;
import java.util.Optional;
import io.swkoreatech.kosp.domain.report.model.Report;
import io.swkoreatech.kosp.domain.report.model.enums.ReportStatus;
import io.swkoreatech.kosp.domain.report.model.enums.ReportTargetType;
import io.swkoreatech.kosp.domain.user.model.User;
import org.springframework.data.repository.Repository;

public interface ReportRepository extends Repository<Report, Long> {
    Report save(Report report);
    Optional<Report> findById(Long id);
    List<Report> findAll();
    List<Report> findAllByStatus(ReportStatus status);
    boolean existsByReporterAndTargetTypeAndTargetId(User reporter, ReportTargetType targetType, Long targetId);
}
