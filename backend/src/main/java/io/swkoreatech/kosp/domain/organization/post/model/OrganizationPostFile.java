package io.swkoreatech.kosp.domain.organization.post.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationPostFile {

    @Column(name = "file_name", nullable = false, length = 300)
    private String fileName;

    @Column(name = "file_url", nullable = false, length = 500)
    private String fileUrl;
}
