package io.swkoreatech.kosp.domain.material.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.material.api.MaterialApi;
import io.swkoreatech.kosp.domain.material.dto.request.MaterialFolderCreateRequest;
import io.swkoreatech.kosp.domain.material.dto.request.MaterialFolderUpdateRequest;
import io.swkoreatech.kosp.domain.material.dto.request.MaterialImportRequest;
import io.swkoreatech.kosp.domain.material.dto.request.MaterialItemCreateRequest;
import io.swkoreatech.kosp.domain.material.dto.request.VisibilityUpdateRequest;
import io.swkoreatech.kosp.domain.material.dto.response.MaterialFolderResponse;
import io.swkoreatech.kosp.domain.material.dto.response.MaterialImportResponse;
import io.swkoreatech.kosp.domain.material.dto.response.MaterialItemResponse;
import io.swkoreatech.kosp.domain.material.service.MaterialImportService;
import io.swkoreatech.kosp.domain.material.service.MaterialService;
import io.swkoreatech.kosp.global.security.annotation.AuthUser;
import io.swkoreatech.kosp.global.security.annotation.Permit;
import lombok.RequiredArgsConstructor;

/**
 * 학습자료 관리 컨트롤러.
 * {@link MaterialApi}의 구현체.
 */
@RestController
@RequiredArgsConstructor
public class MaterialController implements MaterialApi {

    private final MaterialService materialService;
    private final MaterialImportService materialImportService;

    // ── 폴더 ──────────────────────────────────────────────────────────

    @Override
    @Permit(description = "내 학습자료 폴더 트리 조회")
    public ResponseEntity<List<MaterialFolderResponse>> getMyFolders(@AuthUser User user) {
        return ResponseEntity.ok(materialService.getMyFolders(user));
    }

    @Override
    @Permit(description = "시작 폴더 조회")
    public ResponseEntity<MaterialFolderResponse> getStartFolder(@AuthUser User user) {
        MaterialFolderResponse start = materialService.getStartFolder(user);
        return start == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(start);
    }

    @Override
    @Permit(description = "학습자료 폴더 생성")
    public ResponseEntity<MaterialFolderResponse> createFolder(
        @AuthUser User user, MaterialFolderCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(materialService.createFolder(user, request));
    }

    @Override
    @Permit(description = "학습자료 폴더 수정")
    public ResponseEntity<MaterialFolderResponse> updateFolder(
        @AuthUser User user, @PathVariable Long folderId, MaterialFolderUpdateRequest request
    ) {
        return ResponseEntity.ok(materialService.updateFolder(user, folderId, request));
    }

    @Override
    @Permit(description = "폴더 공개/비공개 변경")
    public ResponseEntity<MaterialFolderResponse> changeFolderVisibility(
        @AuthUser User user, @PathVariable Long folderId, VisibilityUpdateRequest request
    ) {
        return ResponseEntity.ok(materialService.changeFolderVisibility(user, folderId, request.visibility()));
    }

    @Override
    @Permit(description = "시작 폴더 지정")
    public ResponseEntity<MaterialFolderResponse> setStartFolder(
        @AuthUser User user, @PathVariable Long folderId
    ) {
        return ResponseEntity.ok(materialService.setStartFolder(user, folderId));
    }

    @Override
    @Permit(description = "학습자료 폴더 삭제")
    public ResponseEntity<Void> deleteFolder(@AuthUser User user, @PathVariable Long folderId) {
        materialService.deleteFolder(user, folderId);
        return ResponseEntity.noContent().build();
    }

    // ── 자료 아이템 ────────────────────────────────────────────────────

    @Override
    @Permit(description = "폴더 내 자료 조회")
    public ResponseEntity<List<MaterialItemResponse>> getFolderItems(
        @AuthUser User user, @PathVariable Long folderId
    ) {
        return ResponseEntity.ok(materialService.getFolderItems(user, folderId));
    }

    @Override
    @Permit(description = "최신 자료 조회")
    public ResponseEntity<List<MaterialItemResponse>> getRecentItems(@AuthUser User user, int limit) {
        return ResponseEntity.ok(materialService.getRecentItems(user, limit));
    }

    @Override
    @Permit(description = "학습자료 등록")
    public ResponseEntity<MaterialItemResponse> createItem(
        @AuthUser User user, MaterialItemCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(materialService.createItem(user, request));
    }

    @Override
    @Permit(description = "아우누리 과제/EL 자료 수집(import)")
    public ResponseEntity<MaterialImportResponse> importMaterials(
        @AuthUser User user, MaterialImportRequest request
    ) {
        return ResponseEntity.ok(materialImportService.importMaterials(user, request));
    }

    @Override
    @Permit(description = "자료 공개/비공개 변경")
    public ResponseEntity<MaterialItemResponse> changeItemVisibility(
        @AuthUser User user, @PathVariable Long itemId, VisibilityUpdateRequest request
    ) {
        return ResponseEntity.ok(materialService.changeItemVisibility(user, itemId, request.visibility()));
    }

    @Override
    @Permit(description = "학습자료 삭제")
    public ResponseEntity<Void> deleteItem(@AuthUser User user, @PathVariable Long itemId) {
        materialService.deleteItem(user, itemId);
        return ResponseEntity.noContent().build();
    }

    // ── 타인 공개 조회 ─────────────────────────────────────────────────

    @Override
    @Permit(permitAll = true, description = "공개 폴더 조회 (타인)")
    public ResponseEntity<List<MaterialFolderResponse>> getPublicFolders(@PathVariable Long userId) {
        return ResponseEntity.ok(materialService.getPublicFolders(userId));
    }

    @Override
    @Permit(permitAll = true, description = "공개 폴더 자료 조회 (타인)")
    public ResponseEntity<List<MaterialItemResponse>> getPublicFolderItems(
        @PathVariable Long userId, @PathVariable Long folderId
    ) {
        return ResponseEntity.ok(materialService.getPublicFolderItems(userId, folderId));
    }
}
