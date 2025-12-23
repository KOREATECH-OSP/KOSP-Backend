package kr.ac.koreatech.sw.kosp.domain.community.controller;

import jakarta.validation.Valid;
import java.net.URI;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.domain.community.api.CommunityApi;
import kr.ac.koreatech.sw.kosp.domain.community.dto.request.ArticleCreateRequest;
import kr.ac.koreatech.sw.kosp.domain.community.dto.request.ArticleUpdateRequest;
import kr.ac.koreatech.sw.kosp.domain.community.dto.response.ArticleListResponse;
import kr.ac.koreatech.sw.kosp.domain.community.dto.response.ArticleResponse;
import kr.ac.koreatech.sw.kosp.domain.community.service.ArticleService;
import kr.ac.koreatech.sw.kosp.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/communities")
@RequiredArgsConstructor
public class ArticleController implements CommunityApi {

    private final ArticleService articleService;

    @Override
    @GetMapping
    public ResponseEntity<Page<ArticleListResponse>> getArticles(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int limit,
        @RequestParam(required = false) String category,
        @RequestParam(defaultValue = "latest") String sort
    ) {
        Sort sortObj = Sort.by(Sort.Direction.DESC, "createdAt");
        if ("popular".equals(sort)) {
            sortObj = Sort.by(Sort.Direction.DESC, "views", "createdAt");
        }

        PageRequest pageRequest = PageRequest.of(page - 1, limit, sortObj);
        return ResponseEntity.ok(articleService.getList(category, pageRequest));
    }

    @Override
    @GetMapping("/{articleId}")
    public ResponseEntity<ArticleResponse> getArticle(@PathVariable Long articleId) {
        return ResponseEntity.ok(articleService.get(articleId));
    }

    @Override
    @PostMapping
    public ResponseEntity<Void> createArticle(
        @AuthenticationPrincipal User user,
        @RequestBody @Valid ArticleCreateRequest request
    ) {
        validateAuthentication(user);
        Long articleId = articleService.create(user.getId(), request);
        return ResponseEntity.created(URI.create("/communities/" + articleId)).build();
    }

    @Override
    @PutMapping("/{articleId}")
    public ResponseEntity<Void> updateArticle(
        @AuthenticationPrincipal User user,
        @PathVariable Long articleId,
        @RequestBody @Valid ArticleUpdateRequest request
    ) {
        validateAuthentication(user);
        articleService.modify(user.getId(), articleId, request);
        return ResponseEntity.ok().build();
    }

    @Override
    @DeleteMapping("/{articleId}")
    public ResponseEntity<Void> deleteArticle(
        @AuthenticationPrincipal User user,
        @PathVariable Long articleId
    ) {
        validateAuthentication(user);
        articleService.delete(user.getId(), articleId);
        return ResponseEntity.ok().build();
    }

    private void validateAuthentication(User user) {
        if (user == null) {
            throw new GlobalException("User not authenticated", HttpStatus.UNAUTHORIZED);
        }
    }
}
