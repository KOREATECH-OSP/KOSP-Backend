package kr.ac.koreatech.sw.kosp.domain.report.repository;

import java.util.List;
import java.util.Optional;
import kr.ac.koreatech.sw.kosp.domain.report.model.Report;
import kr.ac.koreatech.sw.kosp.domain.report.model.enums.ReportStatus;
import kr.ac.koreatech.sw.kosp.domain.report.model.enums.ReportTargetType;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import org.springframework.data.repository.Repository;

public interface ReportRepository extends Repository<Report, Long> {
    Report save(Report report);
    Optional<Report> findById(Long id);
    List<Report> findAll();
    List<Report> findAllByStatus(ReportStatus status);
    boolean existsByReporterAndTargetTypeAndTargetId(User reporter, ReportTargetType targetType, Long targetId);
}
