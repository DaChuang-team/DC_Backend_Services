package org.dachuang_team.dc_backend_services.services;

import org.dachuang_team.dc_backend_services.pojo.tokenSession;
import org.dachuang_team.dc_backend_services.repository.userSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private userSessionRepository sessionRepository;

    /**
     * 校验 Token 是否存在且未过期
     * 对应 Filter 中的 authService.validateToken(token)
     */
    public Optional<tokenSession> validateToken(String token) {
        return sessionRepository.findByToken(token)
                .filter(session -> session.getExpiredAt().isAfter(LocalDateTime.now()));
    }

    /**
     * 登录成功后生成 Token 并存入数据库
     */
    @Transactional
    public String generateToken(Long id, String role) {
        // 清理该角色下的旧 Token
        sessionRepository.deleteByUserIdAndUserRole(id, role);

        // 生成新记录
        String token = UUID.randomUUID().toString().replace("-", "");
        tokenSession session = new tokenSession();
        session.setUserId(id);
        session.setUserRole(role);
        session.setToken(token);
        session.setExpiredAt(LocalDateTime.now().plusDays(7));

        sessionRepository.save(session);
        return token;
    }
}