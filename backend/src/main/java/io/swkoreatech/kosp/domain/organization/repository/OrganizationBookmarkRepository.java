package io.swkoreatech.kosp.domain.organization.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import io.swkoreatech.kosp.common.organization.model.Organization;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.organization.model.OrganizationBookmark;

public interface OrganizationBookmarkRepository extends Repository<OrganizationBookmark, Long> {

    OrganizationBookmark save(OrganizationBookmark bookmark);

    void delete(OrganizationBookmark bookmark);

    Optional<OrganizationBookmark> findByUserAndOrganization(User user, Organization organization);

    boolean existsByUserAndOrganization(User user, Organization organization);

    @Query("SELECT b.organization FROM OrganizationBookmark b WHERE b.user.id = :userId")
    List<Organization> findOrganizationsByUserId(@Param("userId") Long userId);
}
