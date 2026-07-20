package edu.ustb.flowsync.controller;

import edu.ustb.flowsync.common.ApiResponse;
import edu.ustb.flowsync.dto.LoginRequest;
import edu.ustb.flowsync.dto.LoginResponse;
import edu.ustb.flowsync.dto.PasswordUpdateRequest;
import edu.ustb.flowsync.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        if (response == null) {
            return ApiResponse.fail("用户名或密码错误");
        }
        return ApiResponse.success("登录成功", response);
    }

    @PostMapping("/update-password")
    public ApiResponse<Object> updatePassword(@RequestBody PasswordUpdateRequest request) {
        if (authService.updatePassword(request)) {
            return ApiResponse.success("修改密码成功");
        }
        return ApiResponse.fail("旧密码错误或用户不存在");
    }
}
