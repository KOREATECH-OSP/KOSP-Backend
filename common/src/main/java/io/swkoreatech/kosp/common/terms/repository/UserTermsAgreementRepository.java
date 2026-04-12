package io.swkoreatech.kosp.common.terms.repository;

import org.springframework.data.repository.Repository;

import io.swkoreatech.kosp.common.terms.model.Terms;
import io.swkoreatech.kosp.common.terms.model.UserTermsAgreement;
import io.swkoreatech.kosp.common.user.model.User;

/**
 * {@link UserTermsAgreement} 엔티티 데이터 접근 레포지토리.
 */
public interface UserTermsAgreementRepository extends Repository<UserTermsAgreement, Long> {

    UserTermsAgreement save(UserTermsAgreement agreement);

    boolean existsByUserAndTerms(User user, Terms terms);
}
