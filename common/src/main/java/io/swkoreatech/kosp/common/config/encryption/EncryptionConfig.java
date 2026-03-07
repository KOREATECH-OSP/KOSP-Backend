package io.swkoreatech.kosp.common.config.encryption;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;

/**
 * 텍스트 암호화 설정 클래스.
 *
 * <p>Spring Security의 {@link TextEncryptor}를 빈으로 등록하여
 * 애플리케이션 전반에서 텍스트 암호화/복호화 기능을 제공한다.</p>
 */
@Configuration
public class EncryptionConfig {

    @Value("${encryption.password:default-password}")
    private String password;

    @Value("${encryption.salt:1234abcd1234abcd}")
    private String salt;

    /**
     * 텍스트 암호화기 빈을 생성한다.
     *
     * @return 설정된 비밀번호와 솔트를 사용하는 {@link TextEncryptor} 인스턴스
     */
    @Bean
    public TextEncryptor textEncryptor() {
        return Encryptors.text(password, salt);
    }
}
