package com.campus.lostfound.ui.panels;

import com.campus.lostfound.exception.DatabaseException;
import com.campus.lostfound.exception.ResourceNotFoundException;
import com.campus.lostfound.exception.ValidationException;
import com.campus.lostfound.model.BaseItem;
import com.campus.lostfound.model.FoundItem;
import com.campus.lostfound.model.User;
import com.campus.lostfound.service.ClaimService;
import com.campus.lostfound.util.SessionManager;
import com.campus.lostfound.ui.components.ModernButton;
import com.campus.lostfound.ui.theme.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ClaimDialog extends JDialog {
    private final BaseItem item;
    private final ClaimService claimService;
    private final Runnable onSuccessCallback;

    private JTextArea proofArea;

    public ClaimDialog(Window parent, BaseItem item, ClaimService claimService, Runnable onSuccessCallback) {
        super(parent, "Submit Ownership Claim - " + item.getTitle(), ModalityType.APPLICATION_MODAL);
        this.item = item;
        this.claimService = claimService;
        this.onSuccessCallback = onSuccessCallback;
        initUI();
    }

    private void initUI() {
        setSize(520, 480);
        setLocationRelativeTo(getParent());
        getContentPane().setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(238, 242, 255));
        header.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel titleLbl = new JLabel("Claim Belonging: " + item.getTitle());
        titleLbl.setFont(Theme.FONT_HEADER);
        titleLbl.setForeground(Theme.PRIMARY_DARK);

        JLabel infoLbl = new JLabel("Found at " + item.getLocation() + " (" + item.getCategoryName() + ")");
        infoLbl.setFont(Theme.FONT_BODY);
        infoLbl.setForeground(Theme.TEXT_SECONDARY);

        header.add(titleLbl, BorderLayout.NORTH);
        header.add(infoLbl, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(20, 20, 20, 20));

        // If found item has a verification question
        if (item instanceof FoundItem) {
            FoundItem found = (FoundItem) item;
            if (found.getVerificationQuestion() != null && !found.getVerificationQuestion().trim().isEmpty()) {
                JPanel qPanel = new JPanel(new BorderLayout(8, 4));
                qPanel.setBackground(new Color(254, 243, 199));
                qPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(245, 158, 11)),
                    new EmptyBorder(10, 12, 10, 12)
                ));
                qPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

                JLabel qHeader = new JLabel("Security Verification Question:");
                qHeader.setFont(Theme.FONT_BODY_BOLD);
                qHeader.setForeground(new Color(146, 64, 14));

                JLabel qText = new JLabel("<html><i>\"" + found.getVerificationQuestion() + "\"</i></html>");
                qText.setFont(Theme.FONT_BODY);
                qText.setForeground(Theme.TEXT_PRIMARY);

                qPanel.add(qHeader, BorderLayout.NORTH);
                qPanel.add(qText, BorderLayout.CENTER);

                content.add(qPanel);
                content.add(Box.createVerticalStrut(16));
            }
        }

        JLabel proofLabel = new JLabel("Proof of Ownership / Answer / Distinguishing Details:");
        proofLabel.setFont(Theme.FONT_BODY_BOLD);
        proofLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(proofLabel);
        content.add(Box.createVerticalStrut(6));

        JLabel helpLabel = new JLabel("<html>Describe unique features, stickers, serial numbers, password patterns, or contents to prove ownership.</html>");
        helpLabel.setFont(Theme.FONT_SMALL);
        helpLabel.setForeground(Theme.TEXT_SECONDARY);
        helpLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(helpLabel);
        content.add(Box.createVerticalStrut(8));

        proofArea = new JTextArea(6, 30);
        proofArea.setFont(Theme.FONT_BODY);
        proofArea.setLineWrap(true);
        proofArea.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(proofArea);
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(scroll);

        add(content, BorderLayout.CENTER);

        // Footer buttons
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER));

        ModernButton cancelBtn = new ModernButton("Cancel", ModernButton.Style.SECONDARY);
        cancelBtn.addActionListener(e -> dispose());

        ModernButton submitBtn = new ModernButton("Submit Claim", ModernButton.Style.PRIMARY);
        submitBtn.addActionListener(e -> submitClaim());

        footer.add(cancelBtn);
        footer.add(submitBtn);
        add(footer, BorderLayout.SOUTH);
    }

    private void submitClaim() {
        User curUser = SessionManager.getInstance().getCurrentUser();
        if (curUser == null) {
            JOptionPane.showMessageDialog(this, "Please sign in first.");
            return;
        }

        String proof = proofArea.getText().trim();
        try {
            claimService.submitClaim(item.getId(), curUser.getId(), proof);
            JOptionPane.showMessageDialog(this,
                "Claim submitted successfully!\n" +
                "The finder/admin will review your proof and you will receive a notification upon verification.",
                "Claim Sent", JOptionPane.INFORMATION_MESSAGE);

            if (onSuccessCallback != null) {
                onSuccessCallback.run();
            }
            dispose();
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (ResourceNotFoundException | DatabaseException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "System Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
