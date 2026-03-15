package org.dachuang_team.dc_backend_services.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.dachuang_team.dc_backend_services.services.AuthService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

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
        String path = request.getRequestURI();

        // 检查是否为 permitAll() 的接口
        if (isPermitAllPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 验证 Token
        if (token != null && !token.isEmpty()) {
            authService.validateToken(token).ifPresent(session -> {
                List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + session.getUserRole())
                );

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(session.getUserId(), null, authorities);

                SecurityContextHolder.getContext().setAuthentication(authentication);
            });
        } else {
            // 未携带 Token 且非 permitAll() 接口，返回 401
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\": 401, \"message\": \"未携带有效的认证信息\", \"data\": null}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPermitAllPath(String path) {
        // 再次配置 permitAll() 的路径
        return  path.startsWith("/api/sysImg/get") ||
                path.startsWith("/api/users/register") ||
                path.startsWith("/api/users/login") ||
                path.startsWith("/api/admins/register") ||
                path.startsWith("/api/admins/login") ||
                path.startsWith("/api/products/approved") ||
                path.startsWith("/api/products/search");
    }
}