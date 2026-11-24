package com.campus.lostfound.ui.panels;

import com.campus.lostfound.exception.DatabaseException;
import com.campus.lostfound.model.*;
import com.campus.lostfound.service.ClaimService;
import com.campus.lostfound.service.ItemService;
import com.campus.lostfound.util.DateUtil;
import com.campus.lostfound.util.ImageUtil;
import com.campus.lostfound.util.SessionManager;
import com.campus.lostfound.ui.components.CardPanel;
import com.campus.lostfound.ui.components.ModernButton;
import com.campus.lostfound.ui.components.StatusBadge;
import com.campus.lostfound.ui.theme.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class ItemDetailDialog extends JDialog {
    private final BaseItem item;
    private final ItemService itemService;
    private final ClaimService claimService;
    private final Runnable onActionCompleteCallback;

    public ItemDetailDialog(Frame parent, BaseItem item, ItemService itemService,
                            ClaimService claimService, Runnable onActionCompleteCallback) {
        super(parent, "Item Details - " + item.getTitle(), true);
        this.item = item;
        this.itemService = itemService;
        this.claimService = claimService;
        this.onActionCompleteCallback = onActionCompleteCallback;
        initUI();
    }

    private void initUI() {
        setSize(700, 680);
        setLocationRelativeTo(getParent());
        getContentPane().setBackground(Theme.BG_MAIN);
        setLayout(new BorderLayout());

        // Header Panel
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(16, 24, 16, 24));

        JPanel titleAndBadges = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titleAndBadges.setBackground(Color.WHITE);

        StatusBadge typeBadge = new StatusBadge();
        typeBadge.setItemType(item.getItemType());

        StatusBadge statusBadge = new StatusBadge();
        statusBadge.setItemStatus(item.getStatus());

        JLabel titleLbl = new JLabel(item.getTitle());
        titleLbl.setFont(Theme.FONT_HEADER);
        titleLbl.setForeground(Theme.PRIMARY_DARK);

        titleAndBadges.add(typeBadge);
        titleAndBadges.add(statusBadge);
        titleAndBadges.add(titleLbl);

        header.add(titleAndBadges, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        // Body Content
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(Theme.BG_MAIN);
        body.setBorder(new EmptyBorder(20, 24, 20, 24));

        // Top info split: Left Photo, Right Key Specs
        JPanel topSplit = new CardPanel(12, 16);
        topSplit.setLayout(new BorderLayout(20, 0));
        topSplit.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));

        JLabel photoLabel = new JLabel(ImageUtil.loadScaledImage(item.getImageUrl(), 160, 150));
        photoLabel.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        topSplit.add(photoLabel, BorderLayout.WEST);

        JPanel specs = new JPanel(new GridLayout(5, 2, 8, 6));
        specs.setBackground(Color.WHITE);

        addSpecRow(specs, "Category:", item.getCategoryName());
        addSpecRow(specs, "Campus Location:", item.getLocation());
        addSpecRow(specs, "Date Reported:", DateUtil.formatDate(item.getDateReported()));
        addSpecRow(specs, item.getSpecificAttributeLabel() + ":", item.getSpecificAttributeValue());
        addSpecRow(specs, "Reported By:", item.getReportedByUserName());

        topSplit.add(specs, BorderLayout.CENTER);
        body.add(topSplit);
        body.add(Box.createVerticalStrut(16));

        // Description Card
        CardPanel descCard = new CardPanel(12, 16);
        descCard.setLayout(new BoxLayout(descCard, BoxLayout.Y_AXIS));

        JLabel descHead = new JLabel("Description & Distinguishing Features");
        descHead.setFont(Theme.FONT_SUBHEADER);
        descHead.setForeground(Theme.PRIMARY_DARK);

        JTextArea descText = new JTextArea(item.getDescription());
        descText.setFont(Theme.FONT_BODY);
        descText.setForeground(Theme.TEXT_PRIMARY);
        descText.setEditable(false);
        descText.setLineWrap(true);
        descText.setWrapStyleWord(true);
        descText.setBackground(Color.WHITE);
        descText.setBorder(new EmptyBorder(8, 0, 8, 0));

        descCard.add(descHead);
        descCard.add(descText);

        // Contact info section
        JPanel contactSec = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        contactSec.setBackground(Color.WHITE);
        if (item.getContactPhone() != null && !item.getContactPhone().isEmpty()) {
            contactSec.add(new JLabel("📞 " + item.getContactPhone()));
        }
        if (item.getContactEmail() != null && !item.getContactEmail().isEmpty()) {
            contactSec.add(new JLabel("✉ " + item.getContactEmail()));
        }
        descCard.add(contactSec);

        body.add(descCard);
        body.add(Box.createVerticalStrut(16));

        // Existing Claims / Activity for Admin or Owner
        User curUser = SessionManager.getInstance().getCurrentUser();
        boolean isOwner = curUser != null && curUser.getId() == item.getReportedByUserId();
        boolean isAdmin = curUser != null && curUser.isAdmin();

        if (isOwner || isAdmin) {
            CardPanel claimsCard = new CardPanel(12, 16);
            claimsCard.setLayout(new BoxLayout(claimsCard, BoxLayout.Y_AXIS));

            JLabel claimSecHead = new JLabel("Active Claims on this Item");
            claimSecHead.setFont(Theme.FONT_SUBHEADER);
            claimSecHead.setForeground(Theme.PRIMARY_DARK);
            claimsCard.add(claimSecHead);
            claimsCard.add(Box.createVerticalStrut(8));

            try {
                List<Claim> claims = claimService.getClaimsForItem(item.getId());
                if (claims.isEmpty()) {
                    JLabel noneLbl = new JLabel("No claims submitted yet.");
                    noneLbl.setFont(Theme.FONT_BODY);
                    noneLbl.setForeground(Theme.TEXT_MUTED);
                    claimsCard.add(noneLbl);
                } else {
                    for (Claim c : claims) {
                        claimsCard.add(createClaimRow(c, isOwner || isAdmin));
                        claimsCard.add(Box.createVerticalStrut(6));
                    }
                }
            } catch (DatabaseException e) {
                claimsCard.add(new JLabel("Could not load claims: " + e.getMessage()));
            }

            body.add(claimsCard);
        }

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);

        // Footer Actions
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER));

        ModernButton matchBtn = new ModernButton("Smart Match Radar", ModernButton.Style.OUTLINE);
        matchBtn.addActionListener(e -> {
            SmartMatchesDialog smd = new SmartMatchesDialog(this, item, itemService, claimService, onActionCompleteCallback);
            smd.setVisible(true);
        });
        footer.add(matchBtn);

        // Claim button (allowed if not owner, and item is open)
        if (curUser != null && !isOwner && (item.isOpen() || item.getStatus() == ItemStatus.CLAIM_PENDING)) {
            ModernButton claimBtn = new ModernButton("Submit Claim / Recover", ModernButton.Style.SUCCESS);
            claimBtn.addActionListener(e -> {
                ClaimDialog cd = new ClaimDialog(this, item, claimService, () -> {
                    if (onActionCompleteCallback != null) onActionCompleteCallback.run();
                    dispose();
                });
                cd.setVisible(true);
            });
            footer.add(claimBtn);
        }

        ModernButton closeBtn = new ModernButton("Close", ModernButton.Style.SECONDARY);
        closeBtn.addActionListener(e -> dispose());
        footer.add(closeBtn);

        add(footer, BorderLayout.SOUTH);
    }

    private void addSpecRow(JPanel panel, String label, String value) {
        JLabel l = new JLabel(label);
        l.setFont(Theme.FONT_BODY_BOLD);
        l.setForeground(Theme.TEXT_SECONDARY);

        JLabel v = new JLabel(value != null && !value.isEmpty() ? value : "N/A");
        v.setFont(Theme.FONT_BODY);
        v.setForeground(Theme.TEXT_PRIMARY);

        panel.add(l);
        panel.add(v);
    }

    private JPanel createClaimRow(Claim c, boolean canReview) {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setBackground(new Color(248, 250, 252));
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER),
            new EmptyBorder(8, 10, 8, 10)
        ));

        StatusBadge badge = new StatusBadge();
        badge.setClaimStatus(c.getStatus());

        JLabel info = new JLabel("Claimant: " + c.getClaimantName() + " | Proof: " + c.getProofDetails());
        info.setFont(Theme.FONT_BODY);

        p.add(badge, BorderLayout.WEST);
        p.add(info, BorderLayout.CENTER);

        if (canReview && c.getStatus() == ClaimStatus.PENDING) {
            JPanel acts = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
            acts.setBackground(p.getBackground());

            JButton accept = new JButton("Approve");
            accept.setFont(Theme.FONT_SMALL_BOLD);
            accept.setBackground(Theme.SUCCESS);
            accept.setForeground(Color.WHITE);
            accept.addActionListener(e -> reviewClaim(c, true));

            JButton reject = new JButton("Reject");
            reject.setFont(Theme.FONT_SMALL_BOLD);
            reject.setBackground(Theme.DANGER);
            reject.setForeground(Color.WHITE);
            reject.addActionListener(e -> reviewClaim(c, false));

            acts.add(accept);
            acts.add(reject);
            p.add(acts, BorderLayout.EAST);
        }

        return p;
    }

    private void reviewClaim(Claim c, boolean approve) {
        User curUser = SessionManager.getInstance().getCurrentUser();
        if (curUser == null) return;

        String notes = JOptionPane.showInputDialog(this,
            (approve ? "Acceptance instructions / notes:" : "Reason for rejection:"),
            approve ? "Approve Claim" : "Reject Claim",
            JOptionPane.QUESTION_MESSAGE);

        if (notes == null) return; // user cancelled

        try {
            if (approve) {
                claimService.approveClaim(c.getId(), curUser.getId(), notes, curUser.isAdmin());
                JOptionPane.showMessageDialog(this, "Claim approved! Item status updated to RETURNED.");
            } else {
                claimService.rejectClaim(c.getId(), curUser.getId(), notes, curUser.isAdmin());
                JOptionPane.showMessageDialog(this, "Claim has been rejected.");
            }
            if (onActionCompleteCallback != null) onActionCompleteCallback.run();
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error processing claim: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
