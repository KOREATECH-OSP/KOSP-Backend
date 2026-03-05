package io.swkoreatech.kosp.domain.upload.event;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 파일 업로드 이벤트.
 * 파일 업로드 시 발행되는 애플리케이션 이벤트이다.
 */
@Getter
@AllArgsConstructor
public class FileUploadEvent {

    private final MultipartFile file;
    private final String storedFileName;
}
