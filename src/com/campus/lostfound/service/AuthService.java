package com.campus.lostfound.service;

import com.campus.lostfound.dao.UserDao;
import com.campus.lostfound.dao.impl.SQLiteUserDao;
import com.campus.lostfound.exception.AuthenticationException;
import com.campus.lostfound.exception.DatabaseException;
import com.campus.lostfound.exception.ValidationException;
import com.campus.lostfound.model.Role;
import com.campus.lostfound.model.User;
import com.campus.lostfound.util.PasswordUtil;
import com.campus.lostfound.util.SessionManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AuthService {
    private final UserDao userDao;

    public AuthService() {
        this.userDao = new SQLiteUserDao();
    }

    public AuthService(UserDao userDao) {
        this.userDao = userDao;
    }

    public User login(String username, String password) throws AuthenticationException, DatabaseException {
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            throw new AuthenticationException("Username and password are required.");
        }

        Optional<User> userOpt = userDao.findByUsername(username.trim());
        if (!userOpt.isPresent()) {
            throw new AuthenticationException("Invalid username or password.");
        }

        User user = userOpt.get();
        boolean valid = PasswordUtil.verifyPassword(password, user.getPasswordHash(), user.getSalt());
        if (!valid) {
            throw new AuthenticationException("Invalid username or password.");
        }

        SessionManager.getInstance().login(user);
        return user;
    }

    public User register(User newUser, String plainPassword) throws ValidationException, DatabaseException {
        List<String> errors = new ArrayList<>();

        if (newUser.getUsername() == null || newUser.getUsername().trim().length() < 3) {
            errors.add("Username must be at least 3 characters long.");
        }
        if (plainPassword == null || plainPassword.length() < 6) {
            errors.add("Password must be at least 6 characters long.");
        }
        if (newUser.getFullName() == null || newUser.getFullName().trim().isEmpty()) {
            errors.add("Full name is required.");
        }
        if (newUser.getEmail() == null || !newUser.getEmail().contains("@")) {
            errors.add("A valid campus email is required.");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }

        // Check uniqueness
        if (userDao.findByUsername(newUser.getUsername().trim()).isPresent()) {
            throw new ValidationException("Username '" + newUser.getUsername() + "' is already registered.");
        }
        if (userDao.findByEmail(newUser.getEmail().trim()).isPresent()) {
            throw new ValidationException("Email '" + newUser.getEmail() + "' is already registered.");
        }

        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hashPassword(plainPassword, salt);

        newUser.setPasswordHash(hash);
        newUser.setSalt(salt);
        newUser.setCreatedAt(LocalDateTime.now());
        if (newUser.getRole() == null) {
            newUser.setRole(Role.STUDENT);
        }

        return userDao.create(newUser);
    }

    public void logout() {
        SessionManager.getInstance().logout();
    }

    public boolean updateProfile(User user) throws ValidationException, DatabaseException {
        if (user.getFullName() == null || user.getFullName().trim().isEmpty()) {
            throw new ValidationException("Full name cannot be empty.");
        }
        if (user.getEmail() == null || !user.getEmail().contains("@")) {
            throw new ValidationException("Valid email is required.");
        }
        return userDao.update(user);
    }

    public List<User> getAllUsers() throws DatabaseException {
        return userDao.findAll();
    }
}
