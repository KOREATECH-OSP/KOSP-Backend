package kr.ac.koreatech.sw.kosp.domain.community.recruit.controller;

import jakarta.validation.Valid;
import java.net.URI;
import kr.ac.koreatech.sw.kosp.domain.community.recruit.api.RecruitApi;
import kr.ac.koreatech.sw.kosp.domain.community.board.model.Board;
import kr.ac.koreatech.sw.kosp.domain.community.board.service.BoardService;
import kr.ac.koreatech.sw.kosp.domain.community.recruit.dto.response.RecruitListResponse;
import kr.ac.koreatech.sw.kosp.domain.community.recruit.dto.request.RecruitRequest;
import kr.ac.koreatech.sw.kosp.domain.community.recruit.dto.response.RecruitResponse;
import kr.ac.koreatech.sw.kosp.domain.community.recruit.service.RecruitService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.global.security.annotation.AuthUser;
import kr.ac.koreatech.sw.kosp.global.security.annotation.Permit;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/community/recruits")
public class RecruitController implements RecruitApi {

    private final RecruitService recruitService;
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
}
