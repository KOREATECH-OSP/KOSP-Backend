package io.swkoreatech.kosp.domain.community.recruit.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.community.article.repository.ArticleBookmarkRepository;
import io.swkoreatech.kosp.domain.community.article.repository.ArticleLikeRepository;
import io.swkoreatech.kosp.domain.community.board.model.Board;
import io.swkoreatech.kosp.domain.community.recruit.dto.request.RecruitRequest;
import io.swkoreatech.kosp.domain.community.recruit.dto.response.RecruitListResponse;
import io.swkoreatech.kosp.domain.community.recruit.dto.response.RecruitResponse;
import io.swkoreatech.kosp.domain.community.recruit.model.Recruit;
import io.swkoreatech.kosp.domain.community.recruit.model.RecruitApply;
import io.swkoreatech.kosp.domain.community.recruit.model.RecruitApply.ApplyStatus;
import io.swkoreatech.kosp.domain.community.recruit.model.RecruitStatus;
import io.swkoreatech.kosp.domain.community.recruit.repository.RecruitApplyRepository;
import io.swkoreatech.kosp.domain.community.recruit.repository.RecruitRepository;
import io.swkoreatech.kosp.domain.community.team.model.Team;
import io.swkoreatech.kosp.domain.community.team.repository.TeamMemberRepository;
import io.swkoreatech.kosp.domain.community.team.repository.TeamRepository;
import io.swkoreatech.kosp.global.dto.PageMeta;
import io.swkoreatech.kosp.global.util.RsqlUtils;
import lombok.RequiredArgsConstructor;

/**
 * 모집 공고 서비스.
 * 모집 공고의 CRUD 및 상태 관리 기능을 담당한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecruitService {

    private final RecruitRepository recruitRepository;
    private final ArticleLikeRepository articleLikeRepository;
    private final ArticleBookmarkRepository articleBookmarkRepository;
    private final TeamRepository teamRepository;
    private final RecruitApplyRepository recruitApplyRepository;
    private final TeamMemberRepository teamMemberRepository;

    /**
     * 모집 공고를 작성한다.
     *
     * @param author 작성자
     * @param board 게시판
     * @param request 모집 공고 작성 요청
     * @return 생성된 모집 공고 ID
     */
    @Transactional
    public Long create(User author, Board board, RecruitRequest request) {
        Recruit recruit = Recruit.recruitBuilder()
            .author(author)
            .board(board)
            .title(request.title())
            .content(request.content())
            .tags(request.tags())
            .team(teamRepository.getById(request.teamId()))
            .status(RecruitStatus.OPEN)
            .startDate(request.startDate())
            .endDate(request.endDate())
            .build();

        return recruitRepository.save(recruit).getId();
    }

    /**
     * 모집 공고 상세 정보를 조회한다.
     *
     * @param id 모집 공고 ID
     * @param user 조회하는 사용자
     * @return 모집 공고 응답
     */
    public RecruitResponse getOne(Long id, User user) {
        Recruit recruit = recruitRepository.getById(id);
        recruit.increaseViews();

        boolean isLiked = isLiked(user, recruit);
        boolean isBookmarked = isBookmarked(user, recruit);
        boolean userCanApply = canApply(user, recruit);

        return RecruitResponse.from(recruit, isLiked, isBookmarked, userCanApply);
    }

    /**
     * 모집 공고 목록을 조회한다.
     *
     * @param board 게시판
     * @param pageable 페이징 정보
     * @param user 조회하는 사용자
     * @param rsql RSQL 필터 문자열
     * @return 모집 공고 목록 응답
     */
    public RecruitListResponse getList(Board board, Pageable pageable, User user, String rsql) {
        Pageable validatedPageable = validatePageSize(pageable);
        Specification<Recruit> spec = createSpecification(board, rsql);
        Page<Recruit> page = recruitRepository.findAll(spec, validatedPageable);
        List<RecruitResponse> recruits = mapToResponses(page, user);
        return RecruitListResponse.from(recruits, PageMeta.from(page));
    }

    private Pageable validatePageSize(Pageable pageable) {
        int size = Math.min(pageable.getPageSize(), 100);
        return PageRequest.of(pageable.getPageNumber(), size, pageable.getSort());
    }

    private Specification<Recruit> createSpecification(Board board, String rsql) {
        Specification<Recruit> boardSpec = (root, query, builder) ->
            builder.equal(root.get("board"), board);
        return RsqlUtils.toSpecification(rsql, boardSpec);
    }

    private List<RecruitResponse> mapToResponses(Page<Recruit> page, User user) {
        return page.getContent().stream()
            .map(recruit -> RecruitResponse.from(
                recruit,
                isLiked(user, recruit),
                isBookmarked(user, recruit),
                canApply(user, recruit)
            ))
            .toList();
    }

    /**
     * 모집 상태를 변경한다.
     *
     * @param author 요청 사용자
     * @param id 모집 공고 ID
     * @param status 변경할 상태
     * @throws GlobalException 작성자가 아닌 경우
     */
    @Transactional
    public void updateStatus(User author, Long id, RecruitStatus status) {
        Recruit recruit = recruitRepository.getById(id);
        validateOwner(recruit, author.getId());
        recruit.updateStatus(status);
    }

    /**
     * 모집 공고를 수정한다.
     *
     * @param author 요청 사용자
     * @param id 모집 공고 ID
     * @param request 수정 요청
     * @throws GlobalException 작성자가 아닌 경우
     */
    @Transactional
    public void update(User author, Long id, RecruitRequest request) {
        Recruit recruit = recruitRepository.getById(id);
        validateOwner(recruit, author.getId());

        recruit.updateRecruit(
            request.title(),
            request.content(),
            request.tags(),
            teamRepository.getById(request.teamId()),
            request.startDate(),
            request.endDate()
        );
    }

    /**
     * 모집 공고를 삭제한다.
     *
     * @param author 요청 사용자
     * @param id 모집 공고 ID
     * @throws GlobalException 작성자가 아닌 경우
     */
    @Transactional
    public void delete(User author, Long id) {
        Recruit recruit = recruitRepository.getById(id);
        validateOwner(recruit, author.getId());
        recruit.updateStatus(RecruitStatus.CLOSED);
        recruit.delete();
    }

    private void validateOwner(Recruit recruit, Long authorId) {
        if (!recruit.getAuthor().getId().equals(authorId)) {
            throw new GlobalException(ExceptionMessage.FORBIDDEN);
        }
    }

    private boolean isLiked(User user, Recruit recruit) {
        return user != null && articleLikeRepository.existsByUserAndArticle(user, recruit);
    }

    private boolean isBookmarked(User user, Recruit recruit) {
        return user != null && articleBookmarkRepository.existsByUserAndArticle(user, recruit);
    }

    boolean canApply(User user, Recruit recruit) {
        if (user == null) {
            return false;
        }
        Optional<RecruitApply> application = recruitApplyRepository.findByRecruitAndUser(recruit, user);
        if (application.isPresent() && isActiveApplication(application.get())) {
            return false;
        }
        Team team = recruit.getTeam();
        if (teamMemberRepository.existsByTeamAndUserAndIsDeletedFalse(team, user)) {
            return false;
        }
        return true;
    }

    private boolean isActiveApplication(RecruitApply apply) {
        ApplyStatus status = apply.getStatus();
        return status == ApplyStatus.PENDING || status == ApplyStatus.ACCEPTED;
    }
}
