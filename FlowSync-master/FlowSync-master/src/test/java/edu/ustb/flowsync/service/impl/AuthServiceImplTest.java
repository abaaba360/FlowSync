package edu.ustb.flowsync.service.impl;

import edu.ustb.flowsync.dto.LoginRequest;
import edu.ustb.flowsync.dto.PasswordUpdateRequest;
import edu.ustb.flowsync.entity.User;
import edu.ustb.flowsync.exception.BusinessException;
import edu.ustb.flowsync.mapper.UserMapper;
import edu.ustb.flowsync.utils.MD5Util;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceImplTest {

    @Test
    void loginMatchesMd5PasswordAndRemovesPasswordFromResponse() {
        UserMapper userMapper = mock(UserMapper.class);
        AuthServiceImpl service = new AuthServiceImpl();
        ReflectionTestUtils.setField(service, "userMapper", userMapper);

        User user = new User();
        user.setId(1L);
        user.setUsername("admin");
        user.setPassword(MD5Util.encrypt("123456"));
        when(userMapper.selectOne(any())).thenReturn(user);

        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("123456");

        assertNotNull(service.login(request));
        assertNull(user.getPassword());
    }

    @Test
    void loginThrowsBusinessExceptionWhenPasswordIsWrong() {
        UserMapper userMapper = mock(UserMapper.class);
        AuthServiceImpl service = new AuthServiceImpl();
        ReflectionTestUtils.setField(service, "userMapper", userMapper);

        User user = new User();
        user.setPassword(MD5Util.encrypt("123456"));
        when(userMapper.selectOne(any())).thenReturn(user);

        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("wrong");

        BusinessException exception = assertThrows(BusinessException.class, () -> service.login(request));
        assertEquals("用户名或密码错误", exception.getMsg());
    }

    @Test
    void updatePasswordStoresMd5Password() {
        UserMapper userMapper = mock(UserMapper.class);
        AuthServiceImpl service = new AuthServiceImpl();
        ReflectionTestUtils.setField(service, "userMapper", userMapper);

        User user = new User();
        user.setId(1L);
        user.setPassword(MD5Util.encrypt("old-pass"));
        when(userMapper.selectById(1L)).thenReturn(user);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        PasswordUpdateRequest request = new PasswordUpdateRequest();
        request.setUserId(1L);
        request.setOldPassword("old-pass");
        request.setNewPassword("new-pass");

        service.updatePassword(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).updateById(captor.capture());
        assertEquals(MD5Util.encrypt("new-pass"), captor.getValue().getPassword());
    }

    @Test
    void loginMigratesLegacyPlainPasswordToMd5() {
        UserMapper userMapper = mock(UserMapper.class);
        AuthServiceImpl service = new AuthServiceImpl();
        ReflectionTestUtils.setField(service, "userMapper", userMapper);

        User user = new User();
        user.setId(1L);
        user.setUsername("admin");
        user.setPassword("123456");
        when(userMapper.selectOne(any())).thenReturn(user);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("123456");

        service.login(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).updateById(captor.capture());
        assertEquals(MD5Util.encrypt("123456"), captor.getValue().getPassword());
    }
}
