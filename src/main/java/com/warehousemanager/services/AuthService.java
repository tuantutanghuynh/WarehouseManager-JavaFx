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
}
