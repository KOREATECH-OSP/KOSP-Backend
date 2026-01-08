package kr.ac.koreatech.sw.kosp.global.auth.resolver;

import java.util.Map;
import java.util.Objects;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.util.ContentCachingRequestWrapper;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import kr.ac.koreatech.sw.kosp.global.auth.annotation.Token;
import kr.ac.koreatech.sw.kosp.global.auth.exception.InvalidTokenException;
import kr.ac.koreatech.sw.kosp.global.auth.token.JwtToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @Token 어노테이션 처리
 * Request Body에서 토큰 추출 및 검증
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtArgumentResolver implements HandlerMethodArgumentResolver {

    private final ObjectMapper objectMapper;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(Token.class)
            && JwtToken.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object resolveArgument(
        MethodParameter parameter,
        ModelAndViewContainer mavContainer,
        NativeWebRequest webRequest,
        WebDataBinderFactory binderFactory
    ) {
        
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        
        Token annotation = parameter.getParameterAnnotation(Token.class);
        Class<? extends JwtToken> tokenClass = (Class<? extends JwtToken>) parameter.getParameterType();
        
        // 1. ✅ 필드명 결정 (파라미터 변수명에서 자동 추론)
        String fieldName = Objects.requireNonNull(annotation).value();
        if (fieldName.isBlank()) {
            fieldName = parameter.getParameterName();
        }
        
        // 2. ✅ Request Body에서 토큰 추출
        String tokenString = extractFromBody(request, fieldName);
        
        if (tokenString.isBlank()) {
            throw new InvalidTokenException("Token not found in request body: " + fieldName);
        }
        
        // 3. ✅ String → Token 객체 변환 (JwtToken.from()이 검증 수행)
        return JwtToken.from(tokenClass, tokenString);
    }
    
    /**
     * ✅ Request Body에서 토큰 추출
     */
    private String extractFromBody(HttpServletRequest request, String fieldName) {
        try {
            ContentCachingRequestWrapper wrapper = new ContentCachingRequestWrapper(request);
            byte[] content = wrapper.getContentAsByteArray();

            if (content.length == 0) {
                return "";
            }
            
            var body = objectMapper.readValue(content, Map.class);
            Object token = body.get(fieldName);
            
            return token != null ? token.toString() : "";
        } catch (Exception e) {
            log.debug("Failed to extract token from body: {}", e.getMessage());
        }
        return "";
    }
}
