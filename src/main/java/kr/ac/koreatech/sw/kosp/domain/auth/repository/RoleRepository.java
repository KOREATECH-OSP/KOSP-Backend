package kr.ac.koreatech.sw.kosp.domain.auth.repository;

import java.util.List;
import java.util.Optional;
import kr.ac.koreatech.sw.kosp.domain.auth.model.Role;
import org.springframework.data.repository.Repository;

public interface RoleRepository extends Repository<Role, Long> {
    Role save(Role role);
    List<Role> findAll();
    Optional<Role> findByName(String name);
    boolean existsByName(String name);
    void deleteByName(String name);

    default Role getByName(String name) {
        return findByName(name)
            .orElseThrow(() -> new kr.ac.koreatech.sw.kosp.global.exception.GlobalException(
                kr.ac.koreatech.sw.kosp.global.exception.ExceptionMessage.NOT_FOUND));
    }
}
