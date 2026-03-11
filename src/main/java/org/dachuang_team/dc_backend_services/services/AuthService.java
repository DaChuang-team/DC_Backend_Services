package org.dachuang_team.dc_backend_services.services;

import org.dachuang_team.dc_backend_services.pojo.UserSession;
import org.dachuang_team.dc_backend_services.repository.UserSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private UserSessionRepository sessionRepository;

    /**
     * 校验 Token 是否存在且未过期
     * 对应你 Filter 中的 authService.validateToken(token)
     */
    public Optional<UserSession> validateToken(String token) {
        return sessionRepository.findByToken(token)
                .filter(session -> session.getExpiredAt().isAfter(LocalDateTime.now()));
    }

    /**
     * 登录成功后生成 Token 并存入数据库
     */
    @Transactional
    public String generateToken(Long userId) {
        // 1. 删除之前的旧 Token（限制单设备登录）
        sessionRepository.deleteByUserId(userId);

        // 2. 生成新 Token
        String token = UUID.randomUUID().toString().replace("-", "");

        // 3. 封装并保存
        UserSession session = new UserSession();
        session.setUserId(userId);
        session.setToken(token);
        session.setExpiredAt(LocalDateTime.now().plusDays(14)); // 设置14天有效期

        sessionRepository.save(session);
        return token;
    }
}