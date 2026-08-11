package com.warehousemanager.session;

import com.warehousemanager.models.entity.User;

// Singleton giữ trạng thái người dùng đang đăng nhập
public class UserSession {

    private static User currentUser;

    private UserSession() {
    }

    public static void set(User user) {
        currentUser = user;
    }

    public static User get() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static boolean isAdmin() {
        return currentUser != null && "admin".equalsIgnoreCase(currentUser.getRole());
    }

    public static void clear() {
        currentUser = null;
    }
}
