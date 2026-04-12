package io.swkoreatech.kosp.domain.admin.season.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.common.season.model.Season;
import io.swkoreatech.kosp.common.season.model.SeasonProject;
import io.swkoreatech.kosp.common.season.model.SeasonProjectMember;
import io.swkoreatech.kosp.common.season.model.enums.ScoreEventType;
import io.swkoreatech.kosp.common.season.model.enums.SeasonProjectRole;
import io.swkoreatech.kosp.common.season.repository.SeasonProjectMemberRepository;
import io.swkoreatech.kosp.common.season.repository.SeasonProjectRepository;
import io.swkoreatech.kosp.common.season.repository.SeasonRepository;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.common.user.repository.UserRepository;
import io.swkoreatech.kosp.domain.admin.season.dto.request.AdminSeasonProjectCreateRequest;
import io.swkoreatech.kosp.domain.admin.season.dto.request.AdminSeasonProjectMemberAddRequest;
import io.swkoreatech.kosp.domain.admin.season.dto.response.AdminSeasonProjectListResponse;
import io.swkoreatech.kosp.domain.admin.season.dto.response.AdminSeasonProjectMemberListResponse;
import io.swkoreatech.kosp.domain.season.service.SeasonScoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 관리자 시즌 프로젝트 서비스.
 *
 * <p>프로젝트 오픈 → 참여자 등록 → 프로젝트 종료(일괄 점수 지급) 흐름을 처리한다.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminSeasonProjectService {

    private static final Map<Integer, Double> LEVEL_SCORE_MAP = Map.of(
        1, 2.0, 2, 4.0, 3, 6.0, 4, 8.0, 5, 10.0
    );
    private static final double MAX_PROJECT_SCORE = 30.0;

    private final SeasonRepository seasonRepository;
    private final SeasonProjectRepository seasonProjectRepository;
    private final SeasonProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final SeasonScoreService seasonScoreService;

    /**
     * 시즌 프로젝트를 오픈한다.
     *
     * @param seasonId 시즌 ID
     * @param request  프로젝트 생성 요청
     * @param adminId  관리자 ID
     */
    @Transactional
    public void openProject(Long seasonId, AdminSeasonProjectCreateRequest request, Long adminId) {
        Season season = seasonRepository.getById(seasonId);
        SeasonProject project = SeasonProject.builder()
            .season(season)
            .name(request.name())
            .projectLevel(request.projectLevel())
            .createdBy(adminId)
            .note(request.note())
            .build();
        seasonProjectRepository.save(project);
        log.info("[AdminSeason] Project opened: seasonId={}, name={}, level={}, adminId={}",
            seasonId, request.name(), request.projectLevel(), adminId);
    }

    /**
     * 프로젝트에 참여자를 등록한다.
     *
     * @param projectId 프로젝트 ID
     * @param request   참여자 추가 요청
     */
    @Transactional
    public void addMember(Long projectId, AdminSeasonProjectMemberAddRequest request) {
        SeasonProject project = seasonProjectRepository.getById(projectId);

        if (!project.isOpen()) {
            throw new GlobalException(ExceptionMessage.SEASON_PROJECT_ALREADY_CLOSED);
        }

        User user = userRepository.getById(request.userId());

        if (projectMemberRepository.existsByProjectAndUser(project, user)) {
            throw new GlobalException(ExceptionMessage.SEASON_PROJECT_MEMBER_ALREADY_EXISTS);
        }

        SeasonProjectMember member = SeasonProjectMember.builder()
            .project(project)
            .user(user)
            .roleType(request.roleType())
            .build();
        projectMemberRepository.save(member);
    }

    /**
     * 프로젝트를 종료하고 참여자에게 점수를 일괄 지급한다.
     *
     * @param projectId 프로젝트 ID
     * @param adminId   관리자 ID
     */
    @Transactional
    public void closeProject(Long projectId, Long adminId) {
        SeasonProject project = seasonProjectRepository.getById(projectId);

        if (!project.isOpen()) {
            throw new GlobalException(ExceptionMessage.SEASON_PROJECT_ALREADY_CLOSED);
        }

        List<SeasonProjectMember> members =
            projectMemberRepository.findAllByProjectAndScoreGrantedFalse(project);

        double baseScore = LEVEL_SCORE_MAP.getOrDefault(project.getProjectLevel(), 0.0);

        for (SeasonProjectMember member : members) {
            double roleBonus = member.getRoleType().getRoleBonus();
            double totalScore = baseScore + roleBonus;

            BigDecimal delta = BigDecimal.valueOf(totalScore);

            seasonScoreService.addScore(
                project.getSeason(),
                member.getUser(),
                ScoreEventType.PROJECT,
                delta,
                LocalDate.now(),
                project.getId(),
                "SEASON_PROJECT"
            );

            member.markGranted();
            projectMemberRepository.save(member);
        }

        project.close();
        seasonProjectRepository.save(project);

        log.info("[AdminSeason] Project closed: projectId={}, level={}, members={}, adminId={}",
            projectId, project.getProjectLevel(), members.size(), adminId);
    }

    /**
     * 시즌 프로젝트 목록을 조회한다.
     */
    public AdminSeasonProjectListResponse getProjects(Long seasonId, Pageable pageable) {
        Season season = seasonRepository.getById(seasonId);
        Page<SeasonProject> page = seasonProjectRepository.findAllBySeasonOrderByCreatedAtDesc(season, pageable);
        return AdminSeasonProjectListResponse.from(page);
    }

    /**
     * 프로젝트 참여자 목록을 조회한다.
     */
    public AdminSeasonProjectMemberListResponse getMembers(Long projectId) {
        SeasonProject project = seasonProjectRepository.getById(projectId);
        List<SeasonProjectMember> members = projectMemberRepository.findAllByProject(project);
        return AdminSeasonProjectMemberListResponse.from(project, members);
    }

    /**
     * 특정 레벨의 기본 점수를 반환한다.
     */
    public static double getBaseScore(int projectLevel) {
        return LEVEL_SCORE_MAP.getOrDefault(projectLevel, 0.0);
    }
}
