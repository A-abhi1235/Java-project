package com.campus.lostfound.ui.panels;

import com.campus.lostfound.exception.DatabaseException;
import com.campus.lostfound.model.BaseItem;
import com.campus.lostfound.model.ItemStatus;
import com.campus.lostfound.model.ItemType;
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

public class MyItemsPanel extends JPanel {
    private final Frame parentFrame;
    private final ItemService itemService;
    private final ClaimService claimService;

    private JTable table;
    private DefaultTableModel tableModel;
    private List<BaseItem> myItems = new ArrayList<>();

    public MyItemsPanel(Frame parentFrame, ItemService itemService, ClaimService claimService) {
        super(new BorderLayout());
        this.parentFrame = parentFrame;
        this.itemService = itemService;
        this.claimService = claimService;
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

        JLabel titleLbl = new JLabel("My Reported Belongings");
        titleLbl.setFont(Theme.FONT_HEADER_LARGE);
        titleLbl.setForeground(Theme.PRIMARY_DARK);

        JLabel subLbl = new JLabel("Manage all items you have reported as Lost or Found.");
        subLbl.setFont(Theme.FONT_BODY);
        subLbl.setForeground(Theme.TEXT_SECONDARY);

        titleGrp.add(titleLbl);
        titleGrp.add(Box.createVerticalStrut(4));
        titleGrp.add(subLbl);
        header.add(titleGrp, BorderLayout.WEST);

        add(header, BorderLayout.NORTH);

        // Table
        String[] cols = {"ID", "Type", "Title", "Category", "Location", "Date Reported", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
            @Override public Class<?> getColumnClass(int col) {
                if (col == 1) return ItemType.class;
                if (col == 6) return ItemStatus.class;
                return String.class;
            }
        };

        table = new JTable(tableModel);
        CustomTableRenderer.applyStyling(table);

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(Theme.BG_MAIN);
        center.setBorder(new EmptyBorder(0, 24, 16, 24));

        CardPanel card = new CardPanel(12, 4);
        card.setLayout(new BorderLayout());
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        card.add(scroll, BorderLayout.CENTER);
        center.add(card, BorderLayout.CENTER);

        add(center, BorderLayout.CENTER);

        // Footer Actions
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER));

        ModernButton viewBtn = new ModernButton("View Details", ModernButton.Style.PRIMARY);
        viewBtn.addActionListener(e -> openDetails());

        ModernButton markReturnedBtn = new ModernButton("Mark as Returned / Resolved", ModernButton.Style.SUCCESS);
        markReturnedBtn.addActionListener(e -> markResolved());

        ModernButton deleteBtn = new ModernButton("Delete Report", ModernButton.Style.DANGER);
        deleteBtn.addActionListener(e -> deleteReport());

        ModernButton refreshBtn = new ModernButton("Refresh", ModernButton.Style.SECONDARY);
        refreshBtn.addActionListener(e -> refreshData());

        footer.add(refreshBtn);
        footer.add(markReturnedBtn);
        footer.add(deleteBtn);
        footer.add(viewBtn);

        add(footer, BorderLayout.SOUTH);
        refreshData();
    }

    public void refreshData() {
        User curUser = SessionManager.getInstance().getCurrentUser();
        if (curUser == null) return;

        try {
            myItems = itemService.getItemsByUser(curUser.getId());
            tableModel.setRowCount(0);
            for (BaseItem item : myItems) {
                tableModel.addRow(new Object[]{
                    item.getId(),
                    item.getItemType(),
                    item.getTitle(),
                    item.getCategoryName(),
                    item.getLocation(),
                    DateUtil.formatDate(item.getDateReported()),
                    item.getStatus()
                });
            }
        } catch (DatabaseException e) {
            System.err.println("Could not load user items: " + e.getMessage());
        }
    }

    private void openDetails() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= myItems.size()) {
            JOptionPane.showMessageDialog(this, "Please select an item first.");
            return;
        }
        ItemDetailDialog dlg = new ItemDetailDialog(parentFrame, myItems.get(row), itemService, claimService, this::refreshData);
        dlg.setVisible(true);
    }

    private void markResolved() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= myItems.size()) {
            JOptionPane.showMessageDialog(this, "Please select an item to mark as returned.");
            return;
        }

        BaseItem item = myItems.get(row);
        if (item.getStatus() == ItemStatus.CLAIM_PENDING) {
            JOptionPane.showMessageDialog(this,
                "This item has a claim under review. Approve or reject the claim before marking it resolved.",
                "Claim Needs Review", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (item.getStatus() == ItemStatus.RETURNED) {
            JOptionPane.showMessageDialog(this, "This item is already marked as returned.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
            "Mark '" + item.getTitle() + "' as returned/resolved?",
            "Confirm Resolution", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                itemService.updateStatus(item.getId(), ItemStatus.RETURNED);
                JOptionPane.showMessageDialog(this, "Item marked as RETURNED successfully!");
                refreshData();
            } catch (DatabaseException e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }

    private void deleteReport() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= myItems.size()) {
            JOptionPane.showMessageDialog(this, "Please select an item to delete.");
            return;
        }

        BaseItem item = myItems.get(row);
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to permanently delete '" + item.getTitle() + "'?",
            "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                itemService.deleteItem(item.getId(), user.getId(), user.isAdmin());
                JOptionPane.showMessageDialog(this, "Report deleted successfully.");
                refreshData();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Delete failed: " + e.getMessage());
            }
        }
    }
}
