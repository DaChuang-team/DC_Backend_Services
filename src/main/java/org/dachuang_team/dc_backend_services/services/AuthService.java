package org.dachuang_team.dc_backend_services.services;

import org.dachuang_team.dc_backend_services.domain.PO.TokenSession;
import org.dachuang_team.dc_backend_services.repository.UserSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AuthService {

    @Autowired
    private UserSessionRepository sessionRepository;

    @Autowired
    private RedisTemplate<String, TokenSession> redisTemplate;

    private static final String TOKEN_SESSION_PREFIX = "tokenSession:";

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    // 校验Token是否存在且未过期
    public Optional<TokenSession> validateToken(String token) {
        String redisKey = TOKEN_SESSION_PREFIX + token;

        // 优先从Redis中读取
        TokenSession cachedSession = redisTemplate.opsForValue().get(redisKey);
        if (cachedSession != null && cachedSession.getExpiredAt().isAfter(LocalDateTime.now())) {
            logger.info("Token [{}] 信息从 Redis 中读取", token);
            return Optional.of(cachedSession);
        }

        // 如果Redis中没有，从数据库查询
        Optional<TokenSession> sessionOptional = sessionRepository.findByToken(token)
                .filter(session -> session.getExpiredAt().isAfter(LocalDateTime.now()));

        if (sessionOptional.isPresent()) {
            logger.info("Token [{}] 信息从数据库中读取", token);
            // 如果数据库中存在，加入Redis缓存
            long remainingSeconds = Duration.between(LocalDateTime.now(), sessionOptional.get().getExpiredAt()).getSeconds();
            if (remainingSeconds > 0) {
                redisTemplate.opsForValue().set(
                        redisKey, sessionOptional.get(),
                        remainingSeconds,
                        TimeUnit.SECONDS
                );
            }
        } else {
            logger.info("Token [{}] 信息不存在", token);
        }

        return sessionOptional;
    }

    @Transactional
    public String generateToken(Long id, String role) {
        // 清理该角色下的旧Token
        sessionRepository.deleteByUserIdAndUserRole(id, role);

        // 生成新记录
        String token = UUID.randomUUID().toString().replace("-", "");
        TokenSession session = new TokenSession();
        session.setUserId(id);
        session.setUserRole(role);
        session.setToken(token);
        session.setExpiredAt(LocalDateTime.now().plusDays(14)); // 设置过期时间为两周

        // 保存到数据库
        sessionRepository.save(session);

        // 保存到Redis，过期时间3天，浮动±1小时
        String redisKey = TOKEN_SESSION_PREFIX + token;
        long baseExpireHours = 72; // 3天
        long randomOffset = ThreadLocalRandom.current().nextLong(-1, 2); // -1, 0, 1
        long expireHours = baseExpireHours + randomOffset;
        redisTemplate.opsForValue().set(redisKey, session, expireHours, TimeUnit.HOURS);

        return token;
    }


    // 主动失效Token
    @Transactional
    public void invalidateToken(Long userId, String role) {
        // 由于没有直接存储userId和role到Redis中，我们需要先查询数据库获取对应的token
        Optional<TokenSession> sessionOptional = sessionRepository.findByUserIdAndUserRole(userId, role);
        // 删除数据库中的TokenSession记录
        sessionRepository.deleteByUserIdAndUserRole(userId, role);

        // 删除Redis中的TokenSession缓存

        sessionOptional.ifPresent(session -> {
            String redisKey = TOKEN_SESSION_PREFIX + session.getToken();
            redisTemplate.delete(redisKey);
            logger.info("Token [{}] 已被主动失效", session.getToken());
        });
    }
}