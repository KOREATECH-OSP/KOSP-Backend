package kr.ac.koreatech.sw.kosp.domain.community.recruit.repository;

import java.util.Optional;
import kr.ac.koreatech.sw.kosp.domain.community.board.model.Board;
import kr.ac.koreatech.sw.kosp.domain.community.recruit.model.Recruit;
import kr.ac.koreatech.sw.kosp.global.exception.ExceptionMessage;
import kr.ac.koreatech.sw.kosp.global.exception.GlobalException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

public interface RecruitRepository extends Repository<Recruit, Long> {

    Recruit save(Recruit recruit);

    Optional<Recruit> findById(Long id);

    void delete(Recruit recruit);

    Page<Recruit> findByBoard(Board board, Pageable pageable);

    default Recruit getById(Long id) {
        return findById(id)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.RECRUITMENT_NOT_FOUND));
    }
}
