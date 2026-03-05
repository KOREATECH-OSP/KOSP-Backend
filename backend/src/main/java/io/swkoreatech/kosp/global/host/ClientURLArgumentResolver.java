package io.swkoreatech.kosp.global.host;

import org.jspecify.annotations.NonNull;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import lombok.RequiredArgsConstructor;

/**
 * {@link ClientURL} 어노테이션이 붙은 컨트롤러 메서드 파라미터에 클라이언트 URL을 주입하는 리졸버.
 * <p>{@link ClientURLContext}로부터 클라이언트 URL 값을 가져와 반환한다.</p>
 */
@Component
@RequiredArgsConstructor
public class ClientURLArgumentResolver implements HandlerMethodArgumentResolver {

    private final ClientURLContext clientURLContext;

    /** {@inheritDoc} */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(ClientURL.class);
    }

    /** {@inheritDoc} */
    @Override
    public Object resolveArgument(
        @NonNull MethodParameter parameter,
        ModelAndViewContainer mavContainer,
        @NonNull NativeWebRequest webRequest,
        WebDataBinderFactory binderFactory
    ) {
        return clientURLContext.getClientURL();
    }
}

