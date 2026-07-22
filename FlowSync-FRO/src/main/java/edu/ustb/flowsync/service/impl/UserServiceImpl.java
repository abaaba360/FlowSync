package edu.ustb.flowsync.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import edu.ustb.flowsync.auth.AuthContext;
import edu.ustb.flowsync.dto.PasswordUpdateRequest;
import edu.ustb.flowsync.entity.User;
import edu.ustb.flowsync.exception.BusinessException;
import edu.ustb.flowsync.mapper.UserMapper;
import edu.ustb.flowsync.service.PermissionService;
import edu.ustb.flowsync.service.UserService;
import edu.ustb.flowsync.utils.MD5Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public List<User> findList() {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.orderByAsc("id");
        List<User> users = userMapper.selectList(wrapper);
        users.forEach(user -> user.setPassword(null));
        return users;
    }

    @Override
    public User findById(Long id) {
        User user = userMapper.selectById(id);
        if (user != null) {
            user.setPassword(null);
        }
        return user;
    }

    @Override
    public boolean updatePassword(PasswordUpdateRequest request) {
        User currentUser = AuthContext.getRequiredCurrentUser();
        if (!Objects.equals(currentUser.getId(), request.getUserId()) && !PermissionService.isAdmin(currentUser)) {
            throw new BusinessException(403, "无权限操作");
        }
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
