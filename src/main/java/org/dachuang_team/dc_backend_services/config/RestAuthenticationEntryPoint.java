package org.dachuang_team.dc_backend_services.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.IOException;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final RequestMappingHandlerMapping handlerMapping;

    public RestAuthenticationEntryPoint(RequestMappingHandlerMapping handlerMapping) {
        this.handlerMapping = handlerMapping;
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        // 无映射接口：返回 404
        if (!hasHandler(request)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\": 404, \"message\": \"Not Found\", \"data\": null}");
            return;
        }

        // 有映射但未认证：返回 401（会话失效/未登录）
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\": 401, \"message\": \"认证失败：会话失效或未登录\", \"data\": null}");
    }

    private boolean hasHandler(HttpServletRequest request) {
        try {
            return handlerMapping.getHandler(request) != null;
        } catch (Exception e) {
            // 出错时按“有映射”处理，避免误判成404
            return true;
        }
    }
}

