package kr.ac.koreatech.sw.kosp.domain.community.board.repository;

import java.util.Optional;
import kr.ac.koreatech.sw.kosp.domain.community.board.model.Board;
import kr.ac.koreatech.sw.kosp.global.exception.ExceptionMessage;
import kr.ac.koreatech.sw.kosp.global.exception.GlobalException;
import org.springframework.data.repository.Repository;

public interface BoardRepository extends Repository<Board, Long> {

    Optional<Board> findById(Long id);

    boolean existsById(Long id);

    default Board getById(Long id) {
        return findById(id)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.BOARD_NOT_FOUND));
    }
}
