package io.swkoreatech.kosp.domain.material.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.material.dto.request.MaterialFolderCreateRequest;
import io.swkoreatech.kosp.domain.material.dto.request.MaterialFolderUpdateRequest;
import io.swkoreatech.kosp.domain.material.dto.request.MaterialItemCreateRequest;
import io.swkoreatech.kosp.domain.material.dto.request.VisibilityUpdateRequest;
import io.swkoreatech.kosp.domain.material.dto.response.MaterialFolderResponse;
import io.swkoreatech.kosp.domain.material.dto.response.MaterialItemResponse;
import io.swkoreatech.kosp.global.security.annotation.AuthUser;
import jakarta.validation.Valid;

/**
 * 학습자료(과제/EL) 관리 API.
 *
 * <p>연도·학기·과목 폴더 계층, 폴더별 공개/비공개, 마이페이지 최신 자료 노출,
 * 첨부파일 페이지 진입(시작 폴더)을 제공한다.</p>
 */
@Tag(name = "Material", description = "학습자료(과제/EL) 관리 API")
public interface MaterialApi {

    // ── 폴더 ──────────────────────────────────────────────────────────

    @Operation(summary = "내 폴더 트리 조회", description = "로그인 사용자의 전체 폴더를 정렬 순서대로 반환합니다.")
    @GetMapping("/v1/users/me/material-folders")
    ResponseEntity<List<MaterialFolderResponse>> getMyFolders(
        @Parameter(hidden = true) @AuthUser User user
    );

    @Operation(summary = "시작 폴더 조회", description = "첨부파일 페이지 진입 시 기본 선택할 폴더를 반환합니다. 폴더가 없으면 본문 없음.")
    @GetMapping("/v1/users/me/material-folders/start")
    ResponseEntity<MaterialFolderResponse> getStartFolder(
        @Parameter(hidden = true) @AuthUser User user
    );

    @Operation(summary = "폴더 생성", description = "학습자료 폴더를 생성합니다. parentId 지정 시 하위 폴더로 생성됩니다.")
    @ApiResponse(responseCode = "201", description = "생성 성공")
    @PostMapping("/v1/users/me/material-folders")
    ResponseEntity<MaterialFolderResponse> createFolder(
        @Parameter(hidden = true) @AuthUser User user,
        @RequestBody @Valid MaterialFolderCreateRequest request
    );

    @Operation(summary = "폴더 수정", description = "본인 소유 폴더의 이름/유형/정렬을 수정합니다.")
    @PutMapping("/v1/users/me/material-folders/{folderId}")
    ResponseEntity<MaterialFolderResponse> updateFolder(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long folderId,
        @RequestBody @Valid MaterialFolderUpdateRequest request
    );

    @Operation(summary = "폴더 공개/비공개 변경", description = "폴더 단위 공개 범위를 변경합니다.")
    @PatchMapping("/v1/users/me/material-folders/{folderId}/visibility")
    ResponseEntity<MaterialFolderResponse> changeFolderVisibility(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long folderId,
        @RequestBody @Valid VisibilityUpdateRequest request
    );

    @Operation(summary = "시작 폴더 지정", description = "첨부파일 페이지 진입 기본 폴더로 지정합니다. 기존 시작 폴더는 해제됩니다.")
    @PatchMapping("/v1/users/me/material-folders/{folderId}/start")
    ResponseEntity<MaterialFolderResponse> setStartFolder(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long folderId
    );

    @Operation(summary = "폴더 삭제", description = "본인 소유의 빈 폴더를 삭제합니다. 자료가 남아있으면 400.")
    @ApiResponse(responseCode = "204", description = "삭제 성공")
    @DeleteMapping("/v1/users/me/material-folders/{folderId}")
    ResponseEntity<Void> deleteFolder(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long folderId
    );

    // ── 자료 아이템 ────────────────────────────────────────────────────

    @Operation(summary = "폴더 내 자료 조회", description = "특정 폴더의 자료를 최신순으로 반환합니다.")
    @GetMapping("/v1/users/me/material-folders/{folderId}/items")
    ResponseEntity<List<MaterialItemResponse>> getFolderItems(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long folderId
    );

    @Operation(summary = "최신 자료 조회 (마이페이지)", description = "로그인 사용자의 최신 자료를 limit 개수만큼 반환합니다.")
    @GetMapping("/v1/users/me/materials/recent")
    ResponseEntity<List<MaterialItemResponse>> getRecentItems(
        @Parameter(hidden = true) @AuthUser User user,
        @Parameter(description = "가져올 개수") @RequestParam(defaultValue = "5") int limit
    );

    @Operation(summary = "자료 등록", description = "폴더에 자료를 등록합니다. 파일은 presigned URL 업로드 후 fileUrl 을 전달합니다.")
    @ApiResponse(responseCode = "201", description = "등록 성공")
    @PostMapping("/v1/users/me/materials")
    ResponseEntity<MaterialItemResponse> createItem(
        @Parameter(hidden = true) @AuthUser User user,
        @RequestBody @Valid MaterialItemCreateRequest request
    );

    @Operation(summary = "자료 공개/비공개 변경", description = "자료 단위 공개 override 를 변경합니다.")
    @PatchMapping("/v1/users/me/materials/{itemId}/visibility")
    ResponseEntity<MaterialItemResponse> changeItemVisibility(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long itemId,
        @RequestBody @Valid VisibilityUpdateRequest request
    );

    @Operation(summary = "자료 삭제", description = "본인 소유 자료를 삭제합니다.")
    @ApiResponse(responseCode = "204", description = "삭제 성공")
    @DeleteMapping("/v1/users/me/materials/{itemId}")
    ResponseEntity<Void> deleteItem(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long itemId
    );

    // ── 타인 공개 조회 ─────────────────────────────────────────────────

    @Operation(summary = "공개 폴더 조회 (타인)", description = "특정 사용자의 공개 폴더 트리를 반환합니다.")
    @GetMapping("/v1/users/{userId}/material-folders")
    ResponseEntity<List<MaterialFolderResponse>> getPublicFolders(
        @PathVariable Long userId
    );

    @Operation(summary = "공개 폴더 자료 조회 (타인)", description = "특정 사용자의 공개 폴더 내 공개 자료를 반환합니다. 비공개면 404.")
    @GetMapping("/v1/users/{userId}/material-folders/{folderId}/items")
    ResponseEntity<List<MaterialItemResponse>> getPublicFolderItems(
        @PathVariable Long userId,
        @PathVariable Long folderId
    );
}
