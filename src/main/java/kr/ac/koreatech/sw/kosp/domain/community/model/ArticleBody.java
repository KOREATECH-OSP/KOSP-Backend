package kr.ac.koreatech.sw.kosp.domain.community.model;

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
public class ArticleBody {

    @Embedded
    private ArticleContent content;

    @Embedded
    private ArticleStats stats;
}
