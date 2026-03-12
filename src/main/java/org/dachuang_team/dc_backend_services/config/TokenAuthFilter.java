package org.dachuang_team.dc_backend_services.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.dachuang_team.dc_backend_services.services.AuthService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Component
public class TokenAuthFilter extends OncePerRequestFilter {
    private final AuthService authService;

    public TokenAuthFilter(AuthService authService) {
        this.authService = authService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = request.getHeader("Authorization");

        if (token != null && !token.isEmpty()) {
            authService.validateToken(token).ifPresent(session -> {
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(session.getUserId(), null, new ArrayList<>());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            });
        }

        filterChain.doFilter(request, response);
    }
}
