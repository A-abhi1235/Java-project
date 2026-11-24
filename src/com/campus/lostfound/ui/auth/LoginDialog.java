package com.campus.lostfound.ui.auth;

import com.campus.lostfound.config.AppConfig;
import com.campus.lostfound.exception.AuthenticationException;
import com.campus.lostfound.exception.DatabaseException;
import com.campus.lostfound.model.User;
import com.campus.lostfound.service.AuthService;
import com.campus.lostfound.ui.components.ModernButton;
import com.campus.lostfound.ui.components.ModernPasswordField;
import com.campus.lostfound.ui.components.ModernTextField;
import com.campus.lostfound.ui.theme.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginDialog extends JDialog {
    private final AuthService authService;
    private final ModernTextField usernameField = new ModernTextField(20);
    private final ModernPasswordField passwordField = new ModernPasswordField(20);
    private boolean loginSuccessful = false;

    public LoginDialog(Frame parent, AuthService authService) {
        super(parent, "Sign In - Smart Campus Lost & Found", true);
        this.authService = authService;
        initUI();
    }

    private void initUI() {
        setSize(420, 520);
        setLocationRelativeTo(getParent());
        setResizable(false);
        getContentPane().setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(Color.WHITE);
        container.setBorder(new EmptyBorder(32, 36, 32, 36));

        // Header / Logo
        JLabel titleLabel = new JLabel("Campus Lost & Found");
        titleLabel.setFont(Theme.FONT_HEADER_LARGE);
        titleLabel.setForeground(Theme.PRIMARY_DARK);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Sign in with your campus credentials");
        subtitleLabel.setFont(Theme.FONT_BODY);
        subtitleLabel.setForeground(Theme.TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        container.add(titleLabel);
        container.add(Box.createVerticalStrut(4));
        container.add(subtitleLabel);
        container.add(Box.createVerticalStrut(28));

        // Form Fields
        JLabel userLabel = new JLabel("Username or Campus ID");
        userLabel.setFont(Theme.FONT_BODY_BOLD);
        userLabel.setForeground(Theme.TEXT_PRIMARY);
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(Theme.FONT_BODY_BOLD);
        passLabel.setForeground(Theme.TEXT_PRIMARY);
        passLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);

        container.add(userLabel);
        container.add(Box.createVerticalStrut(6));
        container.add(usernameField);
        container.add(Box.createVerticalStrut(16));
        container.add(passLabel);
        container.add(Box.createVerticalStrut(6));
        container.add(passwordField);
        container.add(Box.createVerticalStrut(24));

        // Sign In Button
        ModernButton loginBtn = new ModernButton("Sign In", ModernButton.Style.PRIMARY);
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginBtn.addActionListener(e -> performLogin());

        // Register Button
        ModernButton registerBtn = new ModernButton("Create New Student Account", ModernButton.Style.OUTLINE);
        registerBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        registerBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerBtn.addActionListener(e -> {
            RegisterDialog reg = new RegisterDialog(this, authService);
            reg.setVisible(true);
            if (reg.isRegistrationSuccessful()) {
                usernameField.setText(reg.getRegisteredUsername());
                passwordField.setText("");
                passwordField.requestFocus();
            }
        });

        container.add(loginBtn);
        container.add(Box.createVerticalStrut(10));
        container.add(registerBtn);
        container.add(Box.createVerticalStrut(20));

        // Quick demo credentials helper panel
        JPanel demoPanel = new JPanel(new GridLayout(2, 1, 6, 6));
        demoPanel.setBackground(new Color(248, 250, 252));
        demoPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER),
            new EmptyBorder(8, 12, 8, 12)
        ));
        demoPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JLabel demoHeader = new JLabel("Quick Fill Demo Accounts:");
        demoHeader.setFont(Theme.FONT_SMALL_BOLD);
        demoHeader.setForeground(Theme.TEXT_SECONDARY);

        JPanel demoBtns = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        demoBtns.setBackground(new Color(248, 250, 252));

        JButton quickAdmin = new JButton("Admin");
        quickAdmin.setFont(Theme.FONT_SMALL);
        quickAdmin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        quickAdmin.addActionListener(e -> {
            usernameField.setText(AppConfig.DEFAULT_ADMIN_USERNAME);
            passwordField.setText(AppConfig.DEFAULT_ADMIN_PASSWORD);
        });

        JButton quickStudent = new JButton("Student (John)");
        quickStudent.setFont(Theme.FONT_SMALL);
        quickStudent.setCursor(new Cursor(Cursor.HAND_CURSOR));
        quickStudent.addActionListener(e -> {
            usernameField.setText("john_student");
            passwordField.setText("student123");
        });

        demoBtns.add(quickAdmin);
        demoBtns.add(quickStudent);

        demoPanel.add(demoHeader);
        demoPanel.add(demoBtns);

        container.add(demoPanel);

        add(container, BorderLayout.CENTER);

        // Enter key submits login
        getRootPane().setDefaultButton(loginBtn);
    }

    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        try {
            User user = authService.login(username, password);
            if (user != null) {
                loginSuccessful = true;
                dispose();
            }
        } catch (AuthenticationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Authentication Failed", JOptionPane.WARNING_MESSAGE);
        } catch (DatabaseException ex) {
            JOptionPane.showMessageDialog(this, "Database connection error: " + ex.getMessage(), "System Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isLoginSuccessful() {
        return loginSuccessful;
    }
}
