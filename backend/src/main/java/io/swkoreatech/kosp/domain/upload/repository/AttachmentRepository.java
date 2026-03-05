package io.swkoreatech.kosp.domain.upload.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import io.swkoreatech.kosp.domain.upload.model.Attachment;

/**
 * 첨부파일 리포지토리.
 * 첨부파일의 저장 및 조회 기능을 제공한다.
 */
public interface AttachmentRepository extends Repository<Attachment, Long> {

    /** 첨부파일을 저장한다. */
    Attachment save(Attachment attachment);

    /** ID 목록으로 첨부파일을 일괄 조회한다. */
    List<Attachment> findAllById(Iterable<Long> ids);
}
