package io.swkoreatech.kosp.domain.material.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.material.dto.request.MaterialFolderCreateRequest;
import io.swkoreatech.kosp.domain.material.dto.request.MaterialFolderUpdateRequest;
import io.swkoreatech.kosp.domain.material.dto.request.MaterialItemCreateRequest;
import io.swkoreatech.kosp.domain.material.dto.request.MaterialItemUpdateRequest;
import io.swkoreatech.kosp.domain.material.dto.response.DownloadUrlResponse;
import io.swkoreatech.kosp.domain.material.dto.response.MaterialFolderResponse;
import io.swkoreatech.kosp.domain.material.dto.response.MaterialItemResponse;
import io.swkoreatech.kosp.domain.material.model.MaterialFolder;
import io.swkoreatech.kosp.domain.material.model.MaterialItem;
import io.swkoreatech.kosp.domain.material.model.MaterialSource;
import io.swkoreatech.kosp.domain.material.model.Visibility;
import io.swkoreatech.kosp.domain.material.repository.MaterialFolderRepository;
import io.swkoreatech.kosp.domain.material.repository.MaterialItemRepository;
import io.swkoreatech.kosp.domain.upload.client.S3StorageClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 학습자료(과제/EL) 서비스.
 *
 * <p>연도·학기·과목 폴더 계층과 폴더별 공개/비공개를 관리하고,
 * 마이페이지 최신 자료 노출 및 첨부파일 페이지(폴더 진입)를 지원한다.</p>
 *
 * <h2>공개 여부 상속 규칙</h2>
 * <p>타인에게 노출되는 조건은 다음 세 가지를 모두 만족할 때다.</p>
 * <ol>
 *   <li>소속 폴더가 PUBLIC</li>
 *   <li>루트까지의 모든 상위 폴더가 PUBLIC (상위가 비공개면 하위는 전부 비공개)</li>
 *   <li>자료의 개별 override 가 PRIVATE 이 아님 (override 가 null 이면 폴더 설정을 상속)</li>
 * </ol>
 * <p>즉 비공개가 항상 우선한다. 비공개 폴더 안의 자료는 개별 PUBLIC 으로 지정돼 있어도 노출하지 않는다.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MaterialService {

    /** 폴더 계층 탐색 최대 깊이 (데이터 이상으로 인한 순환 방어). */
    private static final int MAX_FOLDER_DEPTH = 32;

    private final MaterialFolderRepository folderRepository;
    private final MaterialItemRepository itemRepository;
    private final S3StorageClient s3StorageClient;

    // ── 폴더 ──────────────────────────────────────────────────────────

    /**
     * 내 전체 폴더 트리를 조회한다 (자료 수 포함).
     */
    public List<MaterialFolderResponse> getMyFolders(User user) {
        return folderRepository.findAllByUserIdOrderBySortOrderAscIdAsc(user.getId()).stream()
            .map(f -> MaterialFolderResponse.from(f, itemRepository.countByFolderId(f.getId())))
            .toList();
    }

    /**
     * 특정 사용자의 공개 폴더 트리를 조회한다 (타인 조회용).
     *
     * <p>폴더 자신이 PUBLIC 이더라도 상위 폴더 중 하나라도 PRIVATE 이면 제외한다.
     * {@code itemCount} 에는 실제로 노출되는 공개 자료 수만 담는다.</p>
     */
    public List<MaterialFolderResponse> getPublicFolders(Long userId) {
        List<MaterialFolder> folders = folderRepository.findAllByUserIdOrderBySortOrderAscIdAsc(userId);
        Set<Long> publicFolderIds = resolvePublicFolderIds(folders);
        if (publicFolderIds.isEmpty()) {
            return List.of();
        }

        Map<Long, Long> publicCounts = countPublicItemsByFolder(publicFolderIds);
        return folders.stream()
            .filter(f -> publicFolderIds.contains(f.getId()))
            .map(f -> MaterialFolderResponse.fromPublic(f, publicCounts.getOrDefault(f.getId(), 0L)))
            .toList();
    }

    /**
     * 특정 사용자의 공개 자료 전체를 최근 학기·최신순으로 조회한다 (포트폴리오 노출용).
     *
     * @param userId 대상 사용자 ID
     * @param limit  최대 반환 개수 (0 이하이면 전체)
     */
    public List<MaterialItemResponse> getPublicItems(Long userId, int limit) {
        List<MaterialFolder> folders = folderRepository.findAllByUserIdOrderBySortOrderAscIdAsc(userId);
        Set<Long> publicFolderIds = resolvePublicFolderIds(folders);
        if (publicFolderIds.isEmpty()) {
            return List.of();
        }

        java.util.stream.Stream<MaterialItemResponse> stream = itemRepository
            .findAllByFolderIdsOrderByRecentSemester(List.copyOf(publicFolderIds)).stream()
            .filter(item -> item.getVisibility() != Visibility.PRIVATE)
            .map(MaterialItemResponse::fromPublic);

        if (limit > 0) {
            stream = stream.limit(limit);
        }
        return stream.toList();
    }

    /**
     * 첨부파일 페이지 진입 시 사용할 시작 폴더를 조회한다.
     * 지정된 시작 폴더가 없으면 가장 첫 폴더를 반환하고, 폴더가 하나도 없으면 null.
     */
    public MaterialFolderResponse getStartFolder(User user) {
        MaterialFolder folder = folderRepository.findByUserIdAndIsStartFolderTrue(user.getId())
            .orElseGet(() -> folderRepository
                .findAllByUserIdOrderBySortOrderAscIdAsc(user.getId()).stream()
                .findFirst()
                .orElse(null));
        if (folder == null) {
            return null;
        }
        return MaterialFolderResponse.from(folder, itemRepository.countByFolderId(folder.getId()));
    }

    /**
     * 폴더를 생성한다. 상위 폴더 지정 시 본인 소유 여부를 검증한다.
     */
    @Transactional
    public MaterialFolderResponse createFolder(User user, MaterialFolderCreateRequest request) {
        MaterialFolder parent = null;
        if (request.parentId() != null) {
            parent = folderRepository.findByIdAndUserId(request.parentId(), user.getId())
                .orElseThrow(() -> new GlobalException(ExceptionMessage.NOT_FOUND));
        }

        MaterialFolder folder = MaterialFolder.builder()
            .user(user)
            .parent(parent)
            .name(request.name())
            .folderType(request.folderType())
            .source(request.source())
            .visibility(request.visibility())
            .isStartFolder(false)
            .sortOrder(request.sortOrder() == null ? 0 : request.sortOrder())
            .build();

        MaterialFolder saved = folderRepository.save(folder);
        log.info("학습자료 폴더 생성: userId={}, folderId={}", user.getId(), saved.getId());
        return MaterialFolderResponse.from(saved, 0);
    }

    /**
     * 폴더 기본 정보를 수정한다 (본인 소유 검증).
     */
    @Transactional
    public MaterialFolderResponse updateFolder(User user, Long folderId, MaterialFolderUpdateRequest request) {
        MaterialFolder folder = getOwnedFolder(user, folderId);
        folder.update(request.name(), request.folderType(),
            request.sortOrder() == null ? folder.getSortOrder() : request.sortOrder());
        return MaterialFolderResponse.from(folder, itemRepository.countByFolderId(folderId));
    }

    /**
     * 폴더 공개 범위를 변경한다 (본인 소유 검증).
     */
    @Transactional
    public MaterialFolderResponse changeFolderVisibility(User user, Long folderId, Visibility visibility) {
        MaterialFolder folder = getOwnedFolder(user, folderId);
        folder.changeVisibility(visibility);
        log.info("폴더 공개설정 변경: userId={}, folderId={}, visibility={}", user.getId(), folderId, visibility);
        return MaterialFolderResponse.from(folder, itemRepository.countByFolderId(folderId));
    }

    /**
     * 특정 폴더를 시작 폴더로 지정한다 (기존 시작 폴더는 해제).
     */
    @Transactional
    public MaterialFolderResponse setStartFolder(User user, Long folderId) {
        MaterialFolder folder = getOwnedFolder(user, folderId);
        folderRepository.clearStartFolderByUserId(user.getId());
        folder.setStartFolder(true);
        return MaterialFolderResponse.from(folder, itemRepository.countByFolderId(folderId));
    }

    /**
     * 폴더를 삭제한다 (본인 소유 검증). 자료가 남아있으면 삭제를 막는다.
     */
    @Transactional
    public void deleteFolder(User user, Long folderId) {
        MaterialFolder folder = getOwnedFolder(user, folderId);
        if (itemRepository.countByFolderId(folderId) > 0) {
            throw new GlobalException(ExceptionMessage.BAD_REQUEST);
        }
        folderRepository.delete(folder);
        log.info("학습자료 폴더 삭제: userId={}, folderId={}", user.getId(), folderId);
    }

    /**
     * 폴더를 다른 상위 폴더로 이동한다 (본인 소유 검증 + 순환참조 방지).
     * {@code parentId} 가 null 이면 최상위로 이동한다.
     */
    @Transactional
    public MaterialFolderResponse moveFolder(User user, Long folderId, Long parentId) {
        MaterialFolder folder = getOwnedFolder(user, folderId);

        MaterialFolder newParent = null;
        if (parentId != null) {
            if (parentId.equals(folderId)) {
                throw new GlobalException(ExceptionMessage.BAD_REQUEST);
            }
            newParent = getOwnedFolder(user, parentId);
            // 자기 자신의 하위(자손) 폴더로 이동하면 순환참조가 되므로 차단한다.
            for (MaterialFolder cursor = newParent; cursor != null; cursor = cursor.getParent()) {
                if (cursor.getId().equals(folderId)) {
                    throw new GlobalException(ExceptionMessage.BAD_REQUEST);
                }
            }
        }

        folder.changeParent(newParent);
        log.info("학습자료 폴더 이동: userId={}, folderId={}, parentId={}", user.getId(), folderId, parentId);
        return MaterialFolderResponse.from(folder, itemRepository.countByFolderId(folderId));
    }

    // ── 자료 아이템 ────────────────────────────────────────────────────

    /**
     * 폴더 내 자료를 최신순으로 조회한다 (본인 소유 검증).
     */
    public List<MaterialItemResponse> getFolderItems(User user, Long folderId) {
        getOwnedFolder(user, folderId);
        return itemRepository.findAllByFolderIdOrderByMaterialDateDescIdDesc(folderId).stream()
            .map(MaterialItemResponse::from)
            .toList();
    }

    /**
     * 마이페이지 노출용 최신 자료를 조회한다.
     */
    public List<MaterialItemResponse> getRecentItems(User user, int limit) {
        return itemRepository
            .findAllByUserIdOrderByMaterialDateDescIdDesc(user.getId(), PageRequest.of(0, limit)).stream()
            .map(MaterialItemResponse::from)
            .toList();
    }

    /**
     * 폴더 내 공개 자료를 최근 학기·최신순으로 조회한다 (타인 조회용).
     *
     * <p>폴더 자신 또는 상위 폴더가 비공개면 폴더의 존재를 드러내지 않도록 404 로 응답한다.</p>
     */
    public List<MaterialItemResponse> getPublicFolderItems(Long userId, Long folderId) {
        List<MaterialFolder> folders = folderRepository.findAllByUserIdOrderBySortOrderAscIdAsc(userId);
        Set<Long> publicFolderIds = resolvePublicFolderIds(folders);
        if (!publicFolderIds.contains(folderId)) {
            throw new GlobalException(ExceptionMessage.NOT_FOUND);
        }
        return itemRepository.findAllByFolderIdsOrderByRecentSemester(List.of(folderId)).stream()
            .filter(item -> item.getVisibility() != Visibility.PRIVATE)
            .map(MaterialItemResponse::fromPublic)
            .toList();
    }

    /**
     * 공개 자료의 다운로드 전용 presigned URL 을 발급한다 (타인 조회용).
     *
     * <p>비공개 자료의 itemId 를 직접 넣어 호출해도 공개 판정을 통과하지 못하면 404 로 막는다.
     * 발급되는 URL 은 만료 시간이 있는 presigned URL 이다.</p>
     */
    public DownloadUrlResponse getPublicItemDownloadUrl(Long userId, Long itemId) {
        MaterialItem item = itemRepository.findByIdAndUserId(itemId, userId)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.NOT_FOUND));

        List<MaterialFolder> folders = folderRepository.findAllByUserIdOrderBySortOrderAscIdAsc(userId);
        Set<Long> publicFolderIds = resolvePublicFolderIds(folders);
        boolean visible = publicFolderIds.contains(item.getFolder().getId())
            && item.getVisibility() != Visibility.PRIVATE;
        if (!visible) {
            log.info("비공개 자료 다운로드 차단: ownerId={}, itemId={}", userId, itemId);
            throw new GlobalException(ExceptionMessage.NOT_FOUND);
        }
        return presignDownload(item);
    }

    /**
     * 자료를 등록한다. 소속 폴더는 본인 소유여야 한다.
     */
    @Transactional
    public MaterialItemResponse createItem(User user, MaterialItemCreateRequest request) {
        MaterialFolder folder = getOwnedFolder(user, request.folderId());

        MaterialSource source = request.source() == null ? MaterialSource.MANUAL : request.source();
        LocalDateTime materialDate = request.materialDate() == null ? LocalDateTime.now() : request.materialDate();

        MaterialItem item = MaterialItem.builder()
            .folder(folder)
            .user(user)
            .title(request.title())
            .subjectName(request.subjectName())
            .materialYear(request.materialYear())
            .semester(request.semester())
            .source(source)
            .sourceUrl(request.sourceUrl())
            .fileUrl(request.fileUrl())
            .originalFileName(request.originalFileName())
            .fileSize(request.fileSize())
            .contentType(request.contentType())
            .visibility(request.visibility())
            .materialDate(materialDate)
            .build();

        MaterialItem saved = itemRepository.save(item);
        log.info("학습자료 등록: userId={}, itemId={}, source={}", user.getId(), saved.getId(), source);
        return MaterialItemResponse.from(saved);
    }

    /**
     * 자료 공개 범위(override)를 변경한다 (본인 소유 검증).
     */
    @Transactional
    public MaterialItemResponse changeItemVisibility(User user, Long itemId, Visibility visibility) {
        MaterialItem item = itemRepository.findByIdAndUserId(itemId, user.getId())
            .orElseThrow(() -> new GlobalException(ExceptionMessage.NOT_FOUND));
        item.changeVisibility(visibility);
        return MaterialItemResponse.from(item);
    }

    /**
     * 자료 메타데이터를 수정한다 (이름 바꾸기 등, 본인 소유 검증).
     */
    @Transactional
    public MaterialItemResponse updateItem(User user, Long itemId, MaterialItemUpdateRequest request) {
        MaterialItem item = getOwnedItem(user, itemId);
        item.update(request.title(), request.subjectName(), request.materialYear(), request.semester());
        return MaterialItemResponse.from(item);
    }

    /**
     * 자료를 다른 폴더로 이동한다 (자료·대상 폴더 모두 본인 소유 검증).
     */
    @Transactional
    public MaterialItemResponse moveItem(User user, Long itemId, Long folderId) {
        MaterialItem item = getOwnedItem(user, itemId);
        MaterialFolder targetFolder = getOwnedFolder(user, folderId);
        item.moveTo(targetFolder);
        log.info("학습자료 이동: userId={}, itemId={}, folderId={}", user.getId(), itemId, folderId);
        return MaterialItemResponse.from(item);
    }

    /**
     * 자료를 삭제한다 (본인 소유 검증).
     */
    @Transactional
    public void deleteItem(User user, Long itemId) {
        MaterialItem item = getOwnedItem(user, itemId);
        itemRepository.delete(item);
        log.info("학습자료 삭제: userId={}, itemId={}", user.getId(), itemId);
    }

    /**
     * 자료 파일의 다운로드 전용 presigned URL을 발급한다 (본인 소유 검증).
     *
     * <p>S3에 저장된 파일만 지원한다. 아우누리 원본 링크 등 외부 URL 파일은 다운로드 URL을 발급할 수 없다.</p>
     */
    public DownloadUrlResponse getItemDownloadUrl(User user, Long itemId) {
        return presignDownload(getOwnedItem(user, itemId));
    }

    // ── private helpers ───────────────────────────────────────────────

    private DownloadUrlResponse presignDownload(MaterialItem item) {
        if (item.getFileUrl() == null || item.getFileUrl().isBlank()) {
            throw new GlobalException(ExceptionMessage.BAD_REQUEST);
        }
        String key = s3StorageClient.extractKeyFromUrl(item.getFileUrl());
        String url = s3StorageClient.getPresignedDownloadUrl(key, item.getOriginalFileName());
        return new DownloadUrlResponse(url);
    }

    /**
     * 타인에게 노출 가능한 폴더 ID 집합을 계산한다.
     *
     * <p>폴더 자신이 PUBLIC 이고, 루트까지의 모든 상위 폴더도 PUBLIC 인 경우에만 공개로 본다.
     * 상위 폴더가 다른 사용자 소유이거나 목록에 없으면(데이터 이상) 비공개로 간주한다.</p>
     *
     * @param folders 대상 사용자의 전체 폴더 목록
     */
    private Set<Long> resolvePublicFolderIds(List<MaterialFolder> folders) {
        if (folders.isEmpty()) {
            return Set.of();
        }
        Map<Long, MaterialFolder> byId = folders.stream()
            .collect(Collectors.toMap(MaterialFolder::getId, Function.identity(), (left, right) -> left));

        Map<Long, Boolean> resolved = new HashMap<>();
        Set<Long> publicIds = new LinkedHashSet<>();
        for (MaterialFolder folder : folders) {
            if (isChainPublic(folder, byId, resolved)) {
                publicIds.add(folder.getId());
            }
        }
        return publicIds;
    }

    /**
     * 폴더와 그 모든 조상이 PUBLIC 인지 판정한다 (결과는 {@code resolved} 에 메모이즈).
     */
    private boolean isChainPublic(
        MaterialFolder folder,
        Map<Long, MaterialFolder> byId,
        Map<Long, Boolean> resolved
    ) {
        Boolean cached = resolved.get(folder.getId());
        if (cached != null) {
            return cached;
        }

        boolean result = true;
        Set<Long> visited = new HashSet<>();
        MaterialFolder cursor = folder;
        for (int depth = 0; cursor != null && depth < MAX_FOLDER_DEPTH; depth++) {
            if (!visited.add(cursor.getId())) {
                result = false; // 순환 참조 방어
                break;
            }
            if (!cursor.isPublic()) {
                result = false;
                break;
            }
            MaterialFolder parent = cursor.getParent();
            if (parent == null) {
                break;
            }
            // 프록시 초기화를 피하려고 ID 로 같은 사용자의 폴더 목록에서 찾는다.
            cursor = byId.get(parent.getId());
            if (cursor == null) {
                result = false; // 소유자 폴더 목록에 없는 상위 = 판정 불가 → 비공개 취급
                break;
            }
        }

        resolved.put(folder.getId(), result);
        return result;
    }

    /**
     * 폴더별 공개 자료 수를 집계한다 (공개 폴더 응답의 itemCount 용).
     */
    private Map<Long, Long> countPublicItemsByFolder(Set<Long> publicFolderIds) {
        return itemRepository.findAllByFolderIdsOrderByRecentSemester(List.copyOf(publicFolderIds)).stream()
            .filter(item -> item.getVisibility() != Visibility.PRIVATE)
            .collect(Collectors.groupingBy(
                item -> item.getFolder().getId(),
                Collectors.counting()
            ));
    }

    private MaterialItem getOwnedItem(User user, Long itemId) {
        return itemRepository.findByIdAndUserId(itemId, user.getId())
            .orElseThrow(() -> new GlobalException(ExceptionMessage.NOT_FOUND));
    }

    private MaterialFolder getOwnedFolder(User user, Long folderId) {
        return folderRepository.findByIdAndUserId(folderId, user.getId())
            .orElseThrow(() -> new GlobalException(ExceptionMessage.NOT_FOUND));
    }
}
