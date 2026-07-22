package edu.ustb.flowsync.auth;

import edu.ustb.flowsync.entity.User;
import edu.ustb.flowsync.exception.BusinessException;

public final class AuthContext {
    private static final ThreadLocal<User> CURRENT_USER = new ThreadLocal<>();

    private AuthContext() {
    }

    public static void setCurrentUser(User user) {
        CURRENT_USER.set(user);
    }

    public static User getCurrentUser() {
        return CURRENT_USER.get();
    }

    public static User getRequiredCurrentUser() {
        User user = getCurrentUser();
        if (user == null) {
            throw new BusinessException(401, "请先登录");
        }
        return user;
    }

    public static void clear() {
        CURRENT_USER.remove();
    }
}
