package io.swkoreatech.kosp.global.host;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

/**
 * 요청 범위(Request Scope)에서 클라이언트 URL을 저장하는 컨텍스트.
 * <p>{@link io.swkoreatech.kosp.global.host.ClientURLInterceptor}에 의해 설정되며,
 * {@link io.swkoreatech.kosp.global.host.ClientURLArgumentResolver}에 의해 컨트롤러에 주입된다.</p>
 */
@Component
@RequestScope
public class ClientURLContext {

    private String clientURL;

    /**
     * 현재 요청의 클라이언트 URL을 반환한다.
     *
     * @return 클라이언트 URL
     */
    public String getClientURL() {
        return clientURL;
    }

    /**
     * 현재 요청의 클라이언트 URL을 설정한다.
     *
     * @param clientURL 클라이언트 URL
     */
    public void setClientURL(String clientURL) {
        this.clientURL = clientURL;
    }
}
