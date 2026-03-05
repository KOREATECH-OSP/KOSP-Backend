package io.swkoreatech.kosp.infra.email.form;

import java.util.Map;

/**
 * 이메일 양식 인터페이스.
 *
 * <p>이메일 발송에 필요한 콘텐츠, 제목, 템플릿 경로를 정의한다.
 * 각 이메일 유형(인증, 비밀번호 초기화, 팀 초대 등)은 이 인터페이스를 구현한다.
 */
public interface EmailForm {

    /**
     * 이메일 템플릿에 바인딩할 콘텐츠를 반환한다.
     *
     * @return 키-값 쌍의 템플릿 변수 맵
     */
    Map<String, String> getContent();

    /**
     * 이메일 제목을 반환한다.
     *
     * @return 이메일 제목 문자열
     */
    String getSubject();

    /**
     * Thymeleaf 템플릿 파일 경로를 반환한다.
     *
     * @return 템플릿 파일명 (확장자 제외)
     */
    String getFilePath();
}
