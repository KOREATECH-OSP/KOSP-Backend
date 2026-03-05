package io.swkoreatech.kosp.global.host;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

/**
 * 요청 범위(Request Scope)에서 서버 URL을 저장하는 컨텍스트.
 * <p>{@link io.swkoreatech.kosp.global.host.ServerURLInterceptor}에 의해 설정되며,
 * {@link io.swkoreatech.kosp.global.host.ServerURLArgumentResolver}에 의해 컨트롤러에 주입된다.</p>
 */
@Component
@RequestScope
public class ServerURLContext {

    private String serverURL;

    /**
     * 현재 요청의 서버 URL을 반환한다.
     *
     * @return 서버 URL
     */
    public String getServerURL() {
        return serverURL;
    }

    /**
     * 현재 요청의 서버 URL을 설정한다.
     *
     * @param host 서버 URL
     */
    public void setServerURL(String host) {
        this.serverURL = host;
    }
}
