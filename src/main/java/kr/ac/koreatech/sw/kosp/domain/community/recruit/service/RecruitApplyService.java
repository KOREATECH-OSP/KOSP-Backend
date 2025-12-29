package kr.ac.koreatech.sw.kosp.domain.community.recruit.service;

import kr.ac.koreatech.sw.kosp.domain.community.recruit.dto.request.RecruitApplyRequest;
import kr.ac.koreatech.sw.kosp.domain.community.recruit.model.Recruit;
import kr.ac.koreatech.sw.kosp.domain.community.recruit.model.RecruitApply;
import kr.ac.koreatech.sw.kosp.domain.community.recruit.model.RecruitStatus;
import kr.ac.koreatech.sw.kosp.domain.community.recruit.repository.RecruitApplyRepository;
import kr.ac.koreatech.sw.kosp.domain.community.recruit.repository.RecruitRepository;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.global.exception.ExceptionMessage;
import kr.ac.koreatech.sw.kosp.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecruitApplyService {

    private final RecruitRepository recruitRepository;
    private final RecruitApplyRepository recruitApplyRepository;

    @Transactional
    public void applyRecruit(Long recruitId, User user, RecruitApplyRequest request) {
        Recruit recruit = recruitRepository.findById(recruitId)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.RECRUITMENT_NOT_FOUND));

        if (recruit.getStatus() != RecruitStatus.OPEN) {
             // Assuming we might need a specific exception for closed recruit, but BAD_REQUEST fits for now or use generic conflict
             throw new GlobalException(ExceptionMessage.BAD_REQUEST); 
        }

        if (recruitApplyRepository.findByRecruitAndUser(recruit, user).isPresent()) {
            throw new GlobalException(ExceptionMessage.CONFLICT);
        }

        RecruitApply recruitApply = RecruitApply.builder()
            .recruit(recruit)
            .user(user)
            .reason(request.reason())
            .portfolioUrl(request.portfolioUrl())
            .build();

        recruitApplyRepository.save(recruitApply);
    }
}
