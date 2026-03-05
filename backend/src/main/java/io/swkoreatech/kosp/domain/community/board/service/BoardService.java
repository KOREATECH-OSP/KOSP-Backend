package io.swkoreatech.kosp.domain.community.board.service;

import io.swkoreatech.kosp.domain.community.board.dto.response.BoardListResponse;
import io.swkoreatech.kosp.domain.community.board.model.Board;
import io.swkoreatech.kosp.domain.community.board.repository.BoardRepository;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * 게시판 서비스.
 * 게시판 목록 조회 및 단건 조회 기능을 담당한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardService {

    private final BoardRepository boardRepository;

    /**
     * 모든 게시판 목록을 조회한다.
     *
     * @return 게시판 목록 응답
     */
    public BoardListResponse getBoards() {
        List<Board> boards = boardRepository.findAll();
        return BoardListResponse.from(boards);
    }

    /**
     * 게시판을 조회한다.
     *
     * @param id 게시판 ID
     * @return 게시판 엔티티
     */
    public Board getBoard(Long id) {
        return boardRepository.getById(id);
    }
}
