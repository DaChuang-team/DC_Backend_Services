package org.dachuang_team.dc_backend_services.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    public WebSocketConfig(WebSocketAuthInterceptor webSocketAuthInterceptor) {
        this.webSocketAuthInterceptor = webSocketAuthInterceptor;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 基础代理频道前缀，推送给客户端的地址必须以这些开头
        registry.enableSimpleBroker("/sys", "/topic", "/queue");

        registry.setApplicationDestinationPrefixes("/app");

        // 点对点频道前缀。客户端订阅时使用 /user/queue/message
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 支持原生WebSocket
        registry.addEndpoint("/ws")
                .setHandshakeHandler(customHandshakeHandler())
                .addInterceptors(webSocketAuthInterceptor)
                .setAllowedOriginPatterns("*");

        // 兼容不支持原生WebSocket的旧版浏览器
        registry.addEndpoint("/sockjs-ws")
                .setHandshakeHandler(customHandshakeHandler())
                .addInterceptors(webSocketAuthInterceptor)
                .setAllowedOriginPatterns("*")
                .withSockJS();

    }

    @Bean
    public DefaultHandshakeHandler customHandshakeHandler() {
        return new DefaultHandshakeHandler() {
            @Override
            protected Principal determineUser(ServerHttpRequest request,
                                              WebSocketHandler wsHandler,
                                              Map<String, Object> attributes) {
                // 包装成Principal返回给框架
                String principalName = (String) attributes.get("principalName");
                if (principalName == null) return null;
                return () -> principalName;
            }
        };
    }
}
