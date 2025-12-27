package kr.ac.koreatech.sw.kosp.domain.community.recruit.controller;

import jakarta.validation.Valid;
import java.net.URI;
import kr.ac.koreatech.sw.kosp.domain.community.recruit.api.RecruitApi;
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

import kr.ac.koreatech.sw.kosp.global.security.annotation.Permit;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/community/recruits")
public class RecruitController implements RecruitApi {

    private final RecruitService recruitService;

    @Override
    @GetMapping
    @Permit(permitAll = true, description = "모집 공고 목록 조회")
    public ResponseEntity<RecruitListResponse> getList(
        @RequestParam Long boardId,
        Pageable pageable
    ) {
        RecruitListResponse response = recruitService.getList(boardId, pageable);
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/{id}")
    @Permit(permitAll = true, description = "모집 공고 상세 조회")
    public ResponseEntity<RecruitResponse> getOne(@PathVariable Long id) {
        RecruitResponse response = recruitService.getOne(id);
        return ResponseEntity.ok(response);
    }

    @Override
    @PostMapping
    @Permit(name = "recruit:create", description = "모집 공고 작성")
    public ResponseEntity<Void> create(@RequestBody @Valid RecruitRequest request) {
        Long userId = 1L; // TODO: user support
        Long id = recruitService.create(userId, request);
        return ResponseEntity.created(URI.create("/v1/community/recruits/" + id)).build();
    }

    @Override
    @PutMapping("/{id}")
    @Permit(name = "recruit:update", description = "모집 공고 수정")
    public ResponseEntity<Void> update(
        @PathVariable Long id,
        @RequestBody @Valid RecruitRequest request
    ) {
        Long userId = 1L; // TODO: user support
        recruitService.update(userId, id, request);
        return ResponseEntity.ok().build();
    }

    @Override
    @DeleteMapping("/{id}")
    @Permit(name = "recruit:delete", description = "모집 공고 삭제")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Long userId = 1L; // TODO: user support
        recruitService.delete(userId, id);
        return ResponseEntity.noContent().build();
    }
}
