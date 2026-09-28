package com.travelbloom.util;

import com.travelbloom.model.User;

/**
 * Very small in-memory session holder. Since this is a single-window
 * desktop app there's only ever one "logged in" user at a time.
 */
public class Session {

    private static User currentUser;

    public static void login(User user) {
        currentUser = user;
    }

    public static void logout() {
        currentUser = null;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static String getUsername() {
        return currentUser == null ? "Guest" : currentUser.getUsername();
    }
}