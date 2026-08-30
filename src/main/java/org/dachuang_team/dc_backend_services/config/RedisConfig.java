package org.dachuang_team.dc_backend_services.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.dachuang_team.dc_backend_services.domain.PO.TokenSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    private static final Logger logger = LoggerFactory.getLogger(RedisConfig.class);

    @Bean
    public RedisTemplate<String, TokenSession> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, TokenSession> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 自定义ObjectMapper
        ObjectMapper om = new ObjectMapper();
        // 注册Java8时间模块
        om.registerModule(new JavaTimeModule());
        // 关闭将日期写为时间戳的行为
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 设置访问权限，确保能够序列化所有属性
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);

        // 激活默认类型信息
        om.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);

        // 使用带参数的构造函数，导入配置好的om
        GenericJackson2JsonRedisSerializer jacksonSerializer = new GenericJackson2JsonRedisSerializer(om);

        // 设置序列化方案
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(jacksonSerializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(jacksonSerializer);

        template.afterPropertiesSet();

        try {
            connectionFactory.getConnection().ping();
            logger.info("成功连接到Redis数据库");
        } catch (Exception e) {
            logger.error("无法连接到Redis数据库: {}", e.getMessage());
            throw new IllegalStateException("Redis 连接失败，无法启动应用程序", e);
        }

        return template;
    }
}