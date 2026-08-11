package com.warehousemanager.services;

import com.warehousemanager.models.dto.LoginRequest;
import com.warehousemanager.models.entity.User;
import com.warehousemanager.repositories.UserRepository;
import com.warehousemanager.utils.Validator;
import com.warehousemanager.utils.PasswordHasher;

public class AuthService {

    private final UserRepository userRepo = new UserRepository();

    public User login(LoginRequest req) {
        Validator.requireNonBlank(req.username(), "Username");
        Validator.requireNonBlank(req.password(), "Password");

        User u = userRepo.findByUsername(req.username());
        if (u == null) {
            return null;
        }
        if (!PasswordHasher.verify(req.password(), u.getSalt(), u.getPasswordHash())) {
            return null;
        }

        return u;
    }

    public boolean register(String username, String password, String confirmPassword, String email) {
        Validator.requireNonBlank(username, "Username");
        Validator.requireMinLength(username, "Username", 3);
        Validator.requireNonBlank(password, "Password");
        Validator.requireMinLength(password, "Password", 6);

        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        if (userRepo.existsByUsername(username)) {
            throw new IllegalArgumentException("Username \"" + username + "\" already taken.");

        }

        String salt = PasswordHasher.generateSalt();

        User u = new User();
        u.setUsername(username);
        u.setSalt(salt);
        u.setPasswordHash(PasswordHasher.hash(password, salt));
        u.setEmail(email == null ? "" : email.trim());
        u.setRole("user");
        u.setStatus(true);

        return userRepo.insert(u);
    }
}
