package io.swkoreatech.kosp.common.title.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.common.title.model.Title;

/**
 * {@link Title} 엔티티 데이터 접근 레포지토리.
 */
public interface TitleRepository extends Repository<Title, Long> {

    Title save(Title title);

    Optional<Title> findById(Long id);

    /**
     * 활성화된 모든 칭호를 조회한다.
     *
     * @return 활성화된 칭호 목록 (display_order 오름차순)
     */
    List<Title> findAllByIsActiveTrueOrderByDisplayOrderAsc();

    /**
     * 모든 칭호를 조회한다 (활성/비활성 포함).
     */
    List<Title> findAll();

    default Title getById(Long id) {
        return findById(id)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.TITLE_NOT_FOUND));
    }
}
