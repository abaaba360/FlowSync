package edu.ustb.flowsync.dto;

import lombok.Data;

@Data
public class PasswordUpdateRequest {
    private Long userId;
    private String oldPassword;
    private String newPassword;
}
