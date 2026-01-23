package io.swkoreatech.kosp.domain.community.recruit.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.swkoreatech.kosp.domain.community.recruit.dto.request.RecruitApplyDecisionRequest;
import io.swkoreatech.kosp.domain.community.recruit.dto.request.RecruitApplyRequest;
import io.swkoreatech.kosp.domain.community.recruit.dto.response.RecruitApplyListResponse;
import io.swkoreatech.kosp.domain.community.recruit.dto.response.RecruitApplyResponse;
import io.swkoreatech.kosp.domain.community.recruit.model.Recruit;
import io.swkoreatech.kosp.domain.community.recruit.model.RecruitApply;
import io.swkoreatech.kosp.domain.community.recruit.model.RecruitApply.ApplyStatus;
import io.swkoreatech.kosp.domain.community.recruit.model.RecruitStatus;
import io.swkoreatech.kosp.domain.community.recruit.repository.RecruitApplyRepository;
import io.swkoreatech.kosp.domain.community.recruit.repository.RecruitRepository;
import io.swkoreatech.kosp.domain.community.team.model.Team;
import io.swkoreatech.kosp.domain.community.team.model.TeamMember;
import io.swkoreatech.kosp.domain.community.team.model.TeamRole;
import io.swkoreatech.kosp.domain.community.team.repository.TeamMemberRepository;
import io.swkoreatech.kosp.domain.user.model.User;
import io.swkoreatech.kosp.global.dto.PageMeta;
import io.swkoreatech.kosp.global.exception.ExceptionMessage;
import io.swkoreatech.kosp.global.exception.GlobalException;
import io.swkoreatech.kosp.global.util.RsqlUtils;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecruitApplyService {

    private final RecruitRepository recruitRepository;
    private final RecruitApplyRepository recruitApplyRepository;
    private final TeamMemberRepository teamMemberRepository;

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

    public RecruitApplyListResponse getApplicants(Long recruitId, User user, String filter, Pageable pageable) {
        Recruit recruit = recruitRepository.findById(recruitId)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.RECRUITMENT_NOT_FOUND));

        validateLeader(recruit.getTeam(), user);

        Specification<RecruitApply> baseSpec = (root, query, cb) -> cb.equal(root.get("recruit"), recruit);
        Specification<RecruitApply> spec = RsqlUtils.toSpecification(filter, baseSpec);
        Page<RecruitApply> page = recruitApplyRepository.findAll(spec, pageable);

        return new RecruitApplyListResponse(
            page.getContent().stream()
                .map(RecruitApplyResponse::from)
                .toList(),
            PageMeta.from(page)
        );
    }

    public RecruitApplyResponse getApplication(Long applicationId, User user) {
        RecruitApply apply = recruitApplyRepository.findById(applicationId)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.APPLICATION_NOT_FOUND));

        validateLeader(apply.getRecruit().getTeam(), user);

        return RecruitApplyResponse.from(apply);
    }

    @Transactional
    public void decideApplication(Long applicationId, User user, RecruitApplyDecisionRequest request) {
        RecruitApply apply = recruitApplyRepository.findById(applicationId)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.APPLICATION_NOT_FOUND));

        validateLeader(apply.getRecruit().getTeam(), user);

        if (apply.getStatus() != ApplyStatus.PENDING) {
            throw new GlobalException(ExceptionMessage.ALREADY_DECIDED);
        }

        apply.updateStatus(request.status());

        if (request.status() == ApplyStatus.ACCEPTED) {
            addMemberToTeam(apply.getRecruit().getTeam(), apply.getUser());
        }
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

