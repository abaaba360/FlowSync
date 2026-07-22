package edu.ustb.flowsync.auth;

import edu.ustb.flowsync.entity.User;
import edu.ustb.flowsync.exception.BusinessException;
import edu.ustb.flowsync.mapper.UserMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthInterceptorTest {

    @AfterEach
    void tearDown() {
        AuthContext.clear();
    }

    @Test
    void preHandleLoadsCurrentUserFromAuthorizationToken() throws Exception {
        UserMapper userMapper = mock(UserMapper.class);
        User user = new User();
        user.setId(1L);
        user.setUsername("leader");
        user.setRole("项目负责人");
        when(userMapper.selectById(1L)).thenReturn(user);

        AuthInterceptor interceptor = new AuthInterceptor(userMapper);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertTrue(interceptor.preHandle(request, response, new Object()));
        assertEquals(1L, AuthContext.getRequiredCurrentUser().getId());
    }

    @Test
    void preHandleRejectsMissingToken() {
        AuthInterceptor interceptor = new AuthInterceptor(mock(UserMapper.class));
        MockHttpServletRequest request = new MockHttpServletRequest();

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> interceptor.preHandle(request, new MockHttpServletResponse(), new Object())
        );

        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, exception.getCode());
        assertEquals("请先登录", exception.getMsg());
    }
}
