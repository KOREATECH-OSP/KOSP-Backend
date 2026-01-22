package io.swkoreatech.kosp.domain.upload.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.StringJoiner;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import io.swkoreatech.kosp.domain.upload.client.S3StorageClient;
import io.swkoreatech.kosp.domain.upload.dto.request.UploadUrlRequest;
import io.swkoreatech.kosp.domain.upload.dto.response.FileResponse;
import io.swkoreatech.kosp.domain.upload.dto.response.UploadFileResponse;
import io.swkoreatech.kosp.domain.upload.dto.response.UploadFilesResponse;
import io.swkoreatech.kosp.domain.upload.dto.response.UploadUrlResponse;
import io.swkoreatech.kosp.domain.upload.event.FileUploadEvent;
import io.swkoreatech.kosp.domain.upload.model.Attachment;
import io.swkoreatech.kosp.domain.upload.model.ImageUploadDomain;
import io.swkoreatech.kosp.domain.upload.repository.AttachmentRepository;
import io.swkoreatech.kosp.domain.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UploadService {

    private final AttachmentRepository attachmentRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final S3StorageClient s3Client;
    private final Clock clock;

    public UploadUrlResponse getPresignedUrl(ImageUploadDomain domain, UploadUrlRequest request) {
        String filePath = generateFilePath(domain.name(), request.fileName());
        return s3Client.getPresignedUrl(filePath);
    }

    public UploadFileResponse uploadFile(ImageUploadDomain domain, MultipartFile file) {
        String filePath = generateFilePath(domain.name(), file.getOriginalFilename());
        return s3Client.uploadFile(filePath, file);
    }

    public UploadFilesResponse uploadFiles(ImageUploadDomain domain, List<MultipartFile> files) {
        List<String> urls = files.stream()
            .map(file -> uploadFile(domain, file).fileUrl())
            .toList();
        return new UploadFilesResponse(urls);
    }

    @Transactional
    public FileResponse uploadFileWithAttachment(MultipartFile file, User user) {
        String storedFileName = UUID.randomUUID().toString() + getExtension(file.getOriginalFilename());

        eventPublisher.publishEvent(new FileUploadEvent(file, storedFileName));

        Attachment attachment = Attachment.builder()
            .originalFileName(file.getOriginalFilename())
            .storedFileName(storedFileName)
            .fileSize(file.getSize())
            .contentType(file.getContentType())
            .url(s3Client.generateUrl(storedFileName))
            .uploadedBy(user)
            .uploadedAt(LocalDateTime.now(clock))
            .build();

        attachmentRepository.save(attachment);

        return FileResponse.from(attachment);
    }

    private String generateFilePath(String domainName, String fileNameExt) {
        LocalDateTime now = LocalDateTime.now(clock);
        StringJoiner pathBuilder = new StringJoiner("/");
        String fileExt = getExtension(fileNameExt);
        String fileName = fileNameExt.contains(".") 
            ? fileNameExt.substring(0, fileNameExt.lastIndexOf(".")) 
            : fileNameExt;

        pathBuilder.add("upload")
            .add(domainName.toLowerCase())
            .add(String.valueOf(now.getYear()))
            .add(String.valueOf(now.getMonthValue()))
            .add(String.valueOf(now.getDayOfMonth()))
            .add(UUID.randomUUID().toString())
            .add(fileName);

        return pathBuilder + fileExt;
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}
