package io.swkoreatech.kosp.domain.report.repository;

import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.report.model.Report;
import io.swkoreatech.kosp.domain.report.model.enums.ReportStatus;
import io.swkoreatech.kosp.domain.report.model.enums.ReportTargetType;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

public interface ReportRepository extends Repository<Report, Long> {

    Report save(Report report);
    Optional<Report> findById(Long id);
    List<Report> findAll();
    List<Report> findAllByStatus(ReportStatus status);
    boolean existsByReporterAndTargetTypeAndTargetId(User reporter, ReportTargetType targetType, Long targetId);

    default Report getById(Long id) {
        return findById(id)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.NOT_FOUND));
    }
}
