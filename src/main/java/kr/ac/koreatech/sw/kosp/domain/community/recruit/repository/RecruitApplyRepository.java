package kr.ac.koreatech.sw.kosp.domain.community.recruit.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import kr.ac.koreatech.sw.kosp.domain.community.recruit.model.Recruit;
import kr.ac.koreatech.sw.kosp.domain.community.recruit.model.RecruitApply;
import kr.ac.koreatech.sw.kosp.domain.community.recruit.model.RecruitApply.ApplyStatus;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;

public interface RecruitApplyRepository extends JpaRepository<RecruitApply, Long> {
    Optional<RecruitApply> findByRecruitAndUser(Recruit recruit, User user);

    Page<RecruitApply> findByRecruit(Recruit recruit, Pageable pageable);

    List<RecruitApply> findByRecruitAndStatus(Recruit recruit, ApplyStatus status);

    Page<RecruitApply> findByUser(User user, Pageable pageable);

    Page<RecruitApply> findByUserAndStatus(User user, ApplyStatus status, Pageable pageable);

    Page<RecruitApply> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    Page<RecruitApply> findByUserAndStatusOrderByCreatedAtDesc(User user, ApplyStatus status, Pageable pageable);

    void deleteByRecruit(Recruit recruit);
}
