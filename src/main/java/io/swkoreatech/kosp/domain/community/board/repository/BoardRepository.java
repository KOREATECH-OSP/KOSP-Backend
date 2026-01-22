package io.swkoreatech.kosp.domain.community.board.repository;

import java.util.List;
import java.util.Optional;
import io.swkoreatech.kosp.domain.community.board.model.Board;
import io.swkoreatech.kosp.global.exception.ExceptionMessage;
import io.swkoreatech.kosp.global.exception.GlobalException;
import org.springframework.data.repository.Repository;

public interface BoardRepository extends Repository<Board, Long> {

    Board save(Board board);

    List<Board> findAll();

    Optional<Board> findById(Long id);

    default Board getById(Long id) {
        return findById(id)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.BOARD_NOT_FOUND));
    }

    long count();
}
