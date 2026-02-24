package io.swkoreatech.kosp.domain.upload.event;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FileUploadEvent {

    private final MultipartFile file;
    private final String storedFileName;
}
