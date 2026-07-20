package io.swkoreatech.kosp.domain.resume.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import io.swkoreatech.kosp.domain.resume.model.ResumeMaterialProject;

/**
 * 이력서 ↔ 학습자료 자동 프로젝트 링크(예외) 저장소.
 */
public interface ResumeMaterialProjectRepository extends JpaRepository<ResumeMaterialProject, Long> {

    /**
     * 특정 이력서의 예외 링크를 모두 조회한다 (투영 시 tombstone/override 적용용).
     */
    List<ResumeMaterialProject> findAllByResumeId(Long resumeId);

    /**
     * 이력서-자료 단건 링크를 조회한다.
     */
    Optional<ResumeMaterialProject> findByResumeIdAndMaterialItemId(Long resumeId, Long materialItemId);
}
