package kr.ac.koreatech.sw.kosp.domain.community.board.service;

import kr.ac.koreatech.sw.kosp.domain.community.board.dto.response.BoardListResponse;
import kr.ac.koreatech.sw.kosp.domain.community.board.model.Board;
import kr.ac.koreatech.sw.kosp.domain.community.board.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardService {

    private final BoardRepository boardRepository;

    public BoardListResponse getBoards() {
        List<Board> boards = boardRepository.findAll();
        return BoardListResponse.from(boards);
    }

    public Board getBoard(Long id) {
        return boardRepository.getById(id);
    }
}
