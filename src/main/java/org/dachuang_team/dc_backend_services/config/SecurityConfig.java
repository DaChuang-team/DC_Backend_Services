package org.dachuang_team.dc_backend_services.config;

import org.dachuang_team.dc_backend_services.services.AuthService; // 确保导入路径正确
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.Customizer;

@Configuration
public class SecurityConfig {

    @Autowired
    private AuthService authService; // 注入你刚创建的 AuthService

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        TokenAuthFilter tokenAuthFilter = new TokenAuthFilter(authService);

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 1. 公开接口：允许匿名访问（注册、登录等）
                        .requestMatchers("/api/users/register", "/api/users/login").permitAll()
                        .requestMatchers("/api/admins/register", "/api/admins/login").permitAll()

                        // 2. 受保护接口：必须携带合法 Token 才能访问
                        .requestMatchers("/api/users/updateInfo").authenticated()

                        // 3. 测试阶段接口：暂时放行
                        .requestMatchers("/api/users/all", "/api/users/delete").permitAll()
                        .requestMatchers("/api/admins/all", "/api/admins/updateUserStatus", "/api/admins/admindelete").permitAll()
                        .requestMatchers("/api/attractions/**", "/api/hotels/**", "/api/ai/**", "/api/images/**").permitAll()

                        // 4. 其他所有请求默认需要认证
                        .anyRequest().authenticated()
                )
                // 在用户名密码过滤器之前，先执行 Token 校验过滤器
                .addFilterBefore(tokenAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}