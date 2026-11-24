package com.campus.lostfound.ui.panels;

import com.campus.lostfound.config.AppConfig;
import com.campus.lostfound.exception.DatabaseException;
import com.campus.lostfound.model.*;
import com.campus.lostfound.service.ClaimService;
import com.campus.lostfound.service.ItemService;
import com.campus.lostfound.util.DateUtil;
import com.campus.lostfound.ui.components.*;
import com.campus.lostfound.ui.theme.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class BrowseItemsPanel extends JPanel {
    private final Frame parentFrame;
    private final ItemService itemService;
    private final ClaimService claimService;

    private ModernTextField searchField;
    private ModernComboBox<String> typeCombo;
    private ModernComboBox<CategoryItemWrapper> categoryCombo;
    private ModernComboBox<String> locationCombo;
    private ModernComboBox<StatusItemWrapper> statusCombo;

    private JTable itemsTable;
    private DefaultTableModel tableModel;
    private List<BaseItem> displayedItems = new ArrayList<>();

    // Wrapper helper classes for ComboBox items
    private static class CategoryItemWrapper {
        final Integer id;
        final String name;
        CategoryItemWrapper(Integer id, String name) { this.id = id; this.name = name; }
        @Override public String toString() { return name; }
    }

    private static class StatusItemWrapper {
        final ItemStatus status;
        final String name;
        StatusItemWrapper(ItemStatus status, String name) { this.status = status; this.name = name; }
        @Override public String toString() { return name; }
    }

    public BrowseItemsPanel(Frame parentFrame, ItemService itemService, ClaimService claimService) {
        super(new BorderLayout());
        this.parentFrame = parentFrame;
        this.itemService = itemService;
        this.claimService = claimService;
        setBackground(Theme.BG_MAIN);
        initUI();
    }

    private void initUI() {
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setBackground(Theme.BG_MAIN);
        topContainer.setBorder(new EmptyBorder(20, 24, 12, 24));

        // 1. Header Title & Actions
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setBackground(Theme.BG_MAIN);

        JPanel titleGrp = new JPanel();
        titleGrp.setLayout(new BoxLayout(titleGrp, BoxLayout.Y_AXIS));
        titleGrp.setBackground(Theme.BG_MAIN);

        JLabel titleLbl = new JLabel("Browse Campus Belongings");
        titleLbl.setFont(Theme.FONT_HEADER_LARGE);
        titleLbl.setForeground(Theme.PRIMARY_DARK);

        JLabel subLbl = new JLabel("Search and filter all reported lost and found items across campus.");
        subLbl.setFont(Theme.FONT_BODY);
        subLbl.setForeground(Theme.TEXT_SECONDARY);

        titleGrp.add(titleLbl);
        titleGrp.add(Box.createVerticalStrut(4));
        titleGrp.add(subLbl);
        headerRow.add(titleGrp, BorderLayout.WEST);

        JPanel quickReports = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        quickReports.setBackground(Theme.BG_MAIN);

        ModernButton reportLostBtn = new ModernButton("+ Report Lost", ModernButton.Style.DANGER);
        reportLostBtn.addActionListener(e -> {
            ReportItemDialog dlg = new ReportItemDialog(parentFrame, itemService, ItemType.LOST, this::performSearch);
            dlg.setVisible(true);
        });

        ModernButton reportFoundBtn = new ModernButton("+ Report Found", ModernButton.Style.PRIMARY);
        reportFoundBtn.addActionListener(e -> {
            ReportItemDialog dlg = new ReportItemDialog(parentFrame, itemService, ItemType.FOUND, this::performSearch);
            dlg.setVisible(true);
        });

        quickReports.add(reportLostBtn);
        quickReports.add(reportFoundBtn);
        headerRow.add(quickReports, BorderLayout.EAST);

        topContainer.add(headerRow);
        topContainer.add(Box.createVerticalStrut(16));

        // 2. Filter Bar Card
        CardPanel filterCard = new CardPanel(10, 14);
        filterCard.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 6));

        searchField = new ModernTextField(14);
        searchField.setToolTipText("Search title, description or location");

        typeCombo = new ModernComboBox<>(new String[]{"All Types", "Lost Only", "Found Only"});

        categoryCombo = new ModernComboBox<>();
        loadCategoriesIntoCombo();

        locationCombo = new ModernComboBox<>(AppConfig.CAMPUS_LOCATIONS);

        statusCombo = new ModernComboBox<>();
        statusCombo.addItem(new StatusItemWrapper(null, "All Statuses"));
        statusCombo.addItem(new StatusItemWrapper(ItemStatus.OPEN, "Open Only"));
        statusCombo.addItem(new StatusItemWrapper(ItemStatus.CLAIM_PENDING, "Under Review"));
        statusCombo.addItem(new StatusItemWrapper(ItemStatus.RETURNED, "Returned"));

        ModernButton searchBtn = new ModernButton("Search", ModernButton.Style.PRIMARY);
        searchBtn.addActionListener(e -> performSearch());

        ModernButton resetBtn = new ModernButton("Reset", ModernButton.Style.SECONDARY);
        resetBtn.addActionListener(e -> resetFilters());

        filterCard.add(new JLabel("Keyword:"));
        filterCard.add(searchField);
        filterCard.add(new JLabel("Type:"));
        filterCard.add(typeCombo);
        filterCard.add(new JLabel("Category:"));
        filterCard.add(categoryCombo);
        filterCard.add(new JLabel("Location:"));
        filterCard.add(locationCombo);
        filterCard.add(new JLabel("Status:"));
        filterCard.add(statusCombo);
        filterCard.add(searchBtn);
        filterCard.add(resetBtn);

        topContainer.add(filterCard);
        add(topContainer, BorderLayout.NORTH);

        // 3. Items Table
        String[] cols = {"ID", "Type", "Title", "Category", "Campus Location", "Date Reported", "Status", "Reported By"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
            @Override
            public Class<?> getColumnClass(int col) {
                if (col == 1) return ItemType.class;
                if (col == 6) return ItemStatus.class;
                return String.class;
            }
        };

        itemsTable = new JTable(tableModel);
        CustomTableRenderer.applyStyling(itemsTable);

        // Set column widths
        itemsTable.getColumnModel().getColumn(0).setMaxWidth(60);
        itemsTable.getColumnModel().getColumn(1).setMaxWidth(100);
        itemsTable.getColumnModel().getColumn(5).setMaxWidth(130);
        itemsTable.getColumnModel().getColumn(6).setMaxWidth(140);

        itemsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && itemsTable.getSelectedRow() >= 0) {
                    openSelectedItemDetails();
                }
            }
        });

        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.setBackground(Theme.BG_MAIN);
        tableWrapper.setBorder(new EmptyBorder(0, 24, 16, 24));

        CardPanel tableCard = new CardPanel(12, 4);
        tableCard.setLayout(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane(itemsTable);
        scrollPane.setBorder(null);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        tableWrapper.add(tableCard, BorderLayout.CENTER);
        add(tableWrapper, BorderLayout.CENTER);

        // 4. Bottom Action Bar
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        bottomBar.setBackground(Color.WHITE);
        bottomBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER));

        ModernButton viewDetailsBtn = new ModernButton("View Details", ModernButton.Style.PRIMARY);
        viewDetailsBtn.addActionListener(e -> openSelectedItemDetails());

        ModernButton matchRadarBtn = new ModernButton("Smart Match Radar", ModernButton.Style.OUTLINE);
        matchRadarBtn.addActionListener(e -> {
            int row = itemsTable.getSelectedRow();
            BaseItem selected = (row >= 0 && row < displayedItems.size()) ? displayedItems.get(row) : null;
            SmartMatchesDialog smd = new SmartMatchesDialog(parentFrame, selected, itemService, claimService, this::performSearch);
            smd.setVisible(true);
        });

        ModernButton refreshBtn = new ModernButton("Refresh", ModernButton.Style.SECONDARY);
        refreshBtn.addActionListener(e -> performSearch());

        bottomBar.add(matchRadarBtn);
        bottomBar.add(refreshBtn);
        bottomBar.add(viewDetailsBtn);
        add(bottomBar, BorderLayout.SOUTH);

        performSearch();
    }

    private void loadCategoriesIntoCombo() {
        categoryCombo.removeAllItems();
        categoryCombo.addItem(new CategoryItemWrapper(null, "All Categories"));
        try {
            List<Category> cats = itemService.getCategories();
            for (Category c : cats) {
                categoryCombo.addItem(new CategoryItemWrapper(c.getId(), c.getName()));
            }
        } catch (DatabaseException e) {
            System.err.println("Could not load categories in combo: " + e.getMessage());
        }
    }

    private void resetFilters() {
        searchField.setText("");
        typeCombo.setSelectedIndex(0);
        categoryCombo.setSelectedIndex(0);
        locationCombo.setSelectedIndex(0);
        statusCombo.setSelectedIndex(0);
        performSearch();
    }

    public void performSearch() {
        String keyword = searchField.getText().trim();

        ItemType type = null;
        if (typeCombo.getSelectedIndex() == 1) type = ItemType.LOST;
        else if (typeCombo.getSelectedIndex() == 2) type = ItemType.FOUND;

        CategoryItemWrapper catWrap = (CategoryItemWrapper) categoryCombo.getSelectedItem();
        Integer catId = catWrap != null ? catWrap.id : null;

        String location = (String) locationCombo.getSelectedItem();
        if ("All Locations".equalsIgnoreCase(location)) location = null;

        StatusItemWrapper statWrap = (StatusItemWrapper) statusCombo.getSelectedItem();
        ItemStatus status = statWrap != null ? statWrap.status : null;

        try {
            displayedItems = itemService.searchItems(type, catId, location, status, keyword);
            tableModel.setRowCount(0);
            for (BaseItem item : displayedItems) {
                tableModel.addRow(new Object[]{
                    item.getId(),
                    item.getItemType(),
                    item.getTitle(),
                    item.getCategoryName(),
                    item.getLocation(),
                    DateUtil.formatDate(item.getDateReported()),
                    item.getStatus(),
                    item.getReportedByUserName()
                });
            }
        } catch (DatabaseException e) {
            JOptionPane.showMessageDialog(this, "Search error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openSelectedItemDetails() {
        int row = itemsTable.getSelectedRow();
        if (row < 0 || row >= displayedItems.size()) {
            JOptionPane.showMessageDialog(this, "Please select an item from the table first.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        BaseItem selected = displayedItems.get(row);
        ItemDetailDialog dlg = new ItemDetailDialog(parentFrame, selected, itemService, claimService, this::performSearch);
        dlg.setVisible(true);
    }
}
