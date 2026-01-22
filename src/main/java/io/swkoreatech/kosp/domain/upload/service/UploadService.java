package io.swkoreatech.kosp.domain.upload.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.StringJoiner;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.swkoreatech.kosp.domain.upload.client.S3StorageClient;
import io.swkoreatech.kosp.domain.upload.dto.request.UploadUrlRequest;
import io.swkoreatech.kosp.domain.upload.dto.response.UploadUrlResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UploadService {

    private final S3StorageClient s3Client;
    private final Clock clock;

    public UploadUrlResponse getPresignedUrl(UploadUrlRequest request) {
        String filePath = generateFilePath(request.fileName());
        return s3Client.getPresignedUrl(filePath, request.contentLength(), request.contentType());
    }

    private String generateFilePath(String originalFilename) {
        LocalDateTime now = LocalDateTime.now(clock);
        String extension = getExtension(originalFilename);
        String nameWithoutExtension = extractNameWithoutExtension(originalFilename);

        StringJoiner path = new StringJoiner("/");
        path.add("upload")
            .add(String.valueOf(now.getYear()))
            .add(String.valueOf(now.getMonthValue()))
            .add(String.valueOf(now.getDayOfMonth()))
            .add(UUID.randomUUID().toString())
            .add(nameWithoutExtension);

        return path + extension;
    }

    private String extractNameWithoutExtension(String filename) {
        if (filename == null) {
            return "file";
        }
        if (!filename.contains(".")) {
            return filename;
        }
        return filename.substring(0, filename.lastIndexOf("."));
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}
