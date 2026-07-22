package edu.ustb.flowsync.service;

import edu.ustb.flowsync.dto.LoginRequest;
import edu.ustb.flowsync.dto.LoginResponse;
import edu.ustb.flowsync.dto.PasswordUpdateRequest;

public interface AuthService {
    LoginResponse login(LoginRequest request);

    boolean updatePassword(PasswordUpdateRequest request);
}
