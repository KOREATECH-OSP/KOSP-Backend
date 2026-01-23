package io.swkoreatech.kosp.domain.upload.repository;

import java.util.List;
import io.swkoreatech.kosp.domain.upload.model.Attachment;
import org.springframework.data.repository.Repository;

public interface AttachmentRepository extends Repository<Attachment, Long> {
    
    Attachment save(Attachment attachment);
    
    List<Attachment> findAllById(Iterable<Long> ids);
}
