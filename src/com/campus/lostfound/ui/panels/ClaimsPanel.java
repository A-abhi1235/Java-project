package com.campus.lostfound.ui.panels;

import com.campus.lostfound.exception.DatabaseException;
import com.campus.lostfound.model.Claim;
import com.campus.lostfound.model.ClaimStatus;
import com.campus.lostfound.model.User;
import com.campus.lostfound.service.ClaimService;
import com.campus.lostfound.service.ItemService;
import com.campus.lostfound.util.DateUtil;
import com.campus.lostfound.util.SessionManager;
import com.campus.lostfound.ui.components.CardPanel;
import com.campus.lostfound.ui.components.CustomTableRenderer;
import com.campus.lostfound.ui.components.ModernButton;
import com.campus.lostfound.ui.theme.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ClaimsPanel extends JPanel {
    private final Frame parentFrame;
    private final ClaimService claimService;
    private final ItemService itemService;

    private JTabbedPane tabbedPane;

    // Incoming Claims Table
    private JTable incomingTable;
    private DefaultTableModel incomingModel;
    private List<Claim> incomingClaims = new ArrayList<>();

    // My Submitted Claims Table
    private JTable myClaimsTable;
    private DefaultTableModel myClaimsModel;
    private List<Claim> mySubmittedClaims = new ArrayList<>();

    // All Claims (Admin only)
    private JTable allClaimsTable;
    private DefaultTableModel allClaimsModel;
    private List<Claim> allClaims = new ArrayList<>();

    public ClaimsPanel(Frame parentFrame, ClaimService claimService, ItemService itemService) {
        super(new BorderLayout());
        this.parentFrame = parentFrame;
        this.claimService = claimService;
        this.itemService = itemService;
        setBackground(Theme.BG_MAIN);
        initUI();
    }

    private void initUI() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.BG_MAIN);
        header.setBorder(new EmptyBorder(20, 24, 16, 24));

        JPanel titleGrp = new JPanel();
        titleGrp.setLayout(new BoxLayout(titleGrp, BoxLayout.Y_AXIS));
        titleGrp.setBackground(Theme.BG_MAIN);

        JLabel titleLbl = new JLabel("Claims & Belonging Verification");
        titleLbl.setFont(Theme.FONT_HEADER_LARGE);
        titleLbl.setForeground(Theme.PRIMARY_DARK);

        JLabel subLbl = new JLabel("Review ownership proofs submitted by students or track claims you have made.");
        subLbl.setFont(Theme.FONT_BODY);
        subLbl.setForeground(Theme.TEXT_SECONDARY);

        titleGrp.add(titleLbl);
        titleGrp.add(Box.createVerticalStrut(4));
        titleGrp.add(subLbl);
        header.add(titleGrp, BorderLayout.WEST);

        add(header, BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(Theme.FONT_BODY_BOLD);
        tabbedPane.setBackground(Color.WHITE);

        // Tab 1: Incoming Claims
        JPanel incomingPanel = createIncomingClaimsTab();
        tabbedPane.addTab("Incoming Claims (On My Items)", incomingPanel);

        // Tab 2: My Submitted Claims
        JPanel myClaimsPanel = createMyClaimsTab();
        tabbedPane.addTab("My Submitted Claims", myClaimsPanel);

        // Tab 3: Admin audit (if admin)
        User curUser = SessionManager.getInstance().getCurrentUser();
        if (curUser != null && curUser.isAdmin()) {
            JPanel adminClaimsPanel = createAllClaimsTab();
            tabbedPane.addTab("Admin - All Campus Claims", adminClaimsPanel);
        }

        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setBackground(Theme.BG_MAIN);
        centerWrapper.setBorder(new EmptyBorder(0, 24, 16, 24));
        centerWrapper.add(tabbedPane, BorderLayout.CENTER);

        add(centerWrapper, BorderLayout.CENTER);
        refreshData();
    }

    private JPanel createIncomingClaimsTab() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Theme.BG_MAIN);

        String[] cols = {"Claim ID", "Item Title", "Claimant Name", "Contact", "Proof Submitted", "Status", "Date Submitted"};
        incomingModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
            @Override public Class<?> getColumnClass(int col) {
                if (col == 5) return ClaimStatus.class;
                return String.class;
            }
        };

        incomingTable = new JTable(incomingModel);
        CustomTableRenderer.applyStyling(incomingTable);
        incomingTable.getColumnModel().getColumn(0).setMaxWidth(70);
        incomingTable.getColumnModel().getColumn(5).setMaxWidth(130);

        CardPanel card = new CardPanel(12, 4);
        card.setLayout(new BorderLayout());
        card.add(new JScrollPane(incomingTable), BorderLayout.CENTER);
        p.add(card, BorderLayout.CENTER);

        JPanel acts = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        acts.setBackground(Color.WHITE);

        ModernButton approveBtn = new ModernButton("Approve Claim", ModernButton.Style.SUCCESS);
        approveBtn.addActionListener(e -> processClaimAction(incomingTable, incomingClaims, true));

        ModernButton rejectBtn = new ModernButton("Reject Claim", ModernButton.Style.DANGER);
        rejectBtn.addActionListener(e -> processClaimAction(incomingTable, incomingClaims, false));

        ModernButton refreshBtn = new ModernButton("Refresh", ModernButton.Style.SECONDARY);
        refreshBtn.addActionListener(e -> refreshData());

        acts.add(refreshBtn);
        acts.add(rejectBtn);
        acts.add(approveBtn);
        p.add(acts, BorderLayout.SOUTH);

        return p;
    }

    private JPanel createMyClaimsTab() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Theme.BG_MAIN);

        String[] cols = {"Claim ID", "Item Title", "Proof Details", "Status", "Reviewer Notes", "Date"};
        myClaimsModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
            @Override public Class<?> getColumnClass(int col) {
                if (col == 3) return ClaimStatus.class;
                return String.class;
            }
        };

        myClaimsTable = new JTable(myClaimsModel);
        CustomTableRenderer.applyStyling(myClaimsTable);
        myClaimsTable.getColumnModel().getColumn(0).setMaxWidth(70);
        myClaimsTable.getColumnModel().getColumn(3).setMaxWidth(130);

        CardPanel card = new CardPanel(12, 4);
        card.setLayout(new BorderLayout());
        card.add(new JScrollPane(myClaimsTable), BorderLayout.CENTER);
        p.add(card, BorderLayout.CENTER);

        JPanel acts = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        acts.setBackground(Color.WHITE);

        ModernButton refreshBtn = new ModernButton("Refresh", ModernButton.Style.SECONDARY);
        refreshBtn.addActionListener(e -> refreshData());
        acts.add(refreshBtn);
        p.add(acts, BorderLayout.SOUTH);

        return p;
    }

    private JPanel createAllClaimsTab() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Theme.BG_MAIN);

        String[] cols = {"Claim ID", "Item ID", "Item Title", "Claimant", "Proof", "Status", "Reviewer Notes", "Date"};
        allClaimsModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
            @Override public Class<?> getColumnClass(int col) {
                if (col == 5) return ClaimStatus.class;
                return String.class;
            }
        };

        allClaimsTable = new JTable(allClaimsModel);
        CustomTableRenderer.applyStyling(allClaimsTable);
        allClaimsTable.getColumnModel().getColumn(0).setMaxWidth(70);
        allClaimsTable.getColumnModel().getColumn(1).setMaxWidth(70);
        allClaimsTable.getColumnModel().getColumn(5).setMaxWidth(130);

        CardPanel card = new CardPanel(12, 4);
        card.setLayout(new BorderLayout());
        card.add(new JScrollPane(allClaimsTable), BorderLayout.CENTER);
        p.add(card, BorderLayout.CENTER);

        JPanel acts = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        acts.setBackground(Color.WHITE);

        ModernButton approveBtn = new ModernButton("Admin Approve", ModernButton.Style.SUCCESS);
        approveBtn.addActionListener(e -> processClaimAction(allClaimsTable, allClaims, true));

        ModernButton rejectBtn = new ModernButton("Admin Reject", ModernButton.Style.DANGER);
        rejectBtn.addActionListener(e -> processClaimAction(allClaimsTable, allClaims, false));

        acts.add(rejectBtn);
        acts.add(approveBtn);
        p.add(acts, BorderLayout.SOUTH);

        return p;
    }

    private void processClaimAction(JTable table, List<Claim> claimList, boolean approve) {
        int row = table.getSelectedRow();
        if (row < 0 || row >= claimList.size()) {
            JOptionPane.showMessageDialog(this, "Please select a claim row first.");
            return;
        }

        Claim claim = claimList.get(row);
        if (claim.getStatus() != ClaimStatus.PENDING) {
            JOptionPane.showMessageDialog(this, "This claim has already been " + claim.getStatus().name().toLowerCase() + ".");
            return;
        }

        User curUser = SessionManager.getInstance().getCurrentUser();
        if (curUser == null) return;

        String note = JOptionPane.showInputDialog(this,
            approve ? "Enter acceptance / pickup instructions for claimant:" : "Enter reason for rejection:",
            approve ? "Approve Claim #" + claim.getId() : "Reject Claim #" + claim.getId(),
            JOptionPane.QUESTION_MESSAGE);

        if (note == null) return; // cancelled

        try {
            if (approve) {
                claimService.approveClaim(claim.getId(), curUser.getId(), note, curUser.isAdmin());
                JOptionPane.showMessageDialog(this, "Claim approved successfully!");
            } else {
                claimService.rejectClaim(claim.getId(), curUser.getId(), note, curUser.isAdmin());
                JOptionPane.showMessageDialog(this, "Claim rejected.");
            }
            refreshData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void refreshData() {
        User curUser = SessionManager.getInstance().getCurrentUser();
        if (curUser == null) return;

        try {
            // Incoming
            incomingClaims = claimService.getIncomingClaimsForUser(curUser.getId());
            incomingModel.setRowCount(0);
            for (Claim c : incomingClaims) {
                incomingModel.addRow(new Object[]{
                    c.getId(),
                    c.getItemTitle(),
                    c.getClaimantName(),
                    c.getClaimantPhone() != null ? c.getClaimantPhone() : c.getClaimantEmail(),
                    c.getProofDetails(),
                    c.getStatus(),
                    DateUtil.formatDateTime(c.getCreatedAt())
                });
            }

            // My claims
            mySubmittedClaims = claimService.getUserClaims(curUser.getId());
            myClaimsModel.setRowCount(0);
            for (Claim c : mySubmittedClaims) {
                myClaimsModel.addRow(new Object[]{
                    c.getId(),
                    c.getItemTitle(),
                    c.getProofDetails(),
                    c.getStatus(),
                    c.getReviewerNotes() != null ? c.getReviewerNotes() : "Under review",
                    DateUtil.formatDateTime(c.getCreatedAt())
                });
            }

            // Admin
            if (curUser.isAdmin()) {
                allClaims = claimService.getAllClaims();
                allClaimsModel.setRowCount(0);
                for (Claim c : allClaims) {
                    allClaimsModel.addRow(new Object[]{
                        c.getId(),
                        c.getItemId(),
                        c.getItemTitle(),
                        c.getClaimantName(),
                        c.getProofDetails(),
                        c.getStatus(),
                        c.getReviewerNotes(),
                        DateUtil.formatDateTime(c.getCreatedAt())
                    });
                }
            }
        } catch (DatabaseException e) {
            System.err.println("Error refreshing claims: " + e.getMessage());
        }
    }
}
