package com.campus.lostfound.ui.panels;

import com.campus.lostfound.exception.DatabaseException;
import com.campus.lostfound.model.BaseItem;
import com.campus.lostfound.model.ItemType;
import com.campus.lostfound.model.MatchSuggestion;
import com.campus.lostfound.model.User;
import com.campus.lostfound.service.ClaimService;
import com.campus.lostfound.service.ItemService;
import com.campus.lostfound.util.SessionManager;
import com.campus.lostfound.ui.components.CardPanel;
import com.campus.lostfound.ui.components.ModernButton;
import com.campus.lostfound.ui.components.StatusBadge;
import com.campus.lostfound.ui.theme.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class SmartMatchesDialog extends JDialog {
    private final BaseItem targetItem;
    private final ItemService itemService;
    private final ClaimService claimService;
    private final Runnable onActionCallback;

    public SmartMatchesDialog(Window parent, BaseItem targetItem, ItemService itemService,
                              ClaimService claimService, Runnable onActionCallback) {
        super(parent, "Smart Match Radar", ModalityType.APPLICATION_MODAL);
        this.targetItem = targetItem;
        this.itemService = itemService;
        this.claimService = claimService;
        this.onActionCallback = onActionCallback;
        initUI();
    }

    public SmartMatchesDialog(Window parent, ItemService itemService, ClaimService claimService, Runnable onActionCallback) {
        this(parent, null, itemService, claimService, onActionCallback);
    }

    private void initUI() {
        setSize(780, 600);
        setLocationRelativeTo(getParent());
        getContentPane().setBackground(Theme.BG_MAIN);
        setLayout(new BorderLayout());

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(16, 24, 16, 24));

        JLabel titleLbl = new JLabel("Smart Match Radar");
        titleLbl.setFont(Theme.FONT_HEADER);
        titleLbl.setForeground(Theme.PRIMARY_DARK);

        String subtitleText = (targetItem != null)
            ? "Automated matches detected for: " + targetItem.getTitle()
            : "Campus-wide cross-matching between reported Lost & Found belongings";
        JLabel subLbl = new JLabel(subtitleText);
        subLbl.setFont(Theme.FONT_BODY);
        subLbl.setForeground(Theme.TEXT_SECONDARY);

        header.add(titleLbl, BorderLayout.NORTH);
        header.add(subLbl, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        // Match Results List
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(Theme.BG_MAIN);
        listPanel.setBorder(new EmptyBorder(16, 24, 16, 24));

        try {
            List<MatchSuggestion> matches = (targetItem != null)
                ? itemService.getSmartMatchesForItem(targetItem)
                : itemService.getAllSmartMatches();

            if (matches.isEmpty()) {
                JPanel emptyCard = new CardPanel(12, 32);
                JLabel noMatchLbl = new JLabel("No match candidates found with high confidence.", SwingConstants.CENTER);
                noMatchLbl.setFont(Theme.FONT_BODY_BOLD);
                noMatchLbl.setForeground(Theme.TEXT_MUTED);
                emptyCard.add(noMatchLbl, BorderLayout.CENTER);
                listPanel.add(emptyCard);
            } else {
                for (MatchSuggestion m : matches) {
                    listPanel.add(createMatchCard(m));
                    listPanel.add(Box.createVerticalStrut(12));
                }
            }
        } catch (DatabaseException e) {
            listPanel.add(new JLabel("Error querying matches: " + e.getMessage()));
        }

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 12));
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER));

        ModernButton closeBtn = new ModernButton("Close", ModernButton.Style.SECONDARY);
        closeBtn.addActionListener(e -> dispose());
        footer.add(closeBtn);

        add(footer, BorderLayout.SOUTH);
    }

    private JPanel createMatchCard(MatchSuggestion match) {
        CardPanel card = new CardPanel(12, 16);
        card.setLayout(new BorderLayout(16, 0));

        // Left confidence pill
        JPanel leftScore = new JPanel(new BorderLayout());
        leftScore.setBackground(Color.WHITE);
        leftScore.setPreferredSize(new Dimension(90, 70));

        JLabel scoreVal = new JLabel(match.getConfidenceScore() + "%", SwingConstants.CENTER);
        scoreVal.setFont(new Font("Segoe UI", Font.BOLD, 20));
        scoreVal.setForeground(match.getConfidenceScore() >= 70 ? Theme.SUCCESS : Theme.WARNING);

        JLabel scoreSub = new JLabel("Confidence", SwingConstants.CENTER);
        scoreSub.setFont(Theme.FONT_SMALL);
        scoreSub.setForeground(Theme.TEXT_MUTED);

        leftScore.add(scoreVal, BorderLayout.CENTER);
        leftScore.add(scoreSub, BorderLayout.SOUTH);
        card.add(leftScore, BorderLayout.WEST);

        // Middle comparison
        JPanel mid = new JPanel();
        mid.setLayout(new BoxLayout(mid, BoxLayout.Y_AXIS));
        mid.setBackground(Color.WHITE);

        JLabel lostLabel = new JLabel("LOST: " + match.getLostItem().getTitle() + " (" + match.getLostItem().getLocation() + ")");
        lostLabel.setFont(Theme.FONT_BODY_BOLD);
        lostLabel.setForeground(new Color(185, 28, 28));

        JLabel foundLabel = new JLabel("FOUND: " + match.getFoundItem().getTitle() + " (" + match.getFoundItem().getLocation() + ")");
        foundLabel.setFont(Theme.FONT_BODY_BOLD);
        foundLabel.setForeground(new Color(29, 78, 216));

        JLabel reasonLbl = new JLabel("Match Factors: " + match.getMatchReason());
        reasonLbl.setFont(Theme.FONT_SMALL);
        reasonLbl.setForeground(Theme.TEXT_SECONDARY);

        mid.add(lostLabel);
        mid.add(Box.createVerticalStrut(4));
        mid.add(foundLabel);
        mid.add(Box.createVerticalStrut(6));
        mid.add(reasonLbl);

        card.add(mid, BorderLayout.CENTER);

        // Right Action
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 16));
        right.setBackground(Color.WHITE);

        User currentUser = SessionManager.getInstance().getCurrentUser();
        boolean ownsLostReport = currentUser != null
            && match.getLostItem().getReportedByUserId() == currentUser.getId();
        boolean ownsFoundReport = currentUser != null
            && match.getFoundItem().getReportedByUserId() == currentUser.getId();
        if (ownsLostReport && !ownsFoundReport) {
            ModernButton claimBtn = new ModernButton("Claim Item", ModernButton.Style.PRIMARY);
            claimBtn.addActionListener(e -> {
                ClaimDialog cd = new ClaimDialog(this, match.getFoundItem(), claimService, onActionCallback);
                cd.setVisible(true);
            });
            right.add(claimBtn);
        }

        card.add(right, BorderLayout.EAST);
        return card;
    }
}
