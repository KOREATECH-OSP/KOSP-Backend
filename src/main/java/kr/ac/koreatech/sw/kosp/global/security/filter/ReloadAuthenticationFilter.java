package kr.ac.koreatech.sw.kosp.global.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import kr.ac.koreatech.sw.kosp.domain.auth.service.UserRefreshManager;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class ReloadAuthenticationFilter extends OncePerRequestFilter {

    private final UserRefreshManager userRefreshManager;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated()) {
            String username = auth.getName();

            // Check local cache (Zero I/O)
            if (userRefreshManager.isDirty(username)) {
                reloadAuthentication(request, username, auth);
                userRefreshManager.clear(username);
            }
        }
        
        filterChain.doFilter(request, response);
    }

    private void reloadAuthentication(HttpServletRequest request, String username, Authentication currentAuth) {
        // 1. Load fresh details from DB
        UserDetails newPrincipal = userDetailsService.loadUserByUsername(username);

        // 2. Create new Authentication
        UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(
            newPrincipal,
            currentAuth.getCredentials(),
            newPrincipal.getAuthorities()
        );
        newAuth.setDetails(currentAuth.getDetails());

        // 3. Update Context
        SecurityContextHolder.getContext().setAuthentication(newAuth);
        
        // 4. Update Session to persist changes for subsequent requests (if needed)
        request.getSession().setAttribute(
            HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
            SecurityContextHolder.getContext()
        );
    }
}
