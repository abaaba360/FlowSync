package edu.ustb.flowsync.controller;

import edu.ustb.flowsync.common.ApiResponse;
import edu.ustb.flowsync.dto.PasswordUpdateRequest;
import edu.ustb.flowsync.entity.User;
import edu.ustb.flowsync.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping
    public ApiResponse<List<User>> list() {
        return ApiResponse.success("查询用户列表成功", userService.findList());
    }

    @GetMapping("/{id}")
    public ApiResponse<User> one(@PathVariable("id") Long id) {
        return ApiResponse.success("查询用户成功", userService.findById(id));
    }

    @PostMapping("/update-password")
    public ApiResponse<Object> updatePassword(@RequestBody PasswordUpdateRequest request) {
        userService.updatePassword(request);
        return ApiResponse.success("修改密码成功");
    }
}
