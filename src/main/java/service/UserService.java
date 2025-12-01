package service;

import functions.dao.UserDAOPassword;
import model.User;
import model.Role;
import util.PasswordUtil;

import java.util.HashMap;
import java.util.Map;
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

    private static final Map<String, User> users = new HashMap<>();
    static {
        users.put("user", new User("user", "word"));
        users.put("admin", new User("admin", "pass"));
    }

    public User authenticate(String username, String rawPassword) {
        User user = userDAO.findByUsername(username);
        if (user != null && user.isEnabled() && PasswordUtil.verifyPassword(rawPassword, user.getPasswordHash())) {
            return user;
        }
        return null;
    }
//    public User authenticate(String username, String password) {
//        User u = users.get(username);
//        if (u != null && u.getPassword().equals(password)) {
//            return u;
//        }
//        return null;
//    }

    public void register(String username, String password) {
        if (users.containsKey(username)) {
            throw new RuntimeException("Пользователь уже существует");
        }
        int id = users.size() + 1;
        users.put(username, new User(username, password));
    }
}