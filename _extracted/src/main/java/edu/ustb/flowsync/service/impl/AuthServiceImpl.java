package edu.ustb.flowsync.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import edu.ustb.flowsync.dto.LoginRequest;
import edu.ustb.flowsync.dto.LoginResponse;
import edu.ustb.flowsync.dto.PasswordUpdateRequest;
import edu.ustb.flowsync.entity.User;
import edu.ustb.flowsync.mapper.UserMapper;
import edu.ustb.flowsync.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public LoginResponse login(LoginRequest request) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", request.getUsername());
        User user = userMapper.selectOne(wrapper);
        if (user == null || !user.getPassword().equals(request.getPassword())) {
            return null;
        }
        user.setPassword(null);
        return new LoginResponse(user, "token-" + user.getId());
    }

    @Override
    public boolean updatePassword(PasswordUpdateRequest request) {
        User user = userMapper.selectById(request.getUserId());
        if (user == null || !user.getPassword().equals(request.getOldPassword())) {
            return false;
        }
        user.setPassword(request.getNewPassword());
        return userMapper.updateById(user) > 0;
    }
}
