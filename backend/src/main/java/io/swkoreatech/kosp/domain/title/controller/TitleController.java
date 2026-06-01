package io.swkoreatech.kosp.domain.title.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.title.api.TitleApi;
import io.swkoreatech.kosp.domain.title.dto.response.TitleListResponse;
import io.swkoreatech.kosp.domain.title.dto.response.UserTitleListResponse;
import io.swkoreatech.kosp.domain.title.dto.response.UserTitleResponse;
import io.swkoreatech.kosp.domain.title.service.TitleService;
import lombok.RequiredArgsConstructor;

/**
 * 칭호 유저 컨트롤러.
 * <p>{@link TitleApi}를 구현하여 칭호 조회 및 대표 칭호 설정 기능을 제공한다.</p>
 */
@RestController
@RequiredArgsConstructor
public class TitleController implements TitleApi {

    private final TitleService titleService;

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<UserTitleListResponse> getMyTitles(User user) {
        return ResponseEntity.ok(titleService.getMyTitles(user));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<UserTitleListResponse> getUserTitles(Long userId) {
        return ResponseEntity.ok(titleService.getUserTitles(userId));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<UserTitleResponse> setDisplayTitle(Long userTitleId, User user) {
        return ResponseEntity.ok(titleService.setDisplayTitle(userTitleId, user));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<TitleListResponse> getAllTitles() {
        return ResponseEntity.ok(titleService.getAllTitles());
    }
}
