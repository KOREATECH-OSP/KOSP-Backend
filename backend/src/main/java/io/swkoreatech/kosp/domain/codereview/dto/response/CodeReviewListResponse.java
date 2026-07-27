package io.swkoreatech.kosp.domain.codereview.dto.response;

import java.util.List;

public record CodeReviewListResponse(
    long total,
    List<CodeReviewResponse> reviews
) {}
