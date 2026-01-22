package io.swkoreatech.kosp.domain.upload.client;

import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3StorageClient {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket:kosp-bucket}")
    private String bucketName;

    @Value("${aws.region:ap-northeast-2}")
    private String region;

    public void upload(MultipartFile file, String storedFileName) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(storedFileName)
                .contentType(file.getContentType())
                .contentLength(file.getSize())
                .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            log.info("File uploaded successfully to S3: {}", storedFileName);
        } catch (IOException e) {
            log.error("Failed to upload file to S3: {}", storedFileName, e);
            throw new RuntimeException("Failed to upload file to S3", e);
        }
    }

    public String generateUrl(String storedFileName) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, storedFileName);
    }
}
