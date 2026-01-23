package io.swkoreatech.kosp.infra.email.form;

import java.util.Map;

public interface EmailForm {

    Map<String, String> getContent();

    String getSubject();

    String getFilePath();
}
