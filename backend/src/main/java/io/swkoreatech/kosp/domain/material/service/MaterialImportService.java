package io.swkoreatech.kosp.domain.material.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.material.dto.request.MaterialImportRequest;
import io.swkoreatech.kosp.domain.material.dto.response.MaterialImportResponse;
import io.swkoreatech.kosp.domain.material.dto.response.MaterialItemResponse;
import io.swkoreatech.kosp.domain.material.model.FolderType;
import io.swkoreatech.kosp.domain.material.model.MaterialFolder;
import io.swkoreatech.kosp.domain.material.model.MaterialItem;
import io.swkoreatech.kosp.domain.material.model.MaterialSource;
import io.swkoreatech.kosp.domain.material.model.Visibility;
import io.swkoreatech.kosp.domain.material.repository.MaterialFolderRepository;
import io.swkoreatech.kosp.domain.material.repository.MaterialItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 아우누리 과제/EL 자료 반자동 수집(import) · 동기화 서비스.
 *
 * <p>학교 인증은 서버가 다루지 않는다. 브라우저 확장이 사용자의 세션에서 스크랩한
 * 정규화 데이터를 받아 {@code (userId, source, sourceExternalId)} 기준으로 upsert 한다.</p>
 *
 * <ul>
 *   <li>신규 자료: 출처별 자동 폴더에 PRIVATE(비공개 기본값)로 생성, {@code autoImported=true}.</li>
 *   <li>기존 자료: {@code contentHash} 비교로 변경분만 갱신하고 {@code lastSyncedAt} 기록.</li>
 * </ul>
 *
 * <p>사용자가 조정한 공개 설정/폴더 위치는 재동기화 시 보존한다(덮어쓰지 않음).</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MaterialImportService {

    private final MaterialFolderRepository folderRepository;
    private final MaterialItemRepository itemRepository;
    private final MaterialDuplicateDetector duplicateDetector;

    /**
     * 수집된 자료 목록을 upsert 하고, GitHub 프로젝트와의 중복 가능성을 판정한다.
     */
    @Transactional
    public MaterialImportResponse importMaterials(User user, MaterialImportRequest request) {
        LocalDateTime now = LocalDateTime.now();
        int created = 0;
        int updated = 0;
        int unchanged = 0;
        List<MaterialItem> processed = new ArrayList<>();

        for (MaterialImportRequest.Item item : request.items()) {
            MaterialSource source = item.source();
            Integer semesterOrder = semesterOrder(item.materialYear(), item.semester());
            String hash = contentHash(item);

            Optional<MaterialItem> existing = itemRepository
                .findByUserIdAndSourceAndSourceExternalId(user.getId(), source, item.sourceExternalId());

            MaterialItem entity;
            if (existing.isPresent()) {
                entity = existing.get();
                if (hash.equals(entity.getContentHash())) {
                    entity.touchSynced(now);
                    unchanged++;
                } else {
                    entity.syncFrom(
                        item.title(), item.subjectName(), item.materialYear(), item.semester(), semesterOrder,
                        item.sourceUrl(), item.fileUrl(), item.originalFileName(), item.fileSize(),
                        item.contentType(), item.materialDate(), hash, now
                    );
                    updated++;
                }
            } else {
                MaterialFolder folder = resolveAutoFolder(user, source);
                MaterialItem newItem = MaterialItem.builder()
                    .folder(folder)
                    .user(user)
                    .title(item.title())
                    .subjectName(item.subjectName())
                    .materialYear(item.materialYear())
                    .semester(item.semester())
                    .source(source)
                    .sourceUrl(item.sourceUrl())
                    .fileUrl(item.fileUrl())
                    .originalFileName(item.originalFileName())
                    .fileSize(item.fileSize())
                    .contentType(item.contentType())
                    // visibility=null → 폴더(PRIVATE) 설정을 상속 = 비공개 기본값 정책
                    .materialDate(item.materialDate() == null ? now : item.materialDate())
                    .sourceExternalId(item.sourceExternalId())
                    .semesterOrder(semesterOrder)
                    .autoImported(true)
                    .contentHash(hash)
                    .lastSyncedAt(now)
                    .build();
                entity = itemRepository.save(newItem);
                created++;
            }
            processed.add(entity);
        }

        // GitHub 프로젝트와의 중복 가능성 판정 (자동 병합 없이 안내 플래그만 설정)
        duplicateDetector.detect(user, processed);

        List<MaterialItemResponse> results = processed.stream()
            .map(MaterialItemResponse::from)
            .toList();

        log.info("학습자료 import: userId={}, created={}, updated={}, unchanged={}",
            user.getId(), created, updated, unchanged);
        return new MaterialImportResponse(created, updated, unchanged, results);
    }

    // ── private helpers ───────────────────────────────────────────────

    /**
     * 출처별 자동 배정 폴더를 조회하거나(없으면) 생성한다. 공개 기본값은 PRIVATE.
     */
    private MaterialFolder resolveAutoFolder(User user, MaterialSource source) {
        String name = source == MaterialSource.AUNURI_EL ? "아우누리 EL" : "아우누리 과제";
        return folderRepository.findFirstByUserIdAndSourceAndName(user.getId(), source, name)
            .orElseGet(() -> folderRepository.save(MaterialFolder.builder()
                .user(user)
                .name(name)
                .folderType(FolderType.CUSTOM)
                .source(source)
                .visibility(Visibility.PRIVATE)
                .isStartFolder(false)
                .sortOrder(0)
                .build()));
    }

    /**
     * 최근학기 정렬키를 계산한다. year*10 + term (1학기1 / 여름2 / 2학기3 / 겨울4).
     */
    private Integer semesterOrder(Integer year, String semester) {
        if (year == null) {
            return null;
        }
        return year * 10 + termCode(semester);
    }

    private int termCode(String semester) {
        if (semester == null) {
            return 0;
        }
        if (semester.contains("여름")) {
            return 2;
        }
        if (semester.contains("겨울")) {
            return 4;
        }
        if (semester.contains("2")) {
            return 3;
        }
        if (semester.contains("1")) {
            return 1;
        }
        return 0;
    }

    /**
     * 변경 감지용 콘텐츠 해시(SHA-256). 원본 메타데이터가 바뀌면 값이 달라진다.
     */
    private String contentHash(MaterialImportRequest.Item item) {
        String raw = String.join("|",
            nullSafe(item.title()),
            nullSafe(item.subjectName()),
            String.valueOf(item.materialYear()),
            nullSafe(item.semester()),
            nullSafe(item.sourceUrl()),
            nullSafe(item.fileUrl()),
            String.valueOf(item.materialDate())
        );
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new GlobalException(ExceptionMessage.SERVER_ERROR);
        }
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }
}
