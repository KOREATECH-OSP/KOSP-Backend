package kr.ac.koreatech.sw.kosp.domain.community.recruit.repository;

import kr.ac.koreatech.sw.kosp.domain.community.recruit.model.Recruit;
import kr.ac.koreatech.sw.kosp.domain.community.recruit.model.RecruitApply;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RecruitApplyRepository extends JpaRepository<RecruitApply, Long> {
    Optional<RecruitApply> findByRecruitAndUser(Recruit recruit, User user);
}
