package org.dachuang_team.dc_backend_services.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.Customizer;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        //要使用的接口须在这里配置
                        .requestMatchers("/api/users/register", "/api/users/login","/api/users/updateInfo", "/api/users/all", "/api/users/delete").permitAll()
                        .requestMatchers("/api/admins/register", "/api/admins/login", "/api/admins/all", "/api/admins/updateUserStatus", "/api/admins/delete").permitAll()
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}