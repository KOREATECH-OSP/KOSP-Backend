package io.swkoreatech.kosp.common.user.model;

import static lombok.AccessLevel.PROTECTED;

import io.swkoreatech.kosp.common.github.model.GithubUser;
import io.swkoreatech.kosp.common.model.BaseEntity;
import jakarta.persistence.CascadeType;
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

/**
 * 사용자 기반 추상 엔티티.
 *
 * <p>모든 사용자 엔티티에 공통으로 필요한 이름, 학번(KUT ID), 이메일,
 * GitHub 연동 정보 및 삭제 상태를 관리한다.</p>
 */
@Getter
@MappedSuperclass
@NoArgsConstructor(access = PROTECTED)
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

    @OneToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "github_id")
    private GithubUser githubUser;

    /**
     * BaseUser를 생성한다.
     *
     * @param id         사용자 ID
     * @param name       사용자 이름
     * @param kutId      학번 (KUT ID)
     * @param kutEmail   학교 이메일
     * @param githubUser 연동된 GitHub 사용자 (없으면 {@code null})
     */
    protected BaseUser(Long id, String name, String kutId, String kutEmail, GithubUser githubUser) {
        this.id = id;
        this.name = name;
        this.kutId = kutId;
        this.kutEmail = kutEmail;
        this.githubUser = githubUser;
    }

    /**
     * GitHub 계정이 연동되어 있는지 확인한다.
     *
     * @return 연동되어 있으면 {@code true}
     */
    public boolean hasGithubUser() {
        return githubUser != null;
    }

    /**
     * 사용자 이름을 수정한다.
     *
     * @param name 새로운 이름
     */
    public void updateName(String name) {
        this.name = name;
    }

    /**
     * 학번(KUT ID)을 수정한다. {@code null}이면 변경하지 않는다.
     *
     * @param kutId 새로운 학번
     */
    public void updateKutId(String kutId) {
        if (kutId != null)
            this.kutId = kutId;
    }

    /**
     * 학교 이메일을 수정한다. {@code null}이면 변경하지 않으며, 소문자로 변환하여 저장한다.
     *
     * @param kutEmail 새로운 학교 이메일
     */
    public void updateKutEmail(String kutEmail) {
        if (kutEmail != null)
            this.kutEmail = kutEmail.toLowerCase();
    }

    /**
     * 연동된 GitHub 사용자를 변경한다.
     *
     * @param githubUser 연동할 GitHub 사용자
     */
    public void updateGithubUser(GithubUser githubUser) {
        this.githubUser = githubUser;
    }

    /**
     * 사용자를 삭제 상태로 변경한다 (소프트 삭제).
     */
    public void delete() {
        this.isDeleted = true;
    }

    /**
     * 사용자를 활성 상태로 변경한다.
     */
    protected void markAsActive() {
        this.isDeleted = false;
    }
}
