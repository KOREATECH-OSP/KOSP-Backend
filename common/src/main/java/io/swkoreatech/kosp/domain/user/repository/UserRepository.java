package io.swkoreatech.kosp.domain.user.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import io.swkoreatech.kosp.domain.user.model.User;
import io.swkoreatech.kosp.global.exception.ExceptionMessage;
import io.swkoreatech.kosp.global.exception.GlobalException;

public interface UserRepository extends PagingAndSortingRepository<User, Long>, JpaSpecificationExecutor<User> {

    User save(User user);
    
    Page<User> findAll(Pageable pageable);

    Optional<User> findById(Long id);

    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN FETCH u.roles r " +
           "LEFT JOIN FETCH r.policies p " +
           "LEFT JOIN FETCH p.permissions " +
           "WHERE u.id = :userId")
    Optional<User> findByIdWithRolesAndPermissions(@Param("userId") Long userId);

    Optional<User> findByKutEmail(String kutEmail);
    boolean existsByKutEmail(String kutEmail);
    boolean existsByKutId(String kutId);
    boolean existsByKutIdAndIsDeletedFalse(String kutId);
    boolean existsByKutIdAndIdNot(String kutId, Long id);
    boolean existsByKutEmailAndIdNot(String kutEmail, Long id);
    boolean existsByRoles_Name(String roleName);

    java.util.List<User> findByNameContaining(String keyword);

    Optional<User> findByGithubUser_GithubId(Long githubId);

    Optional<User> findByGithubUser_GithubIdAndIsDeletedFalse(Long githubId);
    Optional<User> findByGithubUser_GithubLogin(String githubLogin);

    void deleteById(Long id);

    default User getById(Long id) {
        return findById(id)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.USER_NOT_FOUND));
    }

    default User getByKutEmail(String kutEmail) {
        return findByKutEmail(kutEmail)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.USER_NOT_FOUND));
    }
}
