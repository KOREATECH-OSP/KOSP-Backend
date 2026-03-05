package io.swkoreatech.kosp.domain.community.recruit.service;

import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.community.recruit.dto.request.RecruitApplyDecisionRequest;
import io.swkoreatech.kosp.domain.community.recruit.dto.request.RecruitApplyRequest;
import io.swkoreatech.kosp.domain.community.recruit.dto.response.RecruitApplyListResponse;
import io.swkoreatech.kosp.domain.community.recruit.dto.response.RecruitApplyResponse;
import io.swkoreatech.kosp.domain.community.recruit.model.Recruit;
import io.swkoreatech.kosp.domain.community.recruit.model.RecruitApply.ApplyStatus;
import io.swkoreatech.kosp.domain.community.recruit.model.RecruitApply;
import io.swkoreatech.kosp.domain.community.recruit.model.RecruitStatus;
import io.swkoreatech.kosp.domain.community.recruit.repository.RecruitApplyRepository;
import io.swkoreatech.kosp.domain.community.recruit.repository.RecruitRepository;
import io.swkoreatech.kosp.domain.community.team.model.Team;
import io.swkoreatech.kosp.domain.community.team.model.TeamMember;
import io.swkoreatech.kosp.domain.community.team.model.TeamRole;
import io.swkoreatech.kosp.domain.community.team.repository.TeamMemberRepository;
import io.swkoreatech.kosp.global.dto.PageMeta;
import io.swkoreatech.kosp.global.util.RsqlUtils;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * 모집 지원 서비스.
 * 모집 지원, 지원자 조회, 수락/거절 기능을 담당한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecruitApplyService {

    private final RecruitRepository recruitRepository;
    private final RecruitApplyRepository recruitApplyRepository;
    private final TeamMemberRepository teamMemberRepository;

    /**
     * 모집 공고에 지원한다.
     *
     * @param recruitId 모집 공고 ID
     * @param user 지원자
     * @param request 지원 요청
     * @throws GlobalException 모집이 마감되었거나 이미 지원한 경우
     */
    @Transactional
    public void applyRecruit(Long recruitId, User user, RecruitApplyRequest request) {
        Recruit recruit = recruitRepository.findById(recruitId)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.RECRUITMENT_NOT_FOUND));

        if (recruit.getStatus() != RecruitStatus.OPEN) {
            throw new GlobalException(ExceptionMessage.RECRUIT_CLOSED);
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

    /**
     * 지원자 목록을 조회한다 (팀장 전용).
     *
     * @param recruitId 모집 공고 ID
     * @param user 요청 사용자 (팀장)
     * @param filter RSQL 필터
     * @param pageable 페이징 정보
     * @return 지원자 목록 응답
     * @throws GlobalException 팀장이 아닌 경우
     */
    public RecruitApplyListResponse getApplicants(Long recruitId, User user, String filter, Pageable pageable) {
        Recruit recruit = recruitRepository.findById(recruitId)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.RECRUITMENT_NOT_FOUND));

        validateLeader(recruit.getTeam(), user);

        Specification<RecruitApply> baseSpec = (root, query, cb) -> cb.equal(root.get("recruit"), recruit);
        Specification<RecruitApply> spec = RsqlUtils.toSpecification(filter, baseSpec);
        Page<RecruitApply> page = recruitApplyRepository.findAll(spec, pageable);

        return RecruitApplyListResponse.from(page);
    }

    /**
     * 지원 상세 정보를 조회한다 (팀장 전용).
     *
     * @param applicationId 지원 ID
     * @param user 요청 사용자 (팀장)
     * @return 지원 응답
     * @throws GlobalException 팀장이 아닌 경우
     */
    public RecruitApplyResponse getApplication(Long applicationId, User user) {
        RecruitApply apply = recruitApplyRepository.getById(applicationId);

        validateLeader(apply.getRecruit().getTeam(), user);

        return RecruitApplyResponse.from(apply);
    }

    /**
     * 지원을 수락 또는 거절한다 (팀장 전용).
     *
     * @param applicationId 지원 ID
     * @param user 요청 사용자 (팀장)
     * @param request 결정 요청
     * @throws GlobalException 팀장이 아니거나 이미 결정된 지원인 경우
     */
    @Transactional
    public void decideApplication(Long applicationId, User user, RecruitApplyDecisionRequest request) {
        RecruitApply apply = recruitApplyRepository.getById(applicationId);

        validateLeader(apply.getRecruit().getTeam(), user);

        if (apply.getStatus() != ApplyStatus.PENDING) {
            throw new GlobalException(ExceptionMessage.ALREADY_DECIDED);
        }

        apply.updateStatus(request.status());
        apply.updateDecisionReason(request.decisionReason());

        if (request.status() == ApplyStatus.ACCEPTED) {
            addMemberToTeam(apply.getRecruit().getTeam(), apply.getUser());
        }

        recruitApplyRepository.save(apply);
    }

    private void addMemberToTeam(Team team, User user) {
        if (teamMemberRepository.existsByTeamAndUser(team, user)) {
            return; // 이미 팀원인 경우 스킵
        }

        TeamMember member = TeamMember.builder()
            .team(team)
            .user(user)
            .role(TeamRole.MEMBER)
            .build();

        teamMemberRepository.save(member);
    }

    private void validateLeader(Team team, User user) {
        TeamMember member = teamMemberRepository.findByTeamAndUser(team, user)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.FORBIDDEN));

        if (member.getRole() != TeamRole.LEADER) {
            throw new GlobalException(ExceptionMessage.FORBIDDEN);
        }
    }
}

