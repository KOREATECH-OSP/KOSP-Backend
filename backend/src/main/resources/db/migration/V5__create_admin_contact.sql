-- admin_contact 테이블 생성
CREATE TABLE admin_contact (
    id BIGINT PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 기본 레코드 생성
INSERT INTO admin_contact (id, email) VALUES (1, 'contact@koreatech.ac.kr');
