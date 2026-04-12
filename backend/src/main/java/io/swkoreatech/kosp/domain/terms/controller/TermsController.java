package io.swkoreatech.kosp.domain.terms.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.terms.api.TermsApi;
import io.swkoreatech.kosp.domain.terms.dto.request.AgreeTermsRequest;
import io.swkoreatech.kosp.domain.terms.dto.response.TermsResponse;
import io.swkoreatech.kosp.domain.terms.service.TermsService;
import io.swkoreatech.kosp.global.security.annotation.Permit;
import lombok.RequiredArgsConstructor;

/**
 * 이용약관 컨트롤러.
 * <p>{@link TermsApi}를 구현하여 약관 조회 및 동의 기능을 제공한다.</p>
 */
@RestController
@RequiredArgsConstructor
public class TermsController implements TermsApi {

    private final TermsService termsService;

    /** {@inheritDoc} */
    @Override
    @Permit(permitAll = true, name = "terms:get", description = "현재 약관 조회")
    public ResponseEntity<TermsResponse> getActiveTerms() {
        return ResponseEntity.ok(termsService.getActiveTerms());
    }

    /** {@inheritDoc} */
    @Override
    @Permit(description = "약관 동의")
    public ResponseEntity<Void> agreeTerms(User user, AgreeTermsRequest request) {
        termsService.agreeTerms(user.getId(), request);
        return ResponseEntity.ok().build();
    }
}
