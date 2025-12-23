package kr.ac.koreatech.sw.kosp.domain.community.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ArticleCounts {

    @Column(nullable = false)
    private Integer likes;

    @Column(nullable = false)
    private Integer commentsCount;
}
