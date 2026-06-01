package io.swkoreatech.kosp.domain.admin.title.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.common.title.model.Title;
import io.swkoreatech.kosp.domain.upload.client.S3StorageClient;
import io.swkoreatech.kosp.common.title.model.TitleAdminLog;
import io.swkoreatech.kosp.common.title.model.TitleBatchLog;
import io.swkoreatech.kosp.common.title.model.UserTitle;
import io.swkoreatech.kosp.common.title.model.enums.TitleAdminAction;
import io.swkoreatech.kosp.common.title.model.enums.TitleGrantSource;
import io.swkoreatech.kosp.common.title.repository.TitleAdminLogRepository;
import io.swkoreatech.kosp.common.title.repository.TitleBatchLogRepository;
import io.swkoreatech.kosp.common.title.repository.TitleRepository;
import io.swkoreatech.kosp.common.title.repository.UserTitleRepository;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.common.user.repository.UserRepository;
import io.swkoreatech.kosp.domain.admin.title.dto.request.AdminTitleGrantRequest;
import io.swkoreatech.kosp.domain.admin.title.dto.request.AdminTitleRevokeRequest;
import io.swkoreatech.kosp.domain.admin.title.dto.response.AdminTitleBatchLogListResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 관리자 칭호 수동 지급/회수 서비스.
 *
 * <p>모든 조작 이력은 {@link TitleAdminLog}에 기록된다.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminTitleService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final List<String> ALLOWED_CONTENT_TYPES =
        List.of("image/png", "image/jpeg", "image/jpg", "image/webp");

    private final TitleRepository titleRepository;
    private final UserTitleRepository userTitleRepository;
    private final UserRepository userRepository;
    private final TitleBatchLogRepository titleBatchLogRepository;
    private final TitleAdminLogRepository titleAdminLogRepository;
    private final S3StorageClient s3StorageClient;

    /**
     * 관리자가 특정 유저에게 칭호를 수동 지급한다.
     *
     * @param request 지급 요청 (userId, titleId, reason)
     * @param adminId 조작한 관리자 user_id
     */
    @Transactional
    public void grantTitle(AdminTitleGrantRequest request, Long adminId) {
        User user = userRepository.getById(request.userId());
        Title title = titleRepository.getById(request.titleId());

        // 이미 보유 중인지 확인
        if (userTitleRepository.findByUserAndTitleAndIsRevokedFalse(user, title).isPresent()) {
            throw new GlobalException(ExceptionMessage.TITLE_ALREADY_GRANTED);
        }

        UserTitle userTitle = UserTitle.builder()
            .user(user)
            .title(title)
            .isDisplay(false)
            .grantSource(TitleGrantSource.ADMIN)
            .grantedAt(LocalDateTime.now())
            .build();
        userTitleRepository.save(userTitle);

        saveAdminLog(adminId, user.getId(), title, TitleAdminAction.GRANT, request.reason());

        log.info("[AdminTitle] GRANT: adminId={} → userId={}, titleId={} ({}), reason={}",
            adminId, user.getId(), title.getId(), title.getName(), request.reason());
    }

    /**
     * 관리자가 특정 유저의 칭호를 회수한다 (소프트 회수).
     *
     * @param request 회수 요청 (userTitleId, reason)
     * @param adminId 조작한 관리자 user_id
     */
    @Transactional
    public void revokeTitle(AdminTitleRevokeRequest request, Long adminId) {
        UserTitle userTitle = userTitleRepository.getById(request.userTitleId());

        if (userTitle.isRevoked()) {
            throw new GlobalException(ExceptionMessage.TITLE_REVOKED);
        }

        userTitle.revoke(adminId);
        userTitleRepository.save(userTitle);

        saveAdminLog(adminId, userTitle.getUser().getId(), userTitle.getTitle(),
            TitleAdminAction.REVOKE, request.reason());

        log.info("[AdminTitle] REVOKE: adminId={} → userId={}, titleId={} ({}), userTitleId={}, reason={}",
            adminId, userTitle.getUser().getId(), userTitle.getTitle().getId(),
            userTitle.getTitle().getName(), userTitle.getId(), request.reason());
    }

    /**
     * 배치 실행 이력을 페이지 조회한다.
     *
     * @param pageable 페이징 정보
     * @return 배치 로그 목록
     */
    public AdminTitleBatchLogListResponse getBatchLogs(Pageable pageable) {
        Page<TitleBatchLog> page = titleBatchLogRepository.findAllByOrderByExecutedAtDesc(pageable);
        return AdminTitleBatchLogListResponse.from(page);
    }

    /**
     * 칭호 아이콘 URL을 수정한다 (URL 직접 입력).
     *
     * @param titleId 칭호 PK
     * @param iconUrl 새 아이콘 URL (null 이면 초기화)
     */
    @Transactional
    public void updateTitleImage(Long titleId, String iconUrl) {
        Title title = titleRepository.getById(titleId);
        title.updateIconUrl(iconUrl);
        log.info("[AdminTitle] UPDATE_IMAGE: titleId={} ({}), iconUrl={}", title.getId(), title.getName(), iconUrl);
    }

    /**
     * 이미지 파일을 S3에 업로드하고 칭호 아이콘 URL을 갱신한다.
     *
     * @param titleId 칭호 PK
     * @param file    업로드할 이미지 파일 (png/jpg/jpeg/webp, 최대 5MB)
     * @return 업로드된 이미지의 URL
     */
    @Transactional
    public String uploadTitleImage(Long titleId, MultipartFile file) {
        validateImageFile(file);

        Title title = titleRepository.getById(titleId);

        String ext = getExtension(file.getOriginalFilename());
        String s3Path = "titles/" + title.getCode() + "-" + System.currentTimeMillis() + ext;

        String iconUrl = s3StorageClient.uploadFile(s3Path, file);
        title.updateIconUrl(iconUrl);

        log.info("[AdminTitle] UPLOAD_IMAGE: titleId={} ({}), s3Path={}, iconUrl={}",
            title.getId(), title.getName(), s3Path, iconUrl);

        return iconUrl;
    }

    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new GlobalException(ExceptionMessage.INVALID_PARAMETER);
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new GlobalException(ExceptionMessage.INVALID_FILE_TYPE);
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new GlobalException(ExceptionMessage.FILE_TOO_LARGE);
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf("."));
    }

    private void saveAdminLog(Long adminId, Long userId, Title title, TitleAdminAction action, String reason) {
        TitleAdminLog log = TitleAdminLog.builder()
            .adminId(adminId)
            .userId(userId)
            .title(title)
            .action(action)
            .reason(reason)
            .build();
        titleAdminLogRepository.save(log);
    }
}
