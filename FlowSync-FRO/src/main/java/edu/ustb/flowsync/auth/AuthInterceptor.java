package edu.ustb.flowsync.auth;

import edu.ustb.flowsync.entity.User;
import edu.ustb.flowsync.exception.BusinessException;
import edu.ustb.flowsync.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    private static final String TOKEN_PREFIX = "token-";

    private final UserMapper userMapper;

    public AuthInterceptor(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = resolveToken(request);
        if (token == null || !token.startsWith(TOKEN_PREFIX)) {
            throw new BusinessException(HttpServletResponse.SC_UNAUTHORIZED, "请先登录");
        }

        Long userId = parseUserId(token);
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(HttpServletResponse.SC_UNAUTHORIZED, "请先登录");
        }

        AuthContext.setCurrentUser(user);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        AuthContext.clear();
    }

    private String resolveToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring("Bearer ".length());
        }
        if (token == null || token.isBlank()) {
            token = request.getHeader("X-Auth-Token");
        }
        return token == null ? null : token.trim();
    }

    private Long parseUserId(String token) {
        try {
            return Long.valueOf(token.substring(TOKEN_PREFIX.length()));
        } catch (NumberFormatException e) {
            throw new BusinessException(HttpServletResponse.SC_UNAUTHORIZED, "请先登录");
        }
    }
}
