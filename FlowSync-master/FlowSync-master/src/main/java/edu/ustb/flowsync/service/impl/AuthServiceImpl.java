package edu.ustb.flowsync.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import edu.ustb.flowsync.dto.LoginRequest;
import edu.ustb.flowsync.dto.LoginResponse;
import edu.ustb.flowsync.dto.PasswordUpdateRequest;
import edu.ustb.flowsync.entity.User;
import edu.ustb.flowsync.exception.BusinessException;
import edu.ustb.flowsync.mapper.UserMapper;
import edu.ustb.flowsync.service.AuthService;
import edu.ustb.flowsync.utils.MD5Util;
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
        if (user == null || !MD5Util.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        if (!MD5Util.isEncrypted(user.getPassword())) {
            User updateUser = new User();
            updateUser.setId(user.getId());
            updateUser.setPassword(MD5Util.encrypt(request.getPassword()));
            userMapper.updateById(updateUser);
        }
        user.setPassword(null);
        return new LoginResponse(user, "token-" + user.getId());
    }

    @Override
    public boolean updatePassword(PasswordUpdateRequest request) {
        User user = userMapper.selectById(request.getUserId());
        if (user == null || !MD5Util.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException(400, "旧密码错误或用户不存在");
        }
        user.setPassword(MD5Util.encrypt(request.getNewPassword()));
        if (userMapper.updateById(user) <= 0) {
            throw new BusinessException("修改密码失败");
        }
        return true;
    }
}
