package com.campus.lostfound;

import com.campus.lostfound.service.AuthService;
import com.campus.lostfound.ui.MainWindow;
import com.campus.lostfound.ui.auth.LoginDialog;
import com.campus.lostfound.util.DatabaseManager;
import com.campus.lostfound.util.SessionManager;

import javax.swing.*;

/**
 * Smart Campus Lost & Found Management System
 * Main Entry Point
 */
public class Main {
    public static void main(String[] args) {
        // Configure standard Swing Look & Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            try {
                // 1. Initialize SQLite Database and schema migrations
                DatabaseManager.getInstance();

                AuthService authService = new AuthService();

                // 2. Present Login Dialog before opening Main Window
                LoginDialog loginDialog = new LoginDialog(null, authService);
                loginDialog.setVisible(true);

                if (loginDialog.isLoginSuccessful() && SessionManager.getInstance().isLoggedIn()) {
                    MainWindow mainWindow = new MainWindow();
                    mainWindow.setVisible(true);
                } else {
                    // User closed login dialog without authenticating
                    System.exit(0);
                }

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                    "Application initialization error: " + e.getMessage(),
                    "Startup Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}
