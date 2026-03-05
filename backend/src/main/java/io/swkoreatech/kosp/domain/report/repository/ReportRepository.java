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

/**
 * 신고 리포지토리.
 * 신고의 저장 및 조회 기능을 제공한다.
 */
public interface ReportRepository extends Repository<Report, Long> {

    /** 신고를 저장한다. */
    Report save(Report report);

    /** ID로 신고를 조회한다. */
    Optional<Report> findById(Long id);

    /** 모든 신고를 조회한다. */
    List<Report> findAll();

    /** 상태별 신고 목록을 조회한다. */
    List<Report> findAllByStatus(ReportStatus status);

    /** 신고자, 대상 유형, 대상 ID로 신고 존재 여부를 확인한다. */
    boolean existsByReporterAndTargetTypeAndTargetId(User reporter, ReportTargetType targetType, Long targetId);

    /**
     * ID로 신고를 조회하고, 없으면 예외를 발생시킨다.
     *
     * @param id 신고 ID
     * @return 신고
     * @throws GlobalException 신고를 찾을 수 없는 경우
     */
    default Report getById(Long id) {
        return findById(id)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.NOT_FOUND));
    }
}
