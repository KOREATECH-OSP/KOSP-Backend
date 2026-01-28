package io.swkoreatech.kosp.common.user;

import static lombok.AccessLevel.PROTECTED;

import io.swkoreatech.kosp.common.github.model.GithubUser;
import io.swkoreatech.kosp.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@MappedSuperclass
@NoArgsConstructor(access = PROTECTED)
@SuperBuilder
public abstract class BaseUser extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(max = 50)
    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @NotNull
    @Column(name = "kut_id", unique = true, nullable = false)
    private String kutId;

    @NotNull
    @Size(max = 255)
    @Column(name = "kut_email", unique = true, nullable = false)
    private String kutEmail;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    @OneToOne
    @JoinColumn(name = "github_id")
    private GithubUser githubUser;

    public boolean hasGithubUser() {
        return githubUser != null;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updateKutId(String kutId) {
        if (kutId != null) this.kutId = kutId;
    }

    public void updateKutEmail(String kutEmail) {
        if (kutEmail != null) this.kutEmail = kutEmail.toLowerCase();
    }

    public void updateGithubUser(GithubUser githubUser) {
        this.githubUser = githubUser;
    }

    public void delete() {
        this.isDeleted = true;
    }

    protected void markAsActive() {
        this.isDeleted = false;
    }
}
