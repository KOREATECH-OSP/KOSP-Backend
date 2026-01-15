package kr.ac.koreatech.sw.kosp.domain.community.recruit.controller;

import java.net.URI;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import kr.ac.koreatech.sw.kosp.domain.community.board.model.Board;
import kr.ac.koreatech.sw.kosp.domain.community.board.service.BoardService;
import kr.ac.koreatech.sw.kosp.domain.community.recruit.api.RecruitApi;
import kr.ac.koreatech.sw.kosp.domain.community.recruit.dto.request.RecruitApplyDecisionRequest;
import kr.ac.koreatech.sw.kosp.domain.community.recruit.dto.request.RecruitRequest;
import kr.ac.koreatech.sw.kosp.domain.community.recruit.dto.request.RecruitStatusRequest;
import kr.ac.koreatech.sw.kosp.domain.community.recruit.dto.response.RecruitApplyListResponse;
import kr.ac.koreatech.sw.kosp.domain.community.recruit.dto.response.RecruitApplyResponse;
import kr.ac.koreatech.sw.kosp.domain.community.recruit.dto.response.RecruitListResponse;
import kr.ac.koreatech.sw.kosp.domain.community.recruit.dto.response.RecruitResponse;
import kr.ac.koreatech.sw.kosp.domain.community.recruit.service.RecruitApplyService;
import kr.ac.koreatech.sw.kosp.domain.community.recruit.service.RecruitService;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.global.security.annotation.AuthUser;
import kr.ac.koreatech.sw.kosp.global.security.annotation.Permit;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/community/recruits")
public class RecruitController implements RecruitApi {

    private final RecruitService recruitService;
    private final RecruitApplyService recruitApplyService;
    private final BoardService boardService;

    @Override
    @GetMapping
    @Permit(permitAll = true, description = "모집 공고 목록 조회")
    public ResponseEntity<RecruitListResponse> getList(
        @AuthUser User user,
        @RequestParam Long boardId,
        Pageable pageable
    ) {
        Board board = boardService.getBoard(boardId);
        RecruitListResponse response = recruitService.getList(board, pageable, user);
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/{id}")
    @Permit(permitAll = true, description = "모집 공고 상세 조회")
    public ResponseEntity<RecruitResponse> getOne(
        @AuthUser User user,
        @PathVariable Long id
    ) {
        RecruitResponse response = recruitService.getOne(id, user);
        return ResponseEntity.ok(response);
    }

    @Override
    @PostMapping
    @Permit(name = "recruit:create", description = "모집 공고 작성")
    public ResponseEntity<Void> create(
        @AuthUser User user,
        @RequestBody @Valid RecruitRequest request
    ) {
        Board board = boardService.getBoard(request.boardId());
        Long id = recruitService.create(user, board, request);
        return ResponseEntity.created(URI.create("/v1/community/recruits/" + id)).build();
    }

    @Override
    @PutMapping("/{id}")
    @Permit(name = "recruit:update", description = "모집 공고 수정")
    public ResponseEntity<Void> update(
        @AuthUser User user,
        @PathVariable Long id,
        @RequestBody @Valid RecruitRequest request
    ) {
        recruitService.update(user, id, request);
        return ResponseEntity.ok().build();
    }

    @Override
    @DeleteMapping("/{id}")
    @Permit(name = "recruit:delete", description = "모집 공고 삭제")
    public ResponseEntity<Void> delete(
        @AuthUser User user,
        @PathVariable Long id
    ) {
        recruitService.delete(user, id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PatchMapping("/{id}/status")
    @Permit(name = "recruit:status", description = "모집 상태 변경")
    public ResponseEntity<Void> updateStatus(
        @AuthUser User user,
        @PathVariable Long id,
        @RequestBody @Valid RecruitStatusRequest request
    ) {
        recruitService.updateStatus(user, id, request.status());
        return ResponseEntity.ok().build();
    }

    @Override
    @PostMapping("/{recruitId}/apply")
    @Permit(name = "community:recruits:apply", description = "공고 지원")
    public ResponseEntity<Void> applyRecruit(
        @AuthUser User user,
        @PathVariable Long recruitId,
        @RequestBody @Valid kr.ac.koreatech.sw.kosp.domain.community.recruit.dto.request.RecruitApplyRequest request
    ) {
        recruitApplyService.applyRecruit(recruitId, user, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    @GetMapping("/{recruitId}/applications")
    @Permit(name = "recruit:applications:list", description = "지원자 목록 조회")
    public ResponseEntity<RecruitApplyListResponse> getApplicants(
        @AuthUser User user,
        @PathVariable Long recruitId,
        Pageable pageable
    ) {
        RecruitApplyListResponse response = recruitApplyService.getApplicants(recruitId, user, pageable);
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/applications/{applicationId}")
    @Permit(name = "recruit:applications:read", description = "지원 상세 조회")
    public ResponseEntity<RecruitApplyResponse> getApplication(
        @AuthUser User user,
        @PathVariable Long applicationId
    ) {
        RecruitApplyResponse response = recruitApplyService.getApplication(applicationId, user);
        return ResponseEntity.ok(response);
    }

    @Override
    @PatchMapping("/applications/{applicationId}")
    @Permit(name = "recruit:applications:decide", description = "지원 수락/거절")
    public ResponseEntity<Void> decideApplication(
        @AuthUser User user,
        @PathVariable Long applicationId,
        @RequestBody @Valid RecruitApplyDecisionRequest request
    ) {
        recruitApplyService.decideApplication(applicationId, user, request);
        return ResponseEntity.ok().build();
    }
}

