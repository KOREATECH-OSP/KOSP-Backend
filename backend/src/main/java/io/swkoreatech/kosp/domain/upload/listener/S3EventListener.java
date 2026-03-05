package io.swkoreatech.kosp.domain.upload.listener;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import io.swkoreatech.kosp.domain.upload.client.S3StorageClient;
import io.swkoreatech.kosp.domain.upload.event.FileUploadEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * S3 이벤트 리스너.
 * 파일 업로드 이벤트를 비동기로 처리하여 S3에 파일을 업로드한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class S3EventListener {

    private final S3StorageClient s3Client;

    /**
     * 파일 업로드 이벤트를 비동기로 처리하여 S3에 업로드한다.
     *
     * @param event 파일 업로드 이벤트
     */
    @EventListener
    @Async
    public void handleFileUpload(FileUploadEvent event) {
        log.info("Uploading file to S3: {}", event.getStoredFileName());
        s3Client.uploadFile(event.getStoredFileName(), event.getFile());
        log.info("File uploaded successfully: {}", event.getStoredFileName());
    }
}
