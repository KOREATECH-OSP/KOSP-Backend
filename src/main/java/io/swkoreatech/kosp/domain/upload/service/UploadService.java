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
import io.swkoreatech.kosp.domain.upload.dto.response.UploadResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UploadService {

    private final S3StorageClient s3Client;
    private final Clock clock;

    public UploadResponse upload(List<MultipartFile> files) {
        List<String> urls = files.stream()
            .map(this::uploadSingle)
            .toList();
        return UploadResponse.of(urls);
    }

    private String uploadSingle(MultipartFile file) {
        String filePath = generateFilePath(file.getOriginalFilename());
        return s3Client.uploadFile(filePath, file);
    }

    private String generateFilePath(String originalFilename) {
        LocalDateTime now = LocalDateTime.now(clock);
        String ext = getExtension(originalFilename);
        String name = originalFilename != null && originalFilename.contains(".")
            ? originalFilename.substring(0, originalFilename.lastIndexOf("."))
            : originalFilename;

        StringJoiner path = new StringJoiner("/");
        path.add("upload")
            .add(String.valueOf(now.getYear()))
            .add(String.valueOf(now.getMonthValue()))
            .add(String.valueOf(now.getDayOfMonth()))
            .add(UUID.randomUUID().toString())
            .add(name);

        return path + ext;
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}
