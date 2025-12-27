package kr.ac.koreatech.sw.kosp.domain.community.recruit.service;

import java.util.List;
import kr.ac.koreatech.sw.kosp.domain.community.board.model.Board;
import kr.ac.koreatech.sw.kosp.domain.community.recruit.dto.response.RecruitListResponse;
import kr.ac.koreatech.sw.kosp.domain.community.recruit.dto.request.RecruitRequest;
import kr.ac.koreatech.sw.kosp.domain.community.recruit.dto.response.RecruitResponse;
import kr.ac.koreatech.sw.kosp.domain.community.recruit.model.Recruit;
import kr.ac.koreatech.sw.kosp.domain.community.recruit.model.RecruitStatus;
import kr.ac.koreatech.sw.kosp.domain.community.article.repository.ArticleBookmarkRepository;
import kr.ac.koreatech.sw.kosp.domain.community.article.repository.ArticleLikeRepository;
import kr.ac.koreatech.sw.kosp.domain.community.recruit.repository.RecruitRepository;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.global.dto.PageMeta;
import kr.ac.koreatech.sw.kosp.global.exception.ExceptionMessage;
import kr.ac.koreatech.sw.kosp.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecruitService {

    private final RecruitRepository recruitRepository;
    private final ArticleLikeRepository articleLikeRepository;
    private final ArticleBookmarkRepository articleBookmarkRepository;

    @Transactional
    public Long create(User author, Board board, RecruitRequest req) {
        Recruit recruit = Recruit.builder()
            .author(author)
            .board(board)
            .title(req.title())
            .content(req.content())
            .tags(req.tags())
            .teamId(req.teamId())
            .status(RecruitStatus.OPEN)
            .startDate(req.startDate())
            .endDate(req.endDate())
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
    public void update(User author, Long id, RecruitRequest req) {
        Recruit recruit = recruitRepository.getById(id);
        validateOwner(recruit, author.getId());
        
        recruit.updateRecruit(
            req.title(), 
            req.content(), 
            req.tags(),
            req.teamId(),
            req.startDate(),
            req.endDate()
        );
    }

    @Transactional
    public void delete(User author, Long id) {
        Recruit recruit = recruitRepository.getById(id);
        validateOwner(recruit, author.getId());
        recruitRepository.delete(recruit);
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
