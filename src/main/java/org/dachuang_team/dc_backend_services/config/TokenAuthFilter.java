package org.dachuang_team.dc_backend_services.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.dachuang_team.dc_backend_services.services.AuthService;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class TokenAuthFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthService authService;

    public TokenAuthFilter(AuthService authService) {
        this.authService = authService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain)
            throws ServletException, IOException {

        String token = extractToken(request.getHeader("Authorization"));

        logger.info("获取token: " + token);

        if (token != null && !token.isEmpty()) {
            authService.validateToken(token).ifPresent(session -> {
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + session.getUserRole());

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(session.getUserId(), null, Collections.singletonList(authority));

                logger.info("转换token后得到的结果: " + authority);
                logger.info("token认证对象: " + authentication);

                SecurityContextHolder.getContext().setAuthentication(authentication);
            });
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(String authorizationHeader) {
        if (authorizationHeader == null) {
            return null;
        }

        String value = authorizationHeader.trim();
        if (value.isEmpty()) {
            return null;
        }

        if (value.startsWith(BEARER_PREFIX)) {
            String token = value.substring(BEARER_PREFIX.length()).trim();
            return token.isEmpty() ? null : token;
        }

        return value;
    }
}