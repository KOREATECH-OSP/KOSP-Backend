package io.swkoreatech.kosp.domain.resume.service;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.resume.export.ResumeHwpxExporter;
import io.swkoreatech.kosp.domain.resume.model.UserResume;
import io.swkoreatech.kosp.domain.resume.repository.UserResumeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 이력서 내려받기(hwpx) 서비스.
 *
 * <h2>권한</h2>
 * <p>기존 이력서 내려받기는 프론트엔드가 DOM 을 캡처하는 방식이라, "화면에 없으면 못 뽑는다" 는
 * 성질에 보호를 기대고 있었다. 서버 엔드포인트가 생기는 순간 그 보호는 사라지므로 여기서
 * 명시적으로 검증한다.</p>
 *
 * <ul>
 *   <li>본인 이력서: {@code (resumeId, userId)} 로 조회해 소유권을 확인한다.</li>
 *   <li>타인 이력서: 소유자 일치 + {@code resume_data.isPublic == true} 일 때만 허용한다.</li>
 *   <li>어느 쪽이든 실패는 <b>404</b> 다. 403 을 쓰면 "그 이력서는 존재하지만 비공개" 라는
 *       사실이 새어 나가므로, 기존 {@code ResumeService.getPublicResumeById} 와 동일하게 숨긴다.</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResumeExportService {

    private final UserResumeRepository userResumeRepository;
    private final ResumeHwpxExporter hwpxExporter;
    private final ObjectMapper objectMapper;

    /**
     * 본인 이력서를 hwpx 로 내보낸다.
     *
     * @param user     인증된 사용자
     * @param resumeId 이력서 식별자
     * @throws GlobalException 본인 소유가 아니거나 없으면 {@code NOT_FOUND}
     */
    public HwpxDocument exportMine(User user, Long resumeId) {
        UserResume resume = userResumeRepository.findByIdAndUserId(resumeId, user.getId())
            .orElseThrow(() -> new GlobalException(ExceptionMessage.NOT_FOUND));
        return toDocument(resume);
    }

    /**
     * 타인의 공개 이력서를 hwpx 로 내보낸다.
     *
     * @param userId   이력서 소유자 식별자
     * @param resumeId 이력서 식별자
     * @throws GlobalException 없거나 비공개면 {@code NOT_FOUND}
     */
    public HwpxDocument exportPublic(Long userId, Long resumeId) {
        UserResume resume = userResumeRepository.findByIdAndUserId(resumeId, userId)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.NOT_FOUND));

        if (!isPublic(resume.getResumeData())) {
            log.info("[ResumeHwpx] 비공개 이력서 내려받기 차단: ownerId={}, resumeId={}", userId, resumeId);
            throw new GlobalException(ExceptionMessage.NOT_FOUND);
        }
        return toDocument(resume);
    }

    private HwpxDocument toDocument(UserResume resume) {
        String title = hwpxExporter.resolveTitle(resume.getResumeData());
        byte[] content = hwpxExporter.export(resume.getResumeData(), resume.getId());
        return new HwpxDocument(title + ".hwpx", content);
    }

    /**
     * {@code resume_data} 의 공개 여부를 판정한다.
     *
     * <p>{@code ResumeService#isPublic} 과 동일한 규칙이다. 파싱 실패 시 <b>비공개</b>로 본다
     * (실패를 공개로 해석하면 비공개 이력서가 새어 나간다).</p>
     */
    private boolean isPublic(String json) {
        if (json == null || json.isBlank()) {
            return false;
        }
        try {
            Map<?, ?> map = objectMapper.readValue(json, Map.class);
            return Boolean.TRUE.equals(map.get("isPublic"));
        } catch (Exception e) {
            log.warn("[ResumeHwpx] isPublic 파싱 실패 — 비공개로 처리합니다. error={}", e.getMessage());
            return false;
        }
    }

    /**
     * 생성된 hwpx 문서. 디스크·S3 를 거치지 않고 메모리에만 존재한다.
     *
     * @param fileName 확장자를 포함한 다운로드 파일명
     * @param content  hwpx 바이트
     */
    public record HwpxDocument(String fileName, byte[] content) {
    }
}
