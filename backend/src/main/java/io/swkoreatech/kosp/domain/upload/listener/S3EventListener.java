package io.swkoreatech.kosp.domain.upload.listener;

import io.swkoreatech.kosp.domain.upload.client.S3StorageClient;
import io.swkoreatech.kosp.domain.upload.event.FileUploadEvent;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3EventListener {

    private final S3StorageClient s3Client;

    @EventListener
    @Async
    public void handleFileUpload(FileUploadEvent event) {
        log.info("Uploading file to S3: {}", event.getStoredFileName());
        s3Client.uploadFile(event.getStoredFileName(), event.getFile());
        log.info("File uploaded successfully: {}", event.getStoredFileName());
    }
}
