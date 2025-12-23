package kr.ac.koreatech.sw.kosp.domain.community.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ArticleStats {

    @Column(nullable = false)
    private Integer views;

    @Embedded
    private ArticleCounts counts;
}
