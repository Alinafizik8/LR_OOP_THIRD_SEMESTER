package service;

import functions.dao.UserDAOPassword;
import model.User;
import model.Role;
import util.PasswordUtil;

import java.util.Set;

public class UserService {
    private final UserDAOPassword userDAO = new UserDAOPassword();

    public User createUser(String username, String rawPassword, Set<Role> roles) {
        if (userDAO.findByUsername(username) != null) {
            throw new IllegalArgumentException("User already exists");
        }
        User user = new User(username, PasswordUtil.hashPassword(rawPassword));
        user.setRoles(roles);
        userDAO.findByUsername(user, rawPassword);
        return user;
    }

    public User authenticate(String username, String rawPassword) {
        User user = userDAO.findByUsername(username);
        if (user != null && user.isEnabled() && PasswordUtil.verifyPassword(rawPassword, user.getPasswordHash())) {
            return user;
        }
        return null;
    }
}