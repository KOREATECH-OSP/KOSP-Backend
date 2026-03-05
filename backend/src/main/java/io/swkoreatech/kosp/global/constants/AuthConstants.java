package io.swkoreatech.kosp.global.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 인증 관련 상수 정의 클래스.
 * <p>OAuth2 인증 과정에서 사용되는 세션 및 사용자 속성 키를 정의한다.</p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AuthConstants {

    public static final String REDIRECT_URI_SESSION_ATTR = "REDIRECT_URI_SESSION_ATTRIBUTE";
    public static final String IS_REGISTERED_ATTR = "isRegistered";
    public static final String IS_REREGISTRATION_ATTR = "isReregistration";
    public static final String NEEDS_ADDITIONAL_INFO_ATTR = "needsAdditionalInfo";
    public static final String USER_ATTR = "user";

}
