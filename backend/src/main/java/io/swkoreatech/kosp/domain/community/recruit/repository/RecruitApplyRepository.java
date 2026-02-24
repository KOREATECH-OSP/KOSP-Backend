package io.swkoreatech.kosp.domain.community.recruit.repository;

import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.community.recruit.model.Recruit;
import io.swkoreatech.kosp.domain.community.recruit.model.RecruitApply.ApplyStatus;
import io.swkoreatech.kosp.domain.community.recruit.model.RecruitApply;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RecruitApplyRepository
        extends JpaRepository<RecruitApply, Long>, JpaSpecificationExecutor<RecruitApply> {

    Optional<RecruitApply> findByRecruitAndUser(Recruit recruit, User user);

    Page<RecruitApply> findByRecruit(Recruit recruit, Pageable pageable);

    List<RecruitApply> findByRecruitAndStatus(Recruit recruit, ApplyStatus status);

    Page<RecruitApply> findByUser(User user, Pageable pageable);

    void deleteByRecruit(Recruit recruit);

    default RecruitApply getById(Long id) {
        return findById(id)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.APPLICATION_NOT_FOUND));
    }
}
