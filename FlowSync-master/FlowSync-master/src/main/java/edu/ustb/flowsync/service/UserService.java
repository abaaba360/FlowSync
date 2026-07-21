package edu.ustb.flowsync.service;

import edu.ustb.flowsync.dto.PasswordUpdateRequest;
import edu.ustb.flowsync.entity.User;

import java.util.List;

public interface UserService {
    List<User> findList();

    User findById(Long id);

    boolean updatePassword(PasswordUpdateRequest request);
}
