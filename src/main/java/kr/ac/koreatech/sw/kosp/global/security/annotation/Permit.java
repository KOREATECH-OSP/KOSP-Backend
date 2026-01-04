package kr.ac.koreatech.sw.kosp.global.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
// We use PreAuthorize to hook into Spring Security, allowing permitAll() for public updates
// or isAuthenticated() for secured ones. Ideally we manage this via Aspect, 
// but to let 'permitAll' work for FilterSecurityInterceptor (if configured via annotations),
// we might need PreAuthorize("permitAll()") vs "isAuthenticated()".
// However, User requested AOP management.
// Since SecurityConfig is anyRequest().permitAll(), we can rely purely on Aspect.
// BUT, to be safe, we can keep using PreAuthorize("permitAll()") or similar if helpful.
// Given strict req: "manage via annotation", AOP is the way.
// We'll leave PreAuthorize out or set it to permitAll() effectively if we want AOP to do everything?
// Actually, if we use Aspect @Before, the method entry is intercepted.
// "SecurityConfig" allows all. So Aspect is the only guard.
public @interface Permit {
    String name() default "";
    String description() default "";
    boolean permitAll() default false;
}
