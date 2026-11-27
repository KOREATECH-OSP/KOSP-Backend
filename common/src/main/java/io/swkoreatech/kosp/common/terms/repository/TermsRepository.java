package io.swkoreatech.kosp.common.terms.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.common.terms.model.Terms;
import io.swkoreatech.kosp.common.terms.model.TermsType;

/**
 * {@link Terms} 엔티티 데이터 접근 레포지토리.
 *
 * <p>활성 약관은 종류({@link TermsType})마다 하나씩 존재할 수 있으므로,
 * 종류를 지정하지 않고 활성 약관 "하나"를 조회하는 메서드는 두지 않는다.</p>
 */
public interface TermsRepository extends Repository<Terms, Long> {

    List<Terms> findAllByIsActiveTrue();

    Optional<Terms> findByTypeAndIsActiveTrue(TermsType type);

    Optional<Terms> findByTypeAndVersion(TermsType type, String version);

    /**
     * 종류와 무관하게 버전 문자열로 조회한다. 여러 종류가 같은 버전 문자열을 가질 수 있어 목록을 반환한다.
     */
    List<Terms> findByVersion(String version);

    /**
     * 현재 유효한 서비스 이용약관(SERVICE)을 조회한다.
     */
    default Terms getActive() {
        return findByTypeAndIsActiveTrue(TermsType.SERVICE)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.TERMS_NOT_FOUND));
    }

    /**
     * 버전 문자열로 약관 1건을 조회한다 (하위 호환용).
     *
     * @throws GlobalException 해당 버전이 없거나, 여러 종류에 같은 버전이 있어 특정할 수 없는 경우
     */
    default Terms getByVersion(String version) {
        List<Terms> matched = findByVersion(version);
        if (matched.isEmpty()) {
            throw new GlobalException(ExceptionMessage.TERMS_VERSION_NOT_FOUND);
        }
        if (matched.size() > 1) {
            throw new GlobalException(ExceptionMessage.TERMS_VERSION_AMBIGUOUS);
        }
        return matched.get(0);
    }
}
