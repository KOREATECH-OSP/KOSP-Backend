package kr.ac.koreatech.sw.kosp.infra.email.form;

import java.util.Map;

public interface EmailForm {

    Map<String, String> getContent();

    String getSubject();

    String getFilePath();
}
