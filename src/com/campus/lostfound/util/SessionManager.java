package com.campus.lostfound.util;

import com.campus.lostfound.model.Role;
import com.campus.lostfound.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Singleton managing the currently logged-in user session.
 */
public class SessionManager {
    private static SessionManager instance;
    private User currentUser;
    private final List<SessionListener> listeners = new ArrayList<>();

    public interface SessionListener {
        void onUserLoggedIn(User user);
        void onUserLoggedOut();
    }

    private SessionManager() {}

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void addListener(SessionListener listener) {
        listeners.add(listener);
    }

    public void removeListener(SessionListener listener) {
        listeners.remove(listener);
    }

    public synchronized void login(User user) {
        this.currentUser = user;
        for (SessionListener listener : listeners) {
            listener.onUserLoggedIn(user);
        }
    }

    public synchronized void logout() {
        this.currentUser = null;
        for (SessionListener listener : listeners) {
            listener.onUserLoggedOut();
        }
    }

    public synchronized User getCurrentUser() {
        return currentUser;
    }

    public synchronized boolean isLoggedIn() {
        return currentUser != null;
    }

    public synchronized boolean isAdmin() {
        return currentUser != null && currentUser.getRole() == Role.ADMIN;
    }
}
