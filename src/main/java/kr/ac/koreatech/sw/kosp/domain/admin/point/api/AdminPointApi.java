package kr.ac.koreatech.sw.kosp.domain.admin.point.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.ac.koreatech.sw.kosp.domain.admin.point.dto.request.PointTransactionRequest;
import kr.ac.koreatech.sw.kosp.domain.admin.point.dto.response.PointHistoryResponse;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.global.security.annotation.AuthUser;

@Tag(name = "Admin - Point", description = "관리자 전용 포인트 관리 API")
@RequestMapping("/v1/admin/points")
public interface AdminPointApi {

    @Operation(
        summary = "포인트 변경",
        description = "관리자 권한으로 사용자의 포인트를 변경합니다. 양수는 지급, 음수는 차감입니다."
    )
    @ApiResponse(responseCode = "200", description = "변경 성공")
    @PostMapping("/users/{userId}")
    ResponseEntity<Void> changePoint(
        @Parameter(description = "사용자 ID") @PathVariable Long userId,
        @RequestBody @Valid PointTransactionRequest request,
        @Parameter(hidden = true) @AuthUser User admin
    );

    @Operation(summary = "포인트 내역 조회", description = "관리자 권한으로 사용자의 포인트 거래 내역을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/users/{userId}/history")
    ResponseEntity<PointHistoryResponse> getPointHistory(
        @Parameter(description = "사용자 ID") @PathVariable Long userId,
        @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size
    );
}
