package kr.ac.koreatech.sw.kosp.domain.upload.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@AllArgsConstructor
public class FileUploadEvent {
    private final MultipartFile file;
    private final String storedFileName;
}
