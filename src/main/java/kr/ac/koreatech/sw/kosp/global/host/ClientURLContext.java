package kr.ac.koreatech.sw.kosp.global.host;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
public class ClientURLContext {

    private String clientURL;

    public String getClientURL() {
        return clientURL;
    }

    public void setClientURL(String clientURL) {
        this.clientURL = clientURL;
    }
}
