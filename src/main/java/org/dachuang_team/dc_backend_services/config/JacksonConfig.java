package org.dachuang_team.dc_backend_services.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration  //标记为配置类，Spring 启动时会加载并执行其中的 @Bean 方法
public class JacksonConfig {

    //  覆盖 Spring Boot 默认的 ObjectMapper 配置
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        //  注册 JavaTimeModule 模块：解决 Java 8 时间类型（LocalDateTime/LocalDate 等）的序列化/反序列化
        objectMapper.registerModule(new JavaTimeModule());
        //  禁用「将日期序列化为时间戳」的特性：让时间类型输出为「yyyy-MM-dd HH:mm:ss」格式的字符串，而非 13 位数字时间戳
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
    }
}