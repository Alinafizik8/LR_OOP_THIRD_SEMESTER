package service;

import model.Role;
import model.User;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public class AuthService {
    private static final Set<Role> ADMIN_ONLY = EnumSet.of(Role.ADMIN);
    private static final Set<Role> ADMIN_MODERATOR = EnumSet.of(Role.ADMIN, Role.MODERATOR);

    public static boolean hasRole(User user, Role required) {
        return user != null && user.getRoles() != null && user.getRoles().contains(required);
    }

    public static boolean hasAnyRole(User user, Set<Role> allowed) {
        return user != null && user.getRoles() != null && !Collections.disjoint(user.getRoles(), allowed);
    }

    public static boolean canAccessAdminPanel(User user) {
        return hasAnyRole(user, ADMIN_ONLY);
    }

    public static boolean canManageUsers(User user) {
        return hasAnyRole(user, ADMIN_ONLY);
    }

    public static boolean canViewReports(User user) {
        return hasAnyRole(user, ADMIN_MODERATOR);
    }
}