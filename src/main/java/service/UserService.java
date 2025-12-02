//package service;
//
//import functions.dao.UserDAOPassword;
//import model.User;
//import model.Role;
//import util.PasswordUtil;
//
//import java.util.Set;
//
//public class UserService {
//    private final UserDAOPassword userDAO = new UserDAOPassword();
//
//    public User createUser(String username, String rawPassword, Set<Role> roles) {
//        if (userDAO.findByUsername(username) != null) {
//            throw new IllegalArgumentException("User already exists");
//        }
//        User user = new User(username, PasswordUtil.hashPassword(rawPassword));
//        user.setRoles(roles);
//        userDAO.findByUsername(user, rawPassword);
//        return user;
//    }
//
//    public User authenticate(String username, String rawPassword) {
//        User user = userDAO.findByUsername(username);
//        if (user != null && user.isEnabled() && PasswordUtil.verifyPassword(rawPassword, user.getPasswordHash())) {
//            return user;
//        }
//        return null;
//    }
////    public User authenticate(String username, String password) {
////        User u = users.get(username);
////        if (u != null && u.getPassword().equals(password)) {
////            return u;
////        }
////        return null;
////    }
//
//}
package service;

import functions.dao.UserDAOPassword;
import model.User;
import model.Role;

import java.util.HashSet;
import java.util.Set;

public class UserService {
    private final UserDAOPassword userDAO = new UserDAOPassword();

    // ✅ Метод для регистрации — вызывается из AuthServlet
    public void register(String username, String rawPassword) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Логин не может быть пустым");
        }
        if (rawPassword == null || rawPassword.length() < 4) {
            throw new IllegalArgumentException("Пароль должен быть не короче 4 символов");
        }

        // Проверяем, существует ли уже
        if (userDAO.findByUsername(username) != null) {
            throw new RuntimeException("Пользователь уже существует");
        }

        // Создаём пользователя с ролью USER по умолчанию
        Set<Role> roles = new HashSet<>();
        roles.add(Role.USER);

        String hashedPassword = util.PasswordUtil.hashPassword(rawPassword);
        User user = new User(username, hashedPassword);
        user.setRoles(roles);
        user.setEnabled(true);

        // Сохраняем в БД
        userDAO.save(user); // ← ключевой метод: должен быть в UserDAOPassword
    }

    // ✅ Метод для входа — уже почти правильный, но доработан
    public User authenticate(String username, String rawPassword) {
        if (username == null || rawPassword == null) {
            return null;
        }

        User user = userDAO.findByUsername(username);
        if (user != null
                && user.isEnabled()
                && util.PasswordUtil.verifyPassword(rawPassword, user.getPasswordHash())) {
            return user;
        }
        return null;
    }

    // 🔧 Вспомогательный метод (если нужен для тестов или админа)
    public void createAdmin(String username, String rawPassword) {
        Set<Role> roles = new HashSet<>();
        roles.add(Role.ADMIN);
        roles.add(Role.USER);

        String hashed = util.PasswordUtil.hashPassword(rawPassword);
        User admin = new User(username, hashed);
        admin.setRoles(roles);
        admin.setEnabled(true);
        userDAO.save(admin);
    }
}