package io.swkoreatech.kosp.domain.user.model;

import static lombok.AccessLevel.PROTECTED;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import io.swkoreatech.kosp.common.user.BaseUser;
import io.swkoreatech.kosp.domain.auth.model.Role;
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
import lombok.experimental.SuperBuilder;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = PROTECTED)
@ToString(exclude = {"password"})
@SuperBuilder
public class User extends BaseUser implements UserDetails {

    @NotNull
    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "introduction")
    private String introduction;

    @Builder.Default
    @NotNull
    @Column(name = "point", nullable = false)
    private Integer point = 0;

    @Builder.Default
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_role",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    public void updateInfo(String name, String introduction) {
        if (name != null) this.updateName(name);
        if (introduction != null) this.introduction = introduction;
    }

    public void encodePassword(PasswordEncoder passwordEncoder) {
        this.password = passwordEncoder.encode(password);
    }

    public void changePassword(String rawPassword, PasswordEncoder passwordEncoder) {
        this.password = passwordEncoder.encode(rawPassword);
    }

    public void addPoint(Integer amount) {
        this.point = this.point + amount;
    }

    public void deductPoint(Integer amount) {
        this.point = this.point - amount;
    }

    public void reactivate() {
        this.markAsActive();
        this.roles.clear();
    }

    // UserDetails Implementation

    @Setter
    @Transient
    private Collection<? extends GrantedAuthority> authorities;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public String getUsername() {
        return this.getKutEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return !this.isDeleted();
    }
}
