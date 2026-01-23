package io.swkoreatech.kosp.global.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AuthConstants {

    public static final String REDIRECT_URI_SESSION_ATTR = "REDIRECT_URI_SESSION_ATTRIBUTE";
    public static final String IS_REGISTERED_ATTR = "isRegistered";
    public static final String IS_REREGISTRATION_ATTR = "isReregistration";
    public static final String NEEDS_ADDITIONAL_INFO_ATTR = "needsAdditionalInfo";
    public static final String USER_ATTR = "user";

}
