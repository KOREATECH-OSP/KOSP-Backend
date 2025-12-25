package kr.ac.koreatech.sw.kosp.domain.community.recruit.repository;

import java.util.Optional;
import kr.ac.koreatech.sw.kosp.domain.community.board.model.Board;
import kr.ac.koreatech.sw.kosp.domain.community.recruit.model.Recruitment;
import kr.ac.koreatech.sw.kosp.global.exception.ExceptionMessage;
import kr.ac.koreatech.sw.kosp.global.exception.GlobalException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

public interface RecruitmentRepository extends Repository<Recruitment, Long> {

    Recruitment save(Recruitment recruitment);

    Optional<Recruitment> findById(Long id);

    void delete(Recruitment recruitment);

    Page<Recruitment> findByBoard(Board board, Pageable pageable);

    default Recruitment getById(Long id) {
        return findById(id)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.RECRUITMENT_NOT_FOUND));
    }
}
