package io.swkoreatech.kosp.common.title.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import io.swkoreatech.kosp.common.title.model.Title;
import io.swkoreatech.kosp.common.title.model.TitleCondition;

/**
 * {@link TitleCondition} 엔티티 데이터 접근 레포지토리.
 */
public interface TitleConditionRepository extends Repository<TitleCondition, Long> {

    TitleCondition save(TitleCondition titleCondition);

    /**
     * 특정 칭호에 속한 모든 조건을 조회한다.
     *
     * @param title 칭호 엔티티
     * @return 조건 목록
     */
    List<TitleCondition> findAllByTitle(Title title);
}
