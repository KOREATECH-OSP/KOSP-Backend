package io.swkoreatech.kosp.harvester.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findById(Long id);

    default User getById(Long id) {
        return findById(id).orElseThrow(() -> new RuntimeException("User not found: " + id));
    }

    @Query("SELECT u.id FROM User u WHERE u.isDeleted = false AND u.githubUser IS NOT NULL")
    List<Long> findActiveUserIds();
}
