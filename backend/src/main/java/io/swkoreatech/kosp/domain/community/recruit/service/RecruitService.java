package io.swkoreatech.kosp.domain.community.recruit.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.swkoreatech.kosp.domain.community.article.repository.ArticleBookmarkRepository;
import io.swkoreatech.kosp.domain.community.article.repository.ArticleLikeRepository;
import io.swkoreatech.kosp.domain.community.board.model.Board;
import io.swkoreatech.kosp.domain.community.recruit.dto.request.RecruitRequest;
import io.swkoreatech.kosp.domain.community.recruit.dto.response.RecruitListResponse;
import io.swkoreatech.kosp.domain.community.recruit.dto.response.RecruitResponse;
import io.swkoreatech.kosp.domain.community.recruit.model.Recruit;
import io.swkoreatech.kosp.domain.community.recruit.model.RecruitStatus;
import io.swkoreatech.kosp.domain.community.recruit.model.RecruitApply;
import io.swkoreatech.kosp.domain.community.recruit.model.RecruitApply.ApplyStatus;
import io.swkoreatech.kosp.domain.community.recruit.repository.RecruitApplyRepository;
import io.swkoreatech.kosp.domain.community.recruit.repository.RecruitRepository;
import io.swkoreatech.kosp.domain.community.team.model.Team;
import io.swkoreatech.kosp.domain.community.team.repository.TeamMemberRepository;
import io.swkoreatech.kosp.domain.community.team.repository.TeamRepository;
import io.swkoreatech.kosp.domain.user.model.User;
import io.swkoreatech.kosp.global.dto.PageMeta;
import io.swkoreatech.kosp.global.exception.ExceptionMessage;
import io.swkoreatech.kosp.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;

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

    @Transactional
    public Long create(User author, Board board, RecruitRequest request) {
        Recruit recruit = Recruit.builder()
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

    public RecruitResponse getOne(Long id, User user) {
        Recruit recruit = recruitRepository.getById(id);
        recruit.increaseViews();
        
        boolean isLiked = isLiked(user, recruit);
        boolean isBookmarked = isBookmarked(user, recruit);
        
        return RecruitResponse.from(recruit, isLiked, isBookmarked);
    }

    public RecruitListResponse getList(Board board, Pageable pageable, User user) {
        Page<Recruit> page = recruitRepository.findByBoard(board, pageable);
        List<RecruitResponse> recruits = page.getContent().stream()
            .map(recruit -> RecruitResponse.from(recruit, isLiked(user, recruit), isBookmarked(user, recruit)))
            .toList();
        return new RecruitListResponse(recruits, PageMeta.from(page));
    }

    @Transactional
    public void updateStatus(User author, Long id, RecruitStatus status) {
        Recruit recruit = recruitRepository.getById(id);
        validateOwner(recruit, author.getId());
        recruit.updateStatus(status);
    }

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

    @Transactional
    public void delete(User author, Long id) {
        Recruit recruit = recruitRepository.getById(id);
        validateOwner(recruit, author.getId());
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
        if (user == null) return false;
        
        Optional<RecruitApply> application = recruitApplyRepository.findByRecruitAndUser(recruit, user);
        if (application.isPresent() && isActiveApplication(application.get())) return false;
        
        Team team = recruit.getTeam();
        if (teamMemberRepository.existsByTeamAndUserAndIsDeletedFalse(team, user)) return false;
        
        return true;
    }

    private boolean isActiveApplication(RecruitApply apply) {
        ApplyStatus status = apply.getStatus();
        return status == ApplyStatus.PENDING || status == ApplyStatus.ACCEPTED;
    }
}
