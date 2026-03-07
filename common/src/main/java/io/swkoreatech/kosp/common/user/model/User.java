package io.swkoreatech.kosp.common.user.model;

import static lombok.AccessLevel.PROTECTED;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import io.swkoreatech.kosp.common.auth.model.Role;
import io.swkoreatech.kosp.common.github.model.GithubUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 사용자 엔티티.
 *
 * <p>시스템의 실제 사용자를 나타내며, Spring Security의 {@link UserDetails}를 구현한다.
 * 비밀번호, 자기소개, 포인트, 역할({@link Role}) 등의 정보를 관리한다.</p>
 */
@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = PROTECTED)
@ToString(exclude = {"password"})
public class User extends BaseUser implements UserDetails {

    @NotNull
    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "introduction")
    private String introduction;

    @NotNull
    @Column(name = "point", nullable = false)
    private Integer point = 0;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_role",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private final Set<Role> roles = new HashSet<>();

    @Builder
    private User(
        Long id,
        String name,
        String kutId,
        String kutEmail,
        GithubUser githubUser,
        String password,
        String introduction
    ) {
        super(id, name, kutId, kutEmail, githubUser);
        this.password = password;
        this.introduction = introduction;
    }

    /**
     * 사용자 정보를 수정한다. {@code null}인 항목은 변경하지 않는다.
     *
     * @param name         새로운 이름 ({@code null}이면 변경 없음)
     * @param introduction 새로운 자기소개 ({@code null}이면 변경 없음)
     */
    public void updateInfo(String name, String introduction) {
        if (name != null) {
            this.updateName(name);
        }
        if (introduction != null) {
            this.introduction = introduction;
        }
    }

    /**
     * 현재 저장된 평문 비밀번호를 인코딩하여 저장한다.
     *
     * @param passwordEncoder 비밀번호 인코더
     */
    public void encodePassword(PasswordEncoder passwordEncoder) {
        this.password = passwordEncoder.encode(password);
    }

    /**
     * 비밀번호를 새로운 값으로 변경한다.
     *
     * @param rawPassword     새로운 평문 비밀번호
     * @param passwordEncoder 비밀번호 인코더
     */
    public void changePassword(String rawPassword, PasswordEncoder passwordEncoder) {
        this.password = passwordEncoder.encode(rawPassword);
    }

    /**
     * 포인트를 추가한다.
     *
     * @param amount 추가할 포인트 금액
     */
    public void addPoint(Integer amount) {
        this.point = this.point + amount;
    }

    /**
     * 포인트를 차감한다.
     *
     * @param amount 차감할 포인트 금액
     */
    public void deductPoint(Integer amount) {
        this.point = this.point - amount;
    }

    /**
     * 삭제된 사용자를 재활성화한다.
     *
     * <p>삭제 플래그를 해제하고, 기존에 부여된 역할을 모두 제거한다.</p>
     */
    public void reactivate() {
        this.markAsActive();
        this.roles.clear();
    }

    // UserDetails Implementation

    @Setter
    @Transient
    private Collection<? extends GrantedAuthority> authorities;

    /** 사용자에게 부여된 권한 목록을 반환한다. */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    /** 사용자 식별에 사용되는 학교 이메일을 반환한다. */
    @Override
    public String getUsername() {
        return this.getKutEmail();
    }

    /** 계정 만료 여부를 반환한다. 항상 {@code true}를 반환한다. */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /** 계정 잠금 여부를 반환한다. 항상 {@code true}를 반환한다. */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /** 자격 증명 만료 여부를 반환한다. 항상 {@code true}를 반환한다. */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /** 계정 활성화 여부를 반환한다. 삭제되지 않은 사용자이면 {@code true}를 반환한다. */
    @Override
    public boolean isEnabled() {
        return !this.isDeleted();
    }
}
