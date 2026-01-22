package io.swkoreatech.kosp.domain.upload.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import io.swkoreatech.kosp.global.exception.ExceptionMessage;
import io.swkoreatech.kosp.global.exception.GlobalException;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Slf4j
@Component
public class S3StorageClient {

    private final S3Client s3Client;
    private final String bucketName;
    private final String domainUrlPrefix;

    public S3StorageClient(
        S3Client s3Client,
        @Value("${aws.s3.bucket:kosp-bucket}") String bucketName,
        @Value("${aws.region:ap-northeast-2}") String region
    ) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
        this.domainUrlPrefix = String.format("https://%s.s3.%s.amazonaws.com/", bucketName, region);
    }

    public String uploadFile(String uploadFilePath, MultipartFile file) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(uploadFilePath)
                .contentType(file.getContentType())
                .contentLength(file.getSize())
                .acl(ObjectCannedACL.PUBLIC_READ)
                .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            log.info("File uploaded successfully to S3: {}", uploadFilePath);

            return domainUrlPrefix + uploadFilePath;
        } catch (IOException e) {
            log.error("Failed to upload file to S3: {}", uploadFilePath, e);
            throw new GlobalException(ExceptionMessage.FILE_UPLOAD_FAILED);
        }
    }

    public String uploadFile(String uploadFilePath, byte[] fileData) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(uploadFilePath)
            .contentLength((long) fileData.length)
            .acl(ObjectCannedACL.PUBLIC_READ)
            .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(new ByteArrayInputStream(fileData), fileData.length));
        log.info("File uploaded successfully to S3: {}", uploadFilePath);

        return domainUrlPrefix + uploadFilePath;
    }

    public void deleteFile(String s3Key) {
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .build();
            s3Client.deleteObject(deleteRequest);
            log.info("File deleted successfully from S3: {}", s3Key);
        } catch (Exception e) {
            log.error("Failed to delete file from S3: {}", s3Key, e);
            throw new GlobalException(ExceptionMessage.FILE_DELETE_FAILED);
        }
    }

    public String extractKeyFromUrl(String url) {
        if (!url.startsWith(domainUrlPrefix)) {
            throw new GlobalException(ExceptionMessage.INVALID_PARAMETER);
        }
        return url.substring(domainUrlPrefix.length());
    }

    public String generateUrl(String storedFileName) {
        return domainUrlPrefix + storedFileName;
    }
}
