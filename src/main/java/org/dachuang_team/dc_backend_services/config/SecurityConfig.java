package org.dachuang_team.dc_backend_services.config;

import org.dachuang_team.dc_backend_services.services.AuthService; // 确保导入路径正确
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.Customizer;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private AuthService authService;

    @Autowired
    private RestAccessDeniedHandler restAccessDeniedHandler;

    @Autowired
    private RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // 实例化过滤器
        TokenAuthFilter tokenAuthFilter = new TokenAuthFilter(authService);

        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 公开接口
                        .requestMatchers("/api/users/register", "/api/users/login").permitAll()
                        .requestMatchers("/api/admins/register", "/api/admins/login").permitAll()
                        .requestMatchers("/api/products/approved", "/api/products/search").permitAll()
                        .requestMatchers("/api/sysImg/get").permitAll()

                        // 受保护接口
                        .requestMatchers("/api/users/updateInfo", "/api/users/checkIn", "/api/users/logout").hasRole("USER")
                        .requestMatchers("/api/users/info", "/api/users/points").authenticated()

                        .requestMatchers("/api/ai/**").hasRole("USER")

                        .requestMatchers("/products/currentUser", "/api/products/details").hasRole("USER")
                        .requestMatchers("/api/products/all", "/api/products/unApproved", "/api/products/approve", "/api/products/disApprove").hasRole("ADMIN")
                        .requestMatchers("/api/products/delete", "/api/products/update").authenticated()

                        .requestMatchers("/api/image/upload", "/api/image/purge").hasRole("USER")
                        .requestMatchers("/api/sysImg/upload").hasRole("ADMIN")

                        // 测试/临时放行接口
                        .requestMatchers("/api/users/all", "/api/users/delete").permitAll()
                        .requestMatchers("/api/admins/all", "/api/admins/updateUserStatus", "/api/admins/admindelete").permitAll()
                        .requestMatchers("/api/attractions/**", "/api/hotels/**", "/api/images/**").permitAll()

                        // 默认
                        .anyRequest().authenticated()
                )
                // 异常处理
                .exceptionHandling(exceptions -> exceptions
                        // Token无效或缺失时触发
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                        // 无权访问接口时触发
                        .accessDeniedHandler(restAccessDeniedHandler)
                )
                .addFilterBefore(tokenAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}