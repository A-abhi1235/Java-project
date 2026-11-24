package com.campus.lostfound.ui.panels;

import com.campus.lostfound.exception.DatabaseException;
import com.campus.lostfound.model.BaseItem;
import com.campus.lostfound.model.ItemStatus;
import com.campus.lostfound.model.ItemType;
import com.campus.lostfound.model.User;
import com.campus.lostfound.service.ClaimService;
import com.campus.lostfound.service.ItemService;
import com.campus.lostfound.service.ReportService;
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;

public class DashboardPanel extends JPanel {
    private final ItemService itemService;
    private final ClaimService claimService;
    private final ReportService reportService;
    private final Frame parentFrame;
    private final Runnable navigateToBrowse;

    private JLabel welcomeLabel;
    private JLabel totalItemsVal;
    private JLabel lostCountVal;
    private JLabel foundCountVal;
    private JLabel returnedCountVal;
    private JLabel recoveryRateVal;

    private JTable recentTable;
    private DefaultTableModel tableModel;
    private List<BaseItem> recentItemsList;

    public DashboardPanel(Frame parentFrame, ItemService itemService, ClaimService claimService,
                          ReportService reportService, Runnable navigateToBrowse) {
        super(new BorderLayout());
        this.parentFrame = parentFrame;
        this.itemService = itemService;
        this.claimService = claimService;
        this.reportService = reportService;
        this.navigateToBrowse = navigateToBrowse;
        setBackground(Theme.BG_MAIN);
        initUI();
    }

    private void initUI() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Theme.BG_MAIN);
        content.setBorder(new EmptyBorder(24, 28, 24, 28));

        // 1. Welcome Card Banner
        CardPanel banner = new CardPanel(12, 20);
        banner.setLayout(new BorderLayout());
        banner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        JPanel bannerText = new JPanel();
        bannerText.setLayout(new BoxLayout(bannerText, BoxLayout.Y_AXIS));
        bannerText.setBackground(Color.WHITE);

        welcomeLabel = new JLabel("Welcome to Campus Lost & Found");
        welcomeLabel.setFont(Theme.FONT_HEADER_LARGE);
        welcomeLabel.setForeground(Theme.PRIMARY_DARK);

        JLabel bannerSub = new JLabel("Helping students, faculty, and campus staff recover lost belongings quickly and securely.");
        bannerSub.setFont(Theme.FONT_BODY);
        bannerSub.setForeground(Theme.TEXT_SECONDARY);

        bannerText.add(welcomeLabel);
        bannerText.add(Box.createVerticalStrut(4));
        bannerText.add(bannerSub);
        banner.add(bannerText, BorderLayout.CENTER);

        content.add(banner);
        content.add(Box.createVerticalStrut(18));

        // 2. Metrics Cards Row
        JPanel statsGrid = new JPanel(new GridLayout(1, 5, 14, 0));
        statsGrid.setBackground(Theme.BG_MAIN);
        statsGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 95));

        totalItemsVal = new JLabel("0");
        lostCountVal = new JLabel("0");
        foundCountVal = new JLabel("0");
        returnedCountVal = new JLabel("0");
        recoveryRateVal = new JLabel("0%");

        statsGrid.add(createStatCard("Total Reported", totalItemsVal, Theme.PRIMARY));
        statsGrid.add(createStatCard("Active Lost", lostCountVal, Theme.DANGER));
        statsGrid.add(createStatCard("Active Found", foundCountVal, Theme.INFO));
        statsGrid.add(createStatCard("Recovered Items", returnedCountVal, Theme.SUCCESS));
        statsGrid.add(createStatCard("Recovery Rate", recoveryRateVal, Theme.PURPLE));

        content.add(statsGrid);
        content.add(Box.createVerticalStrut(20));

        // 3. Quick Action Buttons
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        actionRow.setBackground(Theme.BG_MAIN);
        actionRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        ModernButton reportLostBtn = new ModernButton("+ Report Lost Item", ModernButton.Style.DANGER);
        reportLostBtn.addActionListener(e -> {
            ReportItemDialog dlg = new ReportItemDialog(parentFrame, itemService, ItemType.LOST, this::refreshData);
            dlg.setVisible(true);
        });

        ModernButton reportFoundBtn = new ModernButton("+ Report Found Item", ModernButton.Style.PRIMARY);
        reportFoundBtn.addActionListener(e -> {
            ReportItemDialog dlg = new ReportItemDialog(parentFrame, itemService, ItemType.FOUND, this::refreshData);
            dlg.setVisible(true);
        });

        ModernButton smartRadarBtn = new ModernButton("Smart Match Radar", ModernButton.Style.OUTLINE);
        smartRadarBtn.addActionListener(e -> {
            SmartMatchesDialog dlg = new SmartMatchesDialog(parentFrame, itemService, claimService, this::refreshData);
            dlg.setVisible(true);
        });

        ModernButton browseAllBtn = new ModernButton("Browse All Belongings", ModernButton.Style.SECONDARY);
        browseAllBtn.addActionListener(e -> {
            if (navigateToBrowse != null) navigateToBrowse.run();
        });

        actionRow.add(reportLostBtn);
        actionRow.add(reportFoundBtn);
        actionRow.add(smartRadarBtn);
        actionRow.add(browseAllBtn);

        content.add(actionRow);
        content.add(Box.createVerticalStrut(20));

        // 4. Recent Items Table Card
        CardPanel tableCard = new CardPanel(12, 16);
        tableCard.setLayout(new BorderLayout(0, 10));

        JPanel tableHead = new JPanel(new BorderLayout());
        tableHead.setBackground(Color.WHITE);

        JLabel tblTitle = new JLabel("Recent Campus Items");
        tblTitle.setFont(Theme.FONT_SUBHEADER);
        tblTitle.setForeground(Theme.PRIMARY_DARK);

        JLabel hintLbl = new JLabel("Double-click any item to view details & submit claims");
        hintLbl.setFont(Theme.FONT_SMALL);
        hintLbl.setForeground(Theme.TEXT_MUTED);

        tableHead.add(tblTitle, BorderLayout.WEST);
        tableHead.add(hintLbl, BorderLayout.EAST);
        tableCard.add(tableHead, BorderLayout.NORTH);

        String[] cols = {"Type", "Title", "Category", "Location", "Date Reported", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
            @Override
            public Class<?> getColumnClass(int col) {
                if (col == 0) return ItemType.class;
                if (col == 5) return ItemStatus.class;
                return String.class;
            }
        };

        recentTable = new JTable(tableModel);
        CustomTableRenderer.applyStyling(recentTable);
        recentTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && recentTable.getSelectedRow() >= 0) {
                    int row = recentTable.getSelectedRow();
                    if (recentItemsList != null && row < recentItemsList.size()) {
                        BaseItem item = recentItemsList.get(row);
                        ItemDetailDialog dlg = new ItemDetailDialog(parentFrame, item, itemService, claimService, DashboardPanel.this::refreshData);
                        dlg.setVisible(true);
                    }
                }
            }
        });

        JScrollPane tableScroll = new JScrollPane(recentTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        tableScroll.setPreferredSize(new Dimension(800, 260));
        tableCard.add(tableScroll, BorderLayout.CENTER);

        content.add(tableCard);

        JScrollPane mainScroll = new JScrollPane(content);
        mainScroll.setBorder(null);
        add(mainScroll, BorderLayout.CENTER);

        refreshData();
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color accentColor) {
        CardPanel card = new CardPanel(10, 14);
        card.setLayout(new BorderLayout(0, 4));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(Theme.FONT_SMALL_BOLD);
        titleLbl.setForeground(Theme.TEXT_SECONDARY);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(accentColor);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    public void refreshData() {
        User curUser = SessionManager.getInstance().getCurrentUser();
        if (curUser != null) {
            welcomeLabel.setText("Welcome back, " + curUser.getFullName() + "!");
        } else {
            welcomeLabel.setText("Welcome to Campus Lost & Found");
        }

        // Refresh stats
        try {
            Map<String, Object> stats = reportService.getSystemStatistics();
            totalItemsVal.setText(String.valueOf(stats.get("totalItems")));
            lostCountVal.setText(String.valueOf(stats.get("lostCount")));
            foundCountVal.setText(String.valueOf(stats.get("foundCount")));
            returnedCountVal.setText(String.valueOf(stats.get("returnedCount")));
            recoveryRateVal.setText(String.valueOf(stats.get("recoveryRate")));
        } catch (DatabaseException e) {
            System.err.println("Could not load dashboard stats: " + e.getMessage());
        }

        // Refresh recent items
        try {
            recentItemsList = itemService.getAllItems();
            tableModel.setRowCount(0);
            int limit = Math.min(recentItemsList.size(), 10);
            for (int i = 0; i < limit; i++) {
                BaseItem it = recentItemsList.get(i);
                tableModel.addRow(new Object[]{
                    it.getItemType(),
                    it.getTitle(),
                    it.getCategoryName(),
                    it.getLocation(),
                    DateUtil.formatDate(it.getDateReported()),
                    it.getStatus()
                });
            }
        } catch (DatabaseException e) {
            System.err.println("Could not load recent items: " + e.getMessage());
        }
    }
}
