package kr.ac.koreatech.sw.kosp.domain.upload.service;

import java.time.LocalDateTime;
import java.util.UUID;
import kr.ac.koreatech.sw.kosp.domain.upload.client.S3StorageClient;
import kr.ac.koreatech.sw.kosp.domain.upload.dto.response.FileResponse;
import kr.ac.koreatech.sw.kosp.domain.upload.event.FileUploadEvent;
import kr.ac.koreatech.sw.kosp.domain.upload.model.Attachment;
import kr.ac.koreatech.sw.kosp.domain.upload.repository.AttachmentRepository;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UploadService {

    private final AttachmentRepository attachmentRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final S3StorageClient s3Client;

    @Transactional
    public FileResponse uploadFile(MultipartFile file, User user) {
        // 1. Generate UUID-based filename
        String storedFileName = UUID.randomUUID().toString() + getExtension(file.getOriginalFilename());

        // 2. Publish async upload event
        eventPublisher.publishEvent(new FileUploadEvent(file, storedFileName));

        // 3. Save Attachment entity (article_id = null initially)
        Attachment attachment = Attachment.builder()
            .originalFileName(file.getOriginalFilename())
            .storedFileName(storedFileName)
            .fileSize(file.getSize())
            .contentType(file.getContentType())
            .url(s3Client.generateUrl(storedFileName))
            .uploadedBy(user)
            .uploadedAt(LocalDateTime.now())
            .build();

        attachmentRepository.save(attachment);

        return FileResponse.from(attachment);
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}
