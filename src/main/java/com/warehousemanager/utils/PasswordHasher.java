package com.warehousemanager.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import com.warehousemanager.exceptions.AppException;

public class PasswordHasher {

    private PasswordHasher() {
    }

    // sinh chuỗi salt ngẫu nhiên 16bytes
    public static String generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    // Băm mật khẩu cùng với salt SHA-256
    public static String hash(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(Base64.getDecoder().decode(salt));
            byte[] hashedBytes = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new AppException("Hash error: " + e.getMessage(), e);
        }
    }

    // kiểm tra mật khẩu người dùn đăng nhập vào có đúng không
    public static boolean verify(String password, String salt, String expectedHash) {
        String actualHash = hash(password, salt);
        return actualHash.equals(expectedHash);
    }
}
