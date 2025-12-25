package kr.ac.koreatech.sw.kosp.domain.community.article.controller;

import jakarta.validation.Valid;
import java.net.URI;
import kr.ac.koreatech.sw.kosp.domain.community.article.api.ArticleApi;
import kr.ac.koreatech.sw.kosp.domain.community.article.dto.response.ArticleListResponse;
import kr.ac.koreatech.sw.kosp.domain.community.article.dto.request.ArticleRequest;
import kr.ac.koreatech.sw.kosp.domain.community.article.dto.response.ArticleResponse;
import kr.ac.koreatech.sw.kosp.domain.community.article.service.ArticleService;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/community/articles")
public class ArticleController implements ArticleApi {

    private final ArticleService articleService;

    @Override
    @GetMapping
    public ResponseEntity<ArticleListResponse> getList(
        @RequestParam Long boardId,
        Pageable pageable
    ) {
        ArticleListResponse response = articleService.getList(boardId, pageable);
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<ArticleResponse> getOne(@PathVariable Long id) {
        ArticleResponse response = articleService.getOne(id);
        return ResponseEntity.ok(response);
    }

    @Override
    @PostMapping
    public ResponseEntity<Void> create(@RequestBody @Valid ArticleRequest request) {
        Integer userId = 1; // TODO: user support
        Long id = articleService.create(userId, request);
        return ResponseEntity.created(URI.create("/v1/community/articles/" + id)).build();
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<Void> update(
        @PathVariable Long id,
        @RequestBody @Valid ArticleRequest request
    ) {
        Integer userId = 1; // TODO: user support
        articleService.update(userId, id, request);
        return ResponseEntity.ok().build();
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Integer userId = 1; // TODO: user support
        articleService.delete(userId, id);
        return ResponseEntity.noContent().build();
    }
}
