package io.swkoreatech.kosp.domain.resume.export;

import io.swkoreatech.kosp.common.user.model.User;

/**
 * 이력서 머리에 들어가는 사용자 정보. {@code resume_data} 에는 없고 User 에서 온다.
 *
 * @param name      사용자 이름
 * @param email     이메일. 본인 다운로드에만 채운다 (약관 제11조 공개 항목에 이메일이 없고,
 *                  공개 다운로드는 비인증 호출이 가능하다)
 * @param githubUrl GitHub 프로필 주소
 */
public record ResumeProfile(String name, String email, String githubUrl) {

    public static final ResumeProfile EMPTY = new ResumeProfile("", "", "");

    /**
     * User 로부터 만든다.
     *
     * @param includeEmail 본인 다운로드일 때만 true
     */
    public static ResumeProfile of(User user, boolean includeEmail) {
        if (user == null) {
            return EMPTY;
        }
        String githubUrl = "";
        if (user.getGithubUser() != null && user.getGithubUser().getGithubLogin() != null
            && !user.getGithubUser().getGithubLogin().isBlank()) {
            githubUrl = "https://github.com/" + user.getGithubUser().getGithubLogin();
        }
        return new ResumeProfile(
            nullToEmpty(user.getName()),
            includeEmail ? nullToEmpty(user.getKutEmail()) : "",
            githubUrl);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
