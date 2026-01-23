package io.swkoreatech.kosp.harvester.user;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

public interface UserRepository extends Repository<User, Long> {

    Optional<User> findById(Long id);

    default User getById(Long id) {
        return findById(id).orElseThrow(() -> new RuntimeException("User not found: " + id));
    }

    @Query("SELECT u.id FROM User u WHERE u.isDeleted = false AND u.githubUser IS NOT NULL")
    List<Long> findActiveUserIds();
}
