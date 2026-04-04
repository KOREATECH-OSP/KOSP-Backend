package io.swkoreatech.kosp.common.user.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.common.user.model.User;

/**
 * {@link User} 엔티티의 데이터 접근 리포지토리.
 *
 * <p>사용자 저장, 조회, 검색 및 페이징 기능을 제공한다.
 * 동적 쿼리를 위한 {@link JpaSpecificationExecutor}를 함께 상속한다.</p>
 */
public interface UserRepository extends PagingAndSortingRepository<User, Long>, JpaSpecificationExecutor<User> {

    /**
     * 사용자를 저장한다.
     *
     * @param user 저장할 사용자
     * @return 저장된 사용자
     */
    User save(User user);

    /**
     * 모든 사용자를 페이징하여 조회한다.
     *
     * @param pageable 페이징 정보
     * @return 사용자 페이지
     */
    Page<User> findAll(Pageable pageable);

    /**
     * ID로 사용자를 조회한다.
     *
     * @param id 사용자 ID
     * @return 사용자 (존재하지 않으면 빈 {@link Optional})
     */
    Optional<User> findById(Long id);

    /**
     * 사용자를 역할, 정책, 권한 정보와 함께 조회한다.
     *
     * @param userId 사용자 ID
     * @return 역할과 권한이 페치 조인된 사용자
     */
    @Query("SELECT DISTINCT u FROM User u " +
        "LEFT JOIN FETCH u.roles r " +
        "LEFT JOIN FETCH r.policies p " +
        "LEFT JOIN FETCH p.permissions " +
        "WHERE u.id = :userId"
    )
    Optional<User> findByIdWithRolesAndPermissions(@Param("userId") Long userId);

    /**
     * 학교 이메일로 사용자를 조회한다.
     *
     * @param kutEmail 학교 이메일
     * @return 사용자 (존재하지 않으면 빈 {@link Optional})
     */
    Optional<User> findByKutEmail(String kutEmail);

    /**
     * 특정 학교 이메일의 사용자 존재 여부를 확인한다.
     *
     * @param kutEmail 학교 이메일
     * @return 존재하면 {@code true}
     */
    boolean existsByKutEmail(String kutEmail);

    /**
     * 특정 학번의 사용자 존재 여부를 확인한다.
     *
     * @param kutId 학번
     * @return 존재하면 {@code true}
     */
    boolean existsByKutId(String kutId);

    /**
     * 특정 학번의 활성 사용자 존재 여부를 확인한다.
     *
     * @param kutId 학번
     * @return 삭제되지 않은 사용자가 존재하면 {@code true}
     */
    boolean existsByKutIdAndIsDeletedFalse(String kutId);

    /**
     * 특정 학번을 가진 다른 사용자의 존재 여부를 확인한다.
     *
     * @param kutId 학번
     * @param id    제외할 사용자 ID
     * @return 존재하면 {@code true}
     */
    boolean existsByKutIdAndIdNot(String kutId, Long id);

    /**
     * 특정 이메일을 가진 다른 사용자의 존재 여부를 확인한다.
     *
     * @param kutEmail 학교 이메일
     * @param id       제외할 사용자 ID
     * @return 존재하면 {@code true}
     */
    boolean existsByKutEmailAndIdNot(String kutEmail, Long id);

    /**
     * 특정 역할 이름을 가진 사용자의 존재 여부를 확인한다.
     *
     * @param roleName 역할 이름
     * @return 존재하면 {@code true}
     */
    boolean existsByRoles_Name(String roleName);

    /**
     * 이름에 키워드를 포함하는 사용자 목록을 조회한다.
     *
     * @param keyword 검색 키워드
     * @return 키워드를 포함하는 사용자 목록
     */
    java.util.List<User> findByNameContaining(String keyword);

    /**
     * GitHub ID로 사용자를 조회한다.
     *
     * @param githubId GitHub 사용자 ID
     * @return 사용자 (존재하지 않으면 빈 {@link Optional})
     */
    Optional<User> findByGithubUser_GithubId(Long githubId);

    /**
     * GitHub ID로 활성 사용자를 조회한다.
     *
     * @param githubId GitHub 사용자 ID
     * @return 삭제되지 않은 사용자 (존재하지 않으면 빈 {@link Optional})
     */
    Optional<User> findByGithubUser_GithubIdAndIsDeletedFalse(Long githubId);

    /**
     * GitHub 로그인 ID로 사용자를 조회한다.
     *
     * @param githubLogin GitHub 로그인 ID
     * @return 사용자 (존재하지 않으면 빈 {@link Optional})
     */
    Optional<User> findByGithubUser_GithubLogin(String githubLogin);

    /**
     * GitHub 계정이 연동된 활성 사용자의 ID 목록을 조회한다.
     *
     * @return 활성 사용자 ID 목록
     */
    @Query("SELECT u.id FROM User u WHERE u.isDeleted = false AND u.githubUser IS NOT NULL")
    java.util.List<Long> findActiveUserIds();

    /**
     * 탈퇴하지 않은 모든 유저를 조회한다.
     * 칭호 평가 배치에서 전체 유저 순회 시 사용한다.
     *
     * @return 활성 유저 목록
     */
    java.util.List<User> findAllByIsDeletedFalse();

    /**
     * ID로 사용자를 삭제한다.
     *
     * @param id 삭제할 사용자 ID
     */
    void deleteById(Long id);

    /**
     * ID로 사용자를 조회하며, 존재하지 않으면 예외를 발생시킨다.
     *
     * @param id 사용자 ID
     * @return 조회된 사용자
     * @throws GlobalException 사용자가 존재하지 않는 경우
     */
    default User getById(Long id) {
        return findById(id)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.USER_NOT_FOUND));
    }

    /**
     * 학교 이메일로 사용자를 조회하며, 존재하지 않으면 예외를 발생시킨다.
     *
     * @param kutEmail 학교 이메일
     * @return 조회된 사용자
     * @throws GlobalException 사용자가 존재하지 않는 경우
     */
    default User getByKutEmail(String kutEmail) {
        return findByKutEmail(kutEmail)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.USER_NOT_FOUND));
    }
}
