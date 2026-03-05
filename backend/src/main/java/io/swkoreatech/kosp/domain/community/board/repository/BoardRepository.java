package io.swkoreatech.kosp.domain.community.board.repository;

import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.domain.community.board.model.Board;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

/**
 * 게시판 리포지토리.
 * 게시판의 저장 및 조회 기능을 제공한다.
 */
public interface BoardRepository extends Repository<Board, Long> {

    /**
     * 게시판을 저장한다.
     *
     * @param board 저장할 게시판
     * @return 저장된 게시판
     */
    Board save(Board board);

    /**
     * 모든 게시판을 조회한다.
     *
     * @return 게시판 목록
     */
    List<Board> findAll();

    /**
     * ID로 게시판을 조회한다.
     *
     * @param id 게시판 ID
     * @return 게시판 (존재하지 않으면 빈 Optional)
     */
    Optional<Board> findById(Long id);

    /**
     * ID로 게시판을 조회하고, 존재하지 않으면 예외를 발생시킨다.
     *
     * @param id 게시판 ID
     * @return 게시판
     * @throws GlobalException 게시판이 존재하지 않는 경우
     */
    default Board getById(Long id) {
        return findById(id)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.BOARD_NOT_FOUND));
    }

    /**
     * 게시판 총 개수를 반환한다.
     *
     * @return 게시판 개수
     */
    long count();
}
