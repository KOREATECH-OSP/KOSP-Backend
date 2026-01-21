package kr.ac.koreatech.sw.kosp.domain.community.recruit.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.ac.koreatech.sw.kosp.domain.community.article.repository.ArticleBookmarkRepository;
import kr.ac.koreatech.sw.kosp.domain.community.article.repository.ArticleLikeRepository;
import kr.ac.koreatech.sw.kosp.domain.community.board.model.Board;
import kr.ac.koreatech.sw.kosp.domain.community.recruit.dto.request.RecruitRequest;
import kr.ac.koreatech.sw.kosp.domain.community.recruit.dto.response.RecruitListResponse;
import kr.ac.koreatech.sw.kosp.domain.community.recruit.dto.response.RecruitResponse;
import kr.ac.koreatech.sw.kosp.domain.community.recruit.model.Recruit;
import kr.ac.koreatech.sw.kosp.domain.community.recruit.model.RecruitStatus;
import kr.ac.koreatech.sw.kosp.domain.community.recruit.repository.RecruitRepository;
import kr.ac.koreatech.sw.kosp.domain.community.team.repository.TeamRepository;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.global.dto.PageMeta;
import kr.ac.koreatech.sw.kosp.global.exception.ExceptionMessage;
import kr.ac.koreatech.sw.kosp.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecruitService {

    private final RecruitRepository recruitRepository;
    private final ArticleLikeRepository articleLikeRepository;
    private final ArticleBookmarkRepository articleBookmarkRepository;
    private final TeamRepository teamRepository;

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
}
