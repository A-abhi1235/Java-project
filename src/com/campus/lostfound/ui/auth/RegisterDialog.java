package com.campus.lostfound.ui.auth;

import com.campus.lostfound.exception.DatabaseException;
import com.campus.lostfound.exception.ValidationException;
import com.campus.lostfound.model.Role;
import com.campus.lostfound.model.User;
import com.campus.lostfound.service.AuthService;
import com.campus.lostfound.ui.components.ModernButton;
import com.campus.lostfound.ui.components.ModernComboBox;
import com.campus.lostfound.ui.components.ModernPasswordField;
import com.campus.lostfound.ui.components.ModernTextField;
import com.campus.lostfound.ui.theme.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class RegisterDialog extends JDialog {
    private final AuthService authService;
    private final ModernTextField usernameField = new ModernTextField(20);
    private final ModernPasswordField passwordField = new ModernPasswordField(20);
    private final ModernTextField fullNameField = new ModernTextField(20);
    private final ModernTextField emailField = new ModernTextField(20);
    private final ModernTextField phoneField = new ModernTextField(20);
    private final ModernTextField studentIdField = new ModernTextField(20);
    private final ModernComboBox<Role> roleCombo = new ModernComboBox<>(new Role[]{Role.STUDENT, Role.FACULTY});

    private boolean registrationSuccessful = false;
    private String registeredUsername = "";

    public RegisterDialog(Dialog parent, AuthService authService) {
        super(parent, "Register Campus Account", true);
        this.authService = authService;
        initUI();
    }

    private void initUI() {
        setSize(460, 600);
        setLocationRelativeTo(getParent());
        setResizable(false);
        getContentPane().setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(Color.WHITE);
        container.setBorder(new EmptyBorder(24, 32, 24, 32));

        JLabel titleLabel = new JLabel("Join Campus Lost & Found");
        titleLabel.setFont(Theme.FONT_HEADER);
        titleLabel.setForeground(Theme.PRIMARY_DARK);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subLabel = new JLabel("Register your student or staff profile");
        subLabel.setFont(Theme.FONT_BODY);
        subLabel.setForeground(Theme.TEXT_SECONDARY);
        subLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        container.add(titleLabel);
        container.add(Box.createVerticalStrut(4));
        container.add(subLabel);
        container.add(Box.createVerticalStrut(20));

        JPanel formGrid = new JPanel(new GridLayout(7, 2, 8, 12));
        formGrid.setBackground(Color.WHITE);

        formGrid.add(createLabel("Full Name *"));
        formGrid.add(fullNameField);

        formGrid.add(createLabel("Username *"));
        formGrid.add(usernameField);

        formGrid.add(createLabel("Password * (min 6 chars)"));
        formGrid.add(passwordField);

        formGrid.add(createLabel("Campus Email *"));
        formGrid.add(emailField);

        formGrid.add(createLabel("Phone Number"));
        formGrid.add(phoneField);

        formGrid.add(createLabel("Student / Staff ID"));
        formGrid.add(studentIdField);

        formGrid.add(createLabel("Role"));
        formGrid.add(roleCombo);

        container.add(formGrid);
        container.add(Box.createVerticalStrut(24));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        btnPanel.setBackground(Color.WHITE);

        ModernButton cancelBtn = new ModernButton("Cancel", ModernButton.Style.SECONDARY);
        cancelBtn.addActionListener(e -> dispose());

        ModernButton submitBtn = new ModernButton("Register Account", ModernButton.Style.PRIMARY);
        submitBtn.addActionListener(e -> performRegister());

        btnPanel.add(cancelBtn);
        btnPanel.add(submitBtn);

        container.add(btnPanel);
        add(container, BorderLayout.CENTER);
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.FONT_BODY_BOLD);
        label.setForeground(Theme.TEXT_PRIMARY);
        return label;
    }

    private void performRegister() {
        User u = new User();
        u.setUsername(usernameField.getText().trim());
        u.setFullName(fullNameField.getText().trim());
        u.setEmail(emailField.getText().trim());
        u.setPhone(phoneField.getText().trim());
        u.setStudentId(studentIdField.getText().trim());
        u.setRole((Role) roleCombo.getSelectedItem());

        String pass = new String(passwordField.getPassword());

        try {
            authService.register(u, pass);
            JOptionPane.showMessageDialog(this,
                "Account created successfully! You can now sign in.",
                "Registration Complete", JOptionPane.INFORMATION_MESSAGE);
            registrationSuccessful = true;
            registeredUsername = u.getUsername();
            dispose();
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (DatabaseException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isRegistrationSuccessful() {
        return registrationSuccessful;
    }

    public String getRegisteredUsername() {
        return registeredUsername;
    }
}
