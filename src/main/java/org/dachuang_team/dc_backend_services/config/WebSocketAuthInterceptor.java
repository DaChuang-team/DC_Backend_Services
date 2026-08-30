package org.dachuang_team.dc_backend_services.config;

import org.dachuang_team.dc_backend_services.domain.PO.TokenSession;
import org.dachuang_team.dc_backend_services.services.AuthService;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatusCode;
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
            response.setStatusCode(HttpStatusCode.valueOf(401));
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
        // 先尝试从Header取
        List<String> authHeaders = request.getHeaders().get("Authorization");
        if (authHeaders != null && !authHeaders.isEmpty()) {
            String bearer = authHeaders.get(0);
            if (bearer != null && bearer.startsWith("Bearer ")) {
                String token = bearer.substring(7).trim();
                return token.isEmpty() ? null : token;
            }
        }

        // 从URL参数取
        // ws://localhost:8080/ws?token=xxx
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