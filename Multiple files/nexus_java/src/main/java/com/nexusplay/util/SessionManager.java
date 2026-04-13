package com.nexusplay.util;

import com.nexusplay.entity.User;

/**
 * Static session holder for the logged-in user.
 */
public final class SessionManager {

    private static User currentUser;

    private SessionManager() {}

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void clearSession() {
        currentUser = null;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static boolean isAdmin() {
        return currentUser != null && currentUser.getUserType() == User.UserType.ADMIN;
    }
}
