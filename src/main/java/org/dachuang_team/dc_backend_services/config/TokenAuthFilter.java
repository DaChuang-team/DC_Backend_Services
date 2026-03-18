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

@Component
public class TokenAuthFilter extends OncePerRequestFilter {

    private final AuthService authService;

    public TokenAuthFilter(AuthService authService) {
        this.authService = authService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String tokenHeader = request.getHeader("Authorization");
        String token = null;

        // 处理 Token 前缀
        if (tokenHeader != null) {
            if (tokenHeader.startsWith("Bearer ")) {
                // 截取 Bearer 后的纯 Token（从第7位开始，因为 "Bearer " 是7个字符：B(0),e(1),a(2),r(3),e(4),r(5), (空格6)）
                token = tokenHeader.substring(7).trim();
            } else {
                // 兼容不带前缀的场景
                token = tokenHeader.trim();
            }
        }

        logger.info("获取token: " + token);

        if (token != null && !token.isEmpty()) {
            authService.validateToken(token).ifPresent(session -> {
                // 将角色字符串转换为Spring Security识别的Authority
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + session.getUserRole());

                logger.info("转换token后得到的结果: " + authority);

                // 构建认证对象
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(session.getUserId(), null, Collections.singletonList(authority));

                logger.info("token认证对象: " + authentication);

                // 存入上下文
                SecurityContextHolder.getContext().setAuthentication(authentication);
            });
        }
        filterChain.doFilter(request, response);
    }
}