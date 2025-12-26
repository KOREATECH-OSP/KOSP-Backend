package kr.ac.koreatech.sw.kosp.domain.community.recruit.service;

import kr.ac.koreatech.sw.kosp.domain.community.board.model.Board;
import kr.ac.koreatech.sw.kosp.domain.community.board.repository.BoardRepository;
import kr.ac.koreatech.sw.kosp.domain.community.recruit.dto.response.RecruitListResponse;
import kr.ac.koreatech.sw.kosp.domain.community.recruit.dto.request.RecruitRequest;
import kr.ac.koreatech.sw.kosp.domain.community.recruit.dto.response.RecruitResponse;
import kr.ac.koreatech.sw.kosp.domain.community.recruit.model.Recruitment;
import kr.ac.koreatech.sw.kosp.domain.community.recruit.model.RecruitmentStatus;
import kr.ac.koreatech.sw.kosp.domain.community.recruit.repository.RecruitmentRepository;
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

    private final RecruitmentRepository recruitRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    @Transactional
    public Long create(Long authorId, RecruitRequest req) {
        Board board = boardRepository.getById(req.boardId());
        User author = userRepository.getById(authorId);
        
        Recruitment recruitment = Recruitment.recruitBuilder()
            .author(author)
            .board(board)
            .title(req.getTitle())
            .content(req.getContent())
            .tags(req.getTags())
            .teamId(req.getTeamId())
            .status(RecruitmentStatus.OPEN)
            .startDate(req.getStartDate())
            .endDate(req.getEndDate())
            .build();
            
        recruitRepository.save(recruitment);
        return recruitment.getId();
    }

    public RecruitResponse getOne(Long id) {
        Recruitment recruit = recruitRepository.getById(id);
        recruit.increaseViews();
        return RecruitResponse.from(recruit);
    }

    public RecruitListResponse getList(Long boardId, Pageable pageable) {
        Board board = boardRepository.getById(boardId);
        Page<Recruitment> page = recruitRepository.findByBoard(board, pageable);
        return RecruitListResponse.from(page);
    }

    @Transactional
        Recruitment recruit = recruitRepository.getById(id);
        validateOwner(recruit, authorId);
        
        recruit.updateRecruit(
            req.getContent(), 
            req.getTeamId(),
            req.getEndDate()
            req.title(), 
            req.content(), 
            req.tags(),
            req.teamId(),
            req.startDate(),
            req.endDate()
        );
    }

    @Transactional
        Recruitment recruit = recruitRepository.getById(id);
        validateOwner(recruit, authorId);
        recruitRepository.delete(recruit);
    }

        if (!recruit.getAuthor().getId().equals(authorId)) {
            throw new GlobalException(ExceptionMessage.FORBIDDEN);
        }
    }
}
