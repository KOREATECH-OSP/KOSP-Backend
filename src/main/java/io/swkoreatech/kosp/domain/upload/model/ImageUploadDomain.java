package io.swkoreatech.kosp.domain.upload.model;

import java.util.Arrays;

import io.swkoreatech.kosp.global.exception.ExceptionMessage;
import io.swkoreatech.kosp.global.exception.GlobalException;

public enum ImageUploadDomain {
    ARTICLES,
    PROFILES,
    TEAMS,
    RECRUITS,
    ADMIN,
    ;

    public static ImageUploadDomain from(String domain) {
        return Arrays.stream(values())
            .filter(it -> it.name().equalsIgnoreCase(domain))
            .findAny()
            .orElseThrow(() -> new GlobalException(ExceptionMessage.INVALID_PARAMETER));
    }
}
