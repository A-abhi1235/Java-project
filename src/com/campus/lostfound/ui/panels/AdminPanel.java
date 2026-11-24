package com.campus.lostfound.ui.panels;

import com.campus.lostfound.exception.DatabaseException;
import com.campus.lostfound.model.Role;
import com.campus.lostfound.model.User;
import com.campus.lostfound.service.AuthService;
import com.campus.lostfound.service.ReportService;
import com.campus.lostfound.ui.components.CardPanel;
import com.campus.lostfound.ui.components.CustomTableRenderer;
import com.campus.lostfound.ui.components.ModernButton;
import com.campus.lostfound.ui.theme.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.Map;

public class AdminPanel extends JPanel {
    private final Frame parentFrame;
    private final AuthService authService;
    private final ReportService reportService;

    private JLabel totalUsersVal;
    private JLabel totalItemsVal;
    private JLabel recoveryRateVal;
    private JLabel activeClaimsVal;

    private JTable usersTable;
    private DefaultTableModel userModel;
    private List<User> userList;

    public AdminPanel(Frame parentFrame, AuthService authService, ReportService reportService) {
        super(new BorderLayout());
        this.parentFrame = parentFrame;
        this.authService = authService;
        this.reportService = reportService;
        setBackground(Theme.BG_MAIN);
        initUI();
    }

    private void initUI() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Theme.BG_MAIN);
        content.setBorder(new EmptyBorder(24, 28, 24, 28));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.BG_MAIN);

        JLabel titleLbl = new JLabel("Campus Security Administration");
        titleLbl.setFont(Theme.FONT_HEADER_LARGE);
        titleLbl.setForeground(Theme.PRIMARY_DARK);

        JLabel subLbl = new JLabel("Audit system statistics, manage campus users, and export official reports.");
        subLbl.setFont(Theme.FONT_BODY);
        subLbl.setForeground(Theme.TEXT_SECONDARY);

        header.add(titleLbl, BorderLayout.NORTH);
        header.add(subLbl, BorderLayout.SOUTH);
        content.add(header);
        content.add(Box.createVerticalStrut(18));

        // 1. Stats Row
        JPanel statsGrid = new JPanel(new GridLayout(1, 4, 14, 0));
        statsGrid.setBackground(Theme.BG_MAIN);
        statsGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        totalUsersVal = new JLabel("0");
        totalItemsVal = new JLabel("0");
        recoveryRateVal = new JLabel("0%");
        activeClaimsVal = new JLabel("0");

        statsGrid.add(createStatCard("Registered Users", totalUsersVal, Theme.PRIMARY));
        statsGrid.add(createStatCard("Total System Items", totalItemsVal, Theme.INFO));
        statsGrid.add(createStatCard("Recovery Success Rate", recoveryRateVal, Theme.SUCCESS));
        statsGrid.add(createStatCard("Pending Claims", activeClaimsVal, Theme.WARNING));

        content.add(statsGrid);
        content.add(Box.createVerticalStrut(20));

        // 2. Data Export Card
        CardPanel exportCard = new CardPanel(12, 16);
        exportCard.setLayout(new BorderLayout(16, 0));
        exportCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JPanel exportText = new JPanel();
        exportText.setLayout(new BoxLayout(exportText, BoxLayout.Y_AXIS));
        exportText.setBackground(Color.WHITE);

        JLabel expHead = new JLabel("Export Audit Reports");
        expHead.setFont(Theme.FONT_SUBHEADER);
        expHead.setForeground(Theme.PRIMARY_DARK);

        JLabel expSub = new JLabel("Export items log and claim resolution records to standard CSV files for campus audit.");
        expSub.setFont(Theme.FONT_BODY);
        expSub.setForeground(Theme.TEXT_SECONDARY);

        exportText.add(expHead);
        exportText.add(expSub);
        exportCard.add(exportText, BorderLayout.CENTER);

        JPanel expBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        expBtns.setBackground(Color.WHITE);

        ModernButton exportItemsBtn = new ModernButton("Export Items CSV", ModernButton.Style.PRIMARY);
        exportItemsBtn.addActionListener(e -> exportItems());

        ModernButton exportClaimsBtn = new ModernButton("Export Claims CSV", ModernButton.Style.SECONDARY);
        exportClaimsBtn.addActionListener(e -> exportClaims());

        expBtns.add(exportItemsBtn);
        expBtns.add(exportClaimsBtn);
        exportCard.add(expBtns, BorderLayout.EAST);

        content.add(exportCard);
        content.add(Box.createVerticalStrut(20));

        // 3. User Accounts Management Table
        CardPanel userTableCard = new CardPanel(12, 16);
        userTableCard.setLayout(new BorderLayout(0, 10));

        JLabel userTblHead = new JLabel("Registered Campus Users");
        userTblHead.setFont(Theme.FONT_SUBHEADER);
        userTblHead.setForeground(Theme.PRIMARY_DARK);
        userTableCard.add(userTblHead, BorderLayout.NORTH);

        String[] cols = {"User ID", "Full Name", "Username", "Email", "Phone", "Campus ID", "Role"};
        userModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        usersTable = new JTable(userModel);
        CustomTableRenderer.applyStyling(usersTable);
        usersTable.getColumnModel().getColumn(0).setMaxWidth(70);

        JScrollPane scroll = new JScrollPane(usersTable);
        scroll.setPreferredSize(new Dimension(800, 240));
        userTableCard.add(scroll, BorderLayout.CENTER);

        JPanel userActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        userActions.setBackground(Color.WHITE);

        ModernButton refreshBtn = new ModernButton("Refresh Users", ModernButton.Style.SECONDARY);
        refreshBtn.addActionListener(e -> refreshData());
        userActions.add(refreshBtn);

        userTableCard.add(userActions, BorderLayout.SOUTH);
        content.add(userTableCard);

        JScrollPane mainScroll = new JScrollPane(content);
        mainScroll.setBorder(null);
        add(mainScroll, BorderLayout.CENTER);

        refreshData();
    }

    private JPanel createStatCard(String title, JLabel val, Color color) {
        CardPanel card = new CardPanel(10, 14);
        card.setLayout(new BorderLayout(0, 4));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(Theme.FONT_SMALL_BOLD);
        titleLbl.setForeground(Theme.TEXT_SECONDARY);

        val.setFont(new Font("Segoe UI", Font.BOLD, 22));
        val.setForeground(color);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(val, BorderLayout.CENTER);
        return card;
    }

    private void exportItems() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("campus_items_report.csv"));
        int res = chooser.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            try {
                reportService.exportItemsToCsv(chooser.getSelectedFile());
                JOptionPane.showMessageDialog(this, "Items report successfully exported to:\n" + chooser.getSelectedFile().getAbsolutePath(),
                    "Export Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportClaims() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("campus_claims_report.csv"));
        int res = chooser.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            try {
                reportService.exportClaimsToCsv(chooser.getSelectedFile());
                JOptionPane.showMessageDialog(this, "Claims report successfully exported to:\n" + chooser.getSelectedFile().getAbsolutePath(),
                    "Export Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void refreshData() {
        try {
            Map<String, Object> stats = reportService.getSystemStatistics();
            totalUsersVal.setText(String.valueOf(stats.get("totalUsers")));
            totalItemsVal.setText(String.valueOf(stats.get("totalItems")));
            recoveryRateVal.setText(String.valueOf(stats.get("recoveryRate")));
            activeClaimsVal.setText(String.valueOf(stats.get("pendingClaims")));

            userList = authService.getAllUsers();
            userModel.setRowCount(0);
            for (User u : userList) {
                userModel.addRow(new Object[]{
                    u.getId(),
                    u.getFullName(),
                    u.getUsername(),
                    u.getEmail(),
                    u.getPhone() != null ? u.getPhone() : "N/A",
                    u.getStudentId() != null ? u.getStudentId() : "N/A",
                    u.getRole().getDisplayName()
                });
            }
        } catch (DatabaseException e) {
            System.err.println("Error refreshing admin panel: " + e.getMessage());
        }
    }
}
