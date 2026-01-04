package kr.ac.koreatech.sw.kosp.global.auth.core;

public interface AuthToken<T> {
    boolean validate();
    T getData();
}
