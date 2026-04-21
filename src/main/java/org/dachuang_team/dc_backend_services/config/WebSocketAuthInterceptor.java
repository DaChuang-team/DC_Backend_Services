package org.dachuang_team.dc_backend_services.config;

import org.dachuang_team.dc_backend_services.domain.PO.TokenSession;
import org.dachuang_team.dc_backend_services.services.AuthService;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private final AuthService authService;

    public WebSocketAuthInterceptor(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean beforeHandshake(@NotNull ServerHttpRequest request,
                                   @NotNull ServerHttpResponse response,
                                   @NotNull WebSocketHandler wsHandler,
                                   @NotNull Map<String, Object> attributes) {

        String token = extractToken(request);
        if (token == null) {
            return false;
        }

        // 直接复用你 TokenAuthFilter 里的同款逻辑
        Optional<TokenSession> sessionOpt = authService.validateToken(token);
        if (sessionOpt.isEmpty()) {
            return false;
        }

        TokenSession session = sessionOpt.get();
        Long userId     = session.getUserId();
        String userRole = session.getUserRole();

        if (userId == null || userRole == null) {
            return false;
        }

        // 和 TokenAuthFilter 里的 authority 格式对齐
        // TokenAuthFilter：new SimpleGrantedAuthority("ROLE_" + session.getUserRole())
        // 这里取 role 直接用原始值，Principal 不需要加 ROLE_ 前缀
        String principalName = userId + "_" + userRole;
        attributes.put("principalName", principalName);

        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
    }

    private String extractToken(ServerHttpRequest request) {
        // WS 握手本质是 HTTP 请求，先尝试从 Header 取
        // 客户端 WS 连接时在 Header 带上 Authorization: Bearer xxx
        List<String> authHeaders = request.getHeaders().get("Authorization");
        if (authHeaders != null && !authHeaders.isEmpty()) {
            String bearer = authHeaders.get(0);
            if (bearer != null && bearer.startsWith("Bearer ")) {
                String token = bearer.substring(7).trim();
                return token.isEmpty() ? null : token;
            }
        }

        // 备用：从 URL 参数取
        // ws://localhost:8080/ws?token=xxx
        // 部分前端 WS 库不支持自定义 Header，只能走这个方式
        URI uri = request.getURI();
        String query = uri.getQuery();
        if (query != null) {
            for (String param : query.split("&")) {
                if (param.startsWith("token=")) {
                    String token = param.substring(6).trim();
                    return token.isEmpty() ? null : token;
                }
            }
        }

        return null;
    }
}