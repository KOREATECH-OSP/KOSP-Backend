package kr.ac.koreatech.sw.kosp.domain.community.recruit.service;

import kr.ac.koreatech.sw.kosp.domain.community.board.model.Board;
import kr.ac.koreatech.sw.kosp.domain.community.board.repository.BoardRepository;
import kr.ac.koreatech.sw.kosp.domain.community.recruit.dto.response.RecruitListResponse;
import kr.ac.koreatech.sw.kosp.domain.community.recruit.dto.request.RecruitRequest;
import kr.ac.koreatech.sw.kosp.domain.community.recruit.dto.response.RecruitResponse;
import kr.ac.koreatech.sw.kosp.domain.community.recruit.model.Recruit;
import kr.ac.koreatech.sw.kosp.domain.community.recruit.model.RecruitStatus;
import kr.ac.koreatech.sw.kosp.domain.community.recruit.repository.RecruitRepository;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.domain.user.repository.UserRepository;
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
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    @Transactional
    public Long create(Long authorId, RecruitRequest req) {
        Board board = boardRepository.getById(req.boardId());
        User author = userRepository.getById(authorId);
        
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
            
        recruitRepository.save(recruit);
        return recruit.getId();
    }

    public RecruitResponse getOne(Long id) {
        Recruit recruit = recruitRepository.getById(id);
        recruit.increaseViews();
        return RecruitResponse.from(recruit);
    }

    public RecruitListResponse getList(Long boardId, Pageable pageable) {
        Board board = boardRepository.getById(boardId);
        Page<Recruit> page = recruitRepository.findByBoard(board, pageable);
        return RecruitListResponse.from(page);
    }

    @Transactional
    public void update(Long authorId, Long id, RecruitRequest req) {
        Recruit recruit = recruitRepository.getById(id);
        validateOwner(recruit, authorId);
        
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
    public void delete(Long authorId, Long id) {
        Recruit recruit = recruitRepository.getById(id);
        validateOwner(recruit, authorId);
        recruitRepository.delete(recruit);
    }

    private void validateOwner(Recruit recruit, Long authorId) {
        if (!recruit.getAuthor().getId().equals(authorId)) {
            throw new GlobalException(ExceptionMessage.FORBIDDEN);
        }
    }
}
