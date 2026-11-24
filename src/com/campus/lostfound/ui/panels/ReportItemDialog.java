package com.campus.lostfound.ui.panels;

import com.campus.lostfound.config.AppConfig;
import com.campus.lostfound.exception.DatabaseException;
import com.campus.lostfound.exception.ValidationException;
import com.campus.lostfound.model.*;
import com.campus.lostfound.service.ItemService;
import com.campus.lostfound.util.DateUtil;
import com.campus.lostfound.util.ImageUtil;
import com.campus.lostfound.util.SessionManager;
import com.campus.lostfound.ui.components.ModernButton;
import com.campus.lostfound.ui.components.ModernComboBox;
import com.campus.lostfound.ui.components.ModernTextField;
import com.campus.lostfound.ui.theme.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class ReportItemDialog extends JDialog {
    private final ItemService itemService;
    private final Runnable onSavedCallback;

    private JRadioButton lostRadio;
    private JRadioButton foundRadio;

    private ModernTextField titleField;
    private ModernComboBox<Category> categoryCombo;
    private ModernComboBox<String> locationCombo;
    private ModernTextField dateField;
    private ModernTextField phoneField;
    private ModernTextField emailField;
    private JTextArea descArea;

    // Lost-specific fields
    private JPanel lostSpecificPanel;
    private ModernTextField rewardField;
    private ModernTextField lastSeenField;

    // Found-specific fields
    private JPanel foundSpecificPanel;
    private ModernTextField storageLocField;
    private ModernTextField verifQuestionField;

    // Image upload
    private JLabel imagePreviewLabel;
    private String savedImageFileName = null;

    public ReportItemDialog(Frame parent, ItemService itemService, ItemType defaultType, Runnable onSavedCallback) {
        super(parent, "Report Item - Campus Lost & Found", true);
        this.itemService = itemService;
        this.onSavedCallback = onSavedCallback;
        initUI(defaultType);
    }

    private void initUI(ItemType defaultType) {
        setSize(680, 750);
        setLocationRelativeTo(getParent());
        getContentPane().setBackground(Theme.BG_MAIN);
        setLayout(new BorderLayout());

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(new EmptyBorder(16, 24, 16, 24));

        JLabel titleLabel = new JLabel("Report Campus Belonging");
        titleLabel.setFont(Theme.FONT_HEADER);
        titleLabel.setForeground(Theme.PRIMARY_DARK);

        JLabel subLabel = new JLabel("Provide accurate details to help match and return belongings faster.");
        subLabel.setFont(Theme.FONT_BODY);
        subLabel.setForeground(Theme.TEXT_SECONDARY);

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subLabel, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // Form Container with ScrollPane
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(20, 24, 20, 24));

        // 1. Item Type Selector
        JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        typePanel.setBackground(Color.WHITE);
        typePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel typeLabel = new JLabel("Report Type: ");
        typeLabel.setFont(Theme.FONT_BODY_BOLD);

        lostRadio = new JRadioButton("I Lost Something", defaultType == ItemType.LOST);
        foundRadio = new JRadioButton("I Found Something", defaultType == ItemType.FOUND);
        lostRadio.setFont(Theme.FONT_BODY_BOLD);
        foundRadio.setFont(Theme.FONT_BODY_BOLD);
        lostRadio.setBackground(Color.WHITE);
        foundRadio.setBackground(Color.WHITE);

        ButtonGroup group = new ButtonGroup();
        group.add(lostRadio);
        group.add(foundRadio);

        typePanel.add(typeLabel);
        typePanel.add(lostRadio);
        typePanel.add(foundRadio);

        form.add(typePanel);
        form.add(Box.createVerticalStrut(16));

        // 2. Main Details Grid
        JPanel grid = new JPanel(new GridLayout(6, 2, 12, 12));
        grid.setBackground(Color.WHITE);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);

        titleField = new ModernTextField();
        grid.add(createLabel("Item Title / Name *"));
        grid.add(titleField);

        categoryCombo = new ModernComboBox<>();
        loadCategories();
        grid.add(createLabel("Category *"));
        grid.add(categoryCombo);

        locationCombo = new ModernComboBox<>(AppConfig.EDITABLE_CAMPUS_LOCATIONS);
        grid.add(createLabel("Campus Location *"));
        grid.add(locationCombo);

        dateField = new ModernTextField();
        dateField.setText(DateUtil.formatIsoDate(LocalDate.now()));
        grid.add(createLabel("Date (YYYY-MM-DD) *"));
        grid.add(dateField);

        User curUser = SessionManager.getInstance().getCurrentUser();
        phoneField = new ModernTextField();
        phoneField.setText(curUser != null && curUser.getPhone() != null ? curUser.getPhone() : "");
        grid.add(createLabel("Contact Phone"));
        grid.add(phoneField);

        emailField = new ModernTextField();
        emailField.setText(curUser != null && curUser.getEmail() != null ? curUser.getEmail() : "");
        grid.add(createLabel("Contact Email"));
        grid.add(emailField);

        form.add(grid);
        form.add(Box.createVerticalStrut(14));

        // 3. Description
        JLabel descLabel = createLabel("Detailed Description (color, brand, distinguishing marks) *");
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(descLabel);
        form.add(Box.createVerticalStrut(6));

        descArea = new JTextArea(3, 20);
        descArea.setFont(Theme.FONT_BODY);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(descScroll);
        form.add(Box.createVerticalStrut(14));

        // 4. Lost Specific Panel
        lostSpecificPanel = new JPanel(new GridLayout(2, 2, 12, 10));
        lostSpecificPanel.setBackground(Color.WHITE);
        lostSpecificPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rewardField = new ModernTextField();
        rewardField.setText("0.0");
        lastSeenField = new ModernTextField();
        lostSpecificPanel.add(createLabel("Optional Reward ($)"));
        lostSpecificPanel.add(rewardField);
        lostSpecificPanel.add(createLabel("Last Seen Specifics"));
        lostSpecificPanel.add(lastSeenField);

        // 5. Found Specific Panel
        foundSpecificPanel = new JPanel(new GridLayout(2, 2, 12, 10));
        foundSpecificPanel.setBackground(Color.WHITE);
        foundSpecificPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        storageLocField = new ModernTextField();
        storageLocField.setText("Campus Security Desk");
        verifQuestionField = new ModernTextField();
        foundSpecificPanel.add(createLabel("Custody / Handover Location *"));
        foundSpecificPanel.add(storageLocField);
        foundSpecificPanel.add(createLabel("Verification Question (to confirm owner)"));
        foundSpecificPanel.add(verifQuestionField);

        form.add(lostSpecificPanel);
        form.add(foundSpecificPanel);
        form.add(Box.createVerticalStrut(14));

        // Toggle visibility based on selected radio
        lostRadio.addActionListener(e -> updateTypeSpecificFields());
        foundRadio.addActionListener(e -> updateTypeSpecificFields());
        updateTypeSpecificFields();

        // 6. Image Attachment
        JPanel imgPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        imgPanel.setBackground(Color.WHITE);
        imgPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        imagePreviewLabel = new JLabel(ImageUtil.createPlaceholderIcon(64, 64));
        ModernButton chooseImgBtn = new ModernButton("Upload Photo...", ModernButton.Style.SECONDARY);
        chooseImgBtn.addActionListener(e -> chooseAndAttachImage());

        imgPanel.add(imagePreviewLabel);
        imgPanel.add(chooseImgBtn);

        form.add(createLabel("Item Photo / Evidence (Optional)"));
        form.add(Box.createVerticalStrut(6));
        form.add(imgPanel);

        JScrollPane mainScroll = new JScrollPane(form);
        mainScroll.setBorder(null);
        add(mainScroll, BorderLayout.CENTER);

        // Footer Actions
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER));

        ModernButton cancelBtn = new ModernButton("Cancel", ModernButton.Style.SECONDARY);
        cancelBtn.addActionListener(e -> dispose());

        ModernButton submitBtn = new ModernButton("Submit Report", ModernButton.Style.PRIMARY);
        submitBtn.addActionListener(e -> saveItem());

        footer.add(cancelBtn);
        footer.add(submitBtn);
        add(footer, BorderLayout.SOUTH);
    }

    private void updateTypeSpecificFields() {
        boolean isLost = lostRadio.isSelected();
        lostSpecificPanel.setVisible(isLost);
        foundSpecificPanel.setVisible(!isLost);
        revalidate();
        repaint();
    }

    private void loadCategories() {
        try {
            List<Category> list = itemService.getCategories();
            for (Category c : list) {
                categoryCombo.addItem(c);
            }
        } catch (DatabaseException e) {
            JOptionPane.showMessageDialog(this, "Failed to load categories: " + e.getMessage());
        }
    }

    private void chooseAndAttachImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Item Image");
        int res = chooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File selected = chooser.getSelectedFile();
            try {
                savedImageFileName = ImageUtil.saveImageToApp(selected);
                imagePreviewLabel.setIcon(ImageUtil.loadScaledImage(savedImageFileName, 64, 64));
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Could not save image: " + ex.getMessage(), "Image Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.FONT_BODY_BOLD);
        label.setForeground(Theme.TEXT_PRIMARY);
        return label;
    }

    private void saveItem() {
        User curUser = SessionManager.getInstance().getCurrentUser();
        if (curUser == null) {
            JOptionPane.showMessageDialog(this, "Please log in first.");
            return;
        }

        Category cat = (Category) categoryCombo.getSelectedItem();
        int catId = cat != null ? cat.getId() : 0;
        String location = (String) locationCombo.getSelectedItem();
        LocalDate reportDate = DateUtil.parseDate(dateField.getText());

        BaseItem item;
        if (lostRadio.isSelected()) {
            LostItem lost = new LostItem();
            double rew = 0.0;
            try {
                rew = Double.parseDouble(rewardField.getText().trim());
            } catch (NumberFormatException ignored) {}
            lost.setRewardAmount(rew);
            lost.setLastSeenDetails(lastSeenField.getText().trim());
            item = lost;
        } else {
            FoundItem found = new FoundItem();
            found.setStorageLocation(storageLocField.getText().trim());
            found.setVerificationQuestion(verifQuestionField.getText().trim());
            item = found;
        }

        item.setTitle(titleField.getText().trim());
        item.setDescription(descArea.getText().trim());
        item.setCategoryId(catId);
        item.setLocation(location);
        item.setDateReported(reportDate);
        item.setContactPhone(phoneField.getText().trim());
        item.setContactEmail(emailField.getText().trim());
        item.setImageUrl(savedImageFileName);
        item.setReportedByUserId(curUser.getId());
        item.setStatus(ItemStatus.OPEN);

        try {
            itemService.reportItem(item);
            JOptionPane.showMessageDialog(this,
                "Your " + item.getItemType().getDisplayName().toLowerCase() + " item report has been published!\n" +
                "The Smart Matching Engine is actively scanning for matches.",
                "Report Submitted", JOptionPane.INFORMATION_MESSAGE);

            if (onSavedCallback != null) {
                onSavedCallback.run();
            }
            dispose();
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (DatabaseException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
