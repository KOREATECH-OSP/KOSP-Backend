package io.swkoreatech.kosp.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserBasicInfoResponse {
    private User user;
    
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class User {
        private String login;
        private String name;
        private String createdAt;
        private String updatedAt;
    }
}
