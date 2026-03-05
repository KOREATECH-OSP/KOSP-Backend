package io.swkoreatech.kosp.domain.upload.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.domain.upload.dto.response.UploadUrlResponse;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

/**
 * S3 스토리지 클라이언트.
 * AWS S3에 대한 파일 업로드, 삭제, presigned URL 생성 기능을 제공한다.
 */
@Slf4j
@Component
public class S3StorageClient {

    private static final int URL_EXPIRATION_MINUTES = 10;

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucketName;
    private final String domainUrlPrefix;
    private final Clock clock;

    public S3StorageClient(
        S3Client s3Client,
        S3Presigner s3Presigner,
        @Value("${aws.s3.bucket}") String bucketName,
        @Value("${aws.s3.custom_domain}") String customDomain,
        Clock clock
    ) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.bucketName = bucketName;
        this.domainUrlPrefix = customDomain;
        this.clock = clock;
    }

    /**
     * S3 업로드용 presigned URL을 생성한다.
     *
     * @param uploadFilePath 업로드 파일 경로
     * @param contentLength 파일 크기
     * @param contentType 파일 MIME 타입
     * @return 업로드 URL 응답
     */
    public UploadUrlResponse getPresignedUrl(String uploadFilePath, Long contentLength, String contentType) {
        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(URL_EXPIRATION_MINUTES))
            .putObjectRequest(builder -> builder
                .bucket(bucketName)
                .key(uploadFilePath)
                .contentLength(contentLength)
                .contentType(contentType)
                .build()
            )
            .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);

        return new UploadUrlResponse(
            presignedRequest.url().toExternalForm(),
            domainUrlPrefix + uploadFilePath,
            LocalDateTime.now(clock).plusMinutes(URL_EXPIRATION_MINUTES)
        );
    }

    /**
     * MultipartFile을 S3에 업로드한다.
     *
     * @param uploadFilePath 업로드 파일 경로
     * @param file 업로드할 파일
     * @return 업로드된 파일의 URL
     * @throws GlobalException 파일 업로드 실패 시
     */
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

    /**
     * 바이트 배열을 S3에 업로드한다.
     *
     * @param uploadFilePath 업로드 파일 경로
     * @param fileData 업로드할 파일 데이터
     * @return 업로드된 파일의 URL
     */
    public String uploadFile(String uploadFilePath, byte[] fileData) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(uploadFilePath)
            .contentLength((long)fileData.length)
            .acl(ObjectCannedACL.PUBLIC_READ)
            .build();

        s3Client.putObject(
            putObjectRequest,
            RequestBody.fromInputStream(new ByteArrayInputStream(fileData), fileData.length)
        );
        log.info("File uploaded successfully to S3: {}", uploadFilePath);

        return domainUrlPrefix + uploadFilePath;
    }

    /**
     * S3에서 파일을 삭제한다.
     *
     * @param s3Key S3 객체 키
     * @throws GlobalException 파일 삭제 실패 시
     */
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

    /**
     * URL에서 S3 객체 키를 추출한다.
     *
     * @param url 파일 URL
     * @return S3 객체 키
     * @throws GlobalException 잘못된 URL인 경우
     */
    public String extractKeyFromUrl(String url) {
        if (!url.startsWith(domainUrlPrefix)) {
            throw new GlobalException(ExceptionMessage.INVALID_PARAMETER);
        }
        return url.substring(domainUrlPrefix.length());
    }

    /**
     * 저장된 파일명으로 접근 가능한 URL을 생성한다.
     *
     * @param storedFileName 저장된 파일명
     * @return 파일 URL
     */
    public String generateUrl(String storedFileName) {
        return domainUrlPrefix + storedFileName;
    }
}
