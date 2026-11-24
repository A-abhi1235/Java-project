package com.campus.lostfound.ui;

import com.campus.lostfound.config.AppConfig;
import com.campus.lostfound.model.Role;
import com.campus.lostfound.model.User;
import com.campus.lostfound.service.*;
import com.campus.lostfound.util.SessionManager;
import com.campus.lostfound.ui.auth.LoginDialog;
import com.campus.lostfound.ui.components.ModernButton;
import com.campus.lostfound.ui.panels.*;
import com.campus.lostfound.ui.theme.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MainWindow extends JFrame implements SessionManager.SessionListener {
    private final AuthService authService;
    private final ItemService itemService;
    private final ClaimService claimService;
    private final NotificationService notificationService;
    private final ReportService reportService;

    // UI Structure
    private JPanel sidebarPanel;
    private JPanel mainContentPanel;
    private CardLayout cardLayout;

    // Header components
    private JLabel userProfileLabel;
    private JButton notifBtn;

    // Sidebar navigation buttons
    private final List<JButton> navButtons = new ArrayList<>();
    private JButton adminNavBtn;

    // View Panels
    private DashboardPanel dashboardPanel;
    private BrowseItemsPanel browseItemsPanel;
    private MyItemsPanel myItemsPanel;
    private ClaimsPanel claimsPanel;
    private AdminPanel adminPanel;

    public MainWindow() {
        this.authService = new AuthService();
        this.itemService = new ItemService();
        this.claimService = new ClaimService();
        this.notificationService = new NotificationService();
        this.reportService = new ReportService();

        SessionManager.getInstance().addListener(this);

        initWindow();
        initUI();
    }

    private void initWindow() {
        setTitle(AppConfig.APP_NAME + " v" + AppConfig.APP_VERSION);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1180, 780);
        setMinimumSize(new Dimension(980, 640));
        setLocationRelativeTo(null);
    }

    private void initUI() {
        getContentPane().setLayout(new BorderLayout());

        // 1. Top Header Bar
        JPanel topHeader = createTopHeader();
        getContentPane().add(topHeader, BorderLayout.NORTH);

        // 2. Left Sidebar Navigation
        sidebarPanel = createSidebar();
        getContentPane().add(sidebarPanel, BorderLayout.WEST);

        // 3. Center Content Area (CardLayout)
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.setBackground(Theme.BG_MAIN);

        dashboardPanel = new DashboardPanel(this, itemService, claimService, reportService, () -> navigateTo("BROWSE"));
        browseItemsPanel = new BrowseItemsPanel(this, itemService, claimService);
        myItemsPanel = new MyItemsPanel(this, itemService, claimService);
        claimsPanel = new ClaimsPanel(this, claimService, itemService);
        adminPanel = new AdminPanel(this, authService, reportService);

        mainContentPanel.add(dashboardPanel, "DASHBOARD");
        mainContentPanel.add(browseItemsPanel, "BROWSE");
        mainContentPanel.add(myItemsPanel, "MY_ITEMS");
        mainContentPanel.add(claimsPanel, "CLAIMS");
        mainContentPanel.add(adminPanel, "ADMIN");

        getContentPane().add(mainContentPanel, BorderLayout.CENTER);

        updateUserStatus();
        navigateTo("DASHBOARD");
    }

    private JPanel createTopHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setPreferredSize(new Dimension(getWidth(), 64));
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER),
            new EmptyBorder(10, 24, 10, 24)
        ));

        // Brand Title
        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        brand.setBackground(Color.WHITE);

        JLabel logoLbl = new JLabel("🔍");
        logoLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));

        JLabel brandLbl = new JLabel("Campus Lost & Found");
        brandLbl.setFont(Theme.FONT_HEADER);
        brandLbl.setForeground(Theme.PRIMARY_DARK);

        JLabel campusTag = new JLabel("SMART SYSTEM");
        campusTag.setFont(Theme.FONT_SMALL_BOLD);
        campusTag.setForeground(Theme.PRIMARY);
        campusTag.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.PRIMARY, 1, true),
            new EmptyBorder(2, 6, 2, 6)
        ));

        brand.add(logoLbl);
        brand.add(brandLbl);
        brand.add(campusTag);
        header.add(brand, BorderLayout.WEST);

        // Right side: User Profile, Notifications, Sign Out
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 4));
        right.setBackground(Color.WHITE);

        notifBtn = new JButton("🔔 Notifications (0)");
        notifBtn.setFont(Theme.FONT_BODY_BOLD);
        notifBtn.setFocusPainted(false);
        notifBtn.setBackground(new Color(241, 245, 249));
        notifBtn.setForeground(Theme.TEXT_PRIMARY);
        notifBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        notifBtn.addActionListener(e -> {
            NotificationsDialog dlg = new NotificationsDialog(this, notificationService, this::updateNotificationBadge);
            dlg.setVisible(true);
            updateNotificationBadge();
        });

        userProfileLabel = new JLabel("Signed In");
        userProfileLabel.setFont(Theme.FONT_BODY_BOLD);
        userProfileLabel.setForeground(Theme.TEXT_PRIMARY);

        ModernButton signOutBtn = new ModernButton("Sign Out", ModernButton.Style.SECONDARY);
        signOutBtn.addActionListener(e -> handleSignOut());

        right.add(notifBtn);
        right.add(userProfileLabel);
        right.add(signOutBtn);

        header.add(right, BorderLayout.EAST);
        return header;
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Theme.SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(230, getHeight()));
        sidebar.setBorder(new EmptyBorder(20, 12, 20, 12));

        JLabel menuHead = new JLabel("MAIN MENU");
        menuHead.setFont(Theme.FONT_SMALL_BOLD);
        menuHead.setForeground(Theme.TEXT_MUTED);
        menuHead.setAlignmentX(Component.LEFT_ALIGNMENT);
        menuHead.setBorder(new EmptyBorder(0, 12, 10, 0));
        sidebar.add(menuHead);

        sidebar.add(createNavButton("📊  Dashboard", "DASHBOARD"));
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(createNavButton("🔍  Browse Items", "BROWSE"));
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(createNavButton("📦  My Reported Items", "MY_ITEMS"));
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(createNavButton("📝  Claims & Proofs", "CLAIMS"));
        sidebar.add(Box.createVerticalStrut(6));

        JButton smartRadarNav = createNavButton("⚡  Smart Match Radar", "RADAR");
        smartRadarNav.addActionListener(e -> {
            SmartMatchesDialog dlg = new SmartMatchesDialog(this, itemService, claimService, this::refreshAllViews);
            dlg.setVisible(true);
        });
        sidebar.add(smartRadarNav);
        sidebar.add(Box.createVerticalStrut(6));

        adminNavBtn = createNavButton("🛡️  Admin Audit", "ADMIN");
        sidebar.add(adminNavBtn);

        sidebar.add(Box.createVerticalGlue());

        JLabel footerNote = new JLabel("Smart Campus v" + AppConfig.APP_VERSION);
        footerNote.setFont(Theme.FONT_SMALL);
        footerNote.setForeground(Theme.TEXT_MUTED);
        footerNote.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(footerNote);

        return sidebar;
    }

    private JButton createNavButton(String title, String viewName) {
        JButton btn = new JButton(title);
        btn.setFont(Theme.FONT_BODY_BOLD);
        btn.setForeground(new Color(226, 232, 240));
        btn.setBackground(Theme.SIDEBAR_BG);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setBorder(new EmptyBorder(10, 14, 10, 14));

        btn.addActionListener(e -> {
            if (!"RADAR".equals(viewName)) {
                navigateTo(viewName);
            }
        });

        navButtons.add(btn);
        return btn;
    }

    private void navigateTo(String viewName) {
        cardLayout.show(mainContentPanel, viewName);

        // Highlight active nav button
        for (JButton b : navButtons) {
            boolean active = b.getText().toUpperCase().contains(viewName.replace("_", " "));
            if (active) {
                b.setBackground(Theme.SIDEBAR_ACTIVE);
                b.setForeground(Color.WHITE);
            } else {
                b.setBackground(Theme.SIDEBAR_BG);
                b.setForeground(new Color(226, 232, 240));
            }
        }

        // Refresh destination view
        switch (viewName) {
            case "DASHBOARD": dashboardPanel.refreshData(); break;
            case "BROWSE": browseItemsPanel.performSearch(); break;
            case "MY_ITEMS": myItemsPanel.refreshData(); break;
            case "CLAIMS": claimsPanel.refreshData(); break;
            case "ADMIN": adminPanel.refreshData(); break;
        }

        updateNotificationBadge();
    }

    private void updateUserStatus() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            String roleTag = user.isAdmin() ? " [Admin]" : " [" + user.getRole().getDisplayName() + "]";
            userProfileLabel.setText(user.getFullName() + roleTag);
            adminNavBtn.setVisible(user.isAdmin());
            updateNotificationBadge();
        } else {
            userProfileLabel.setText("Not signed in");
            adminNavBtn.setVisible(false);
        }
    }

    private void updateNotificationBadge() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            int unread = notificationService.getUnreadCount(user.getId());
            notifBtn.setText("🔔 Notifications (" + unread + ")");
            if (unread > 0) {
                notifBtn.setBackground(new Color(254, 243, 199)); // amber highlight
                notifBtn.setForeground(new Color(180, 83, 9));
            } else {
                notifBtn.setBackground(new Color(241, 245, 249));
                notifBtn.setForeground(Theme.TEXT_PRIMARY);
            }
        }
    }

    private void handleSignOut() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to sign out?", "Sign Out", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            authService.logout();
            LoginDialog loginDlg = new LoginDialog(this, authService);
            loginDlg.setVisible(true);
            if (!loginDlg.isLoginSuccessful()) {
                System.exit(0);
            }
        }
    }

    public void refreshAllViews() {
        dashboardPanel.refreshData();
        browseItemsPanel.performSearch();
        myItemsPanel.refreshData();
        claimsPanel.refreshData();
        adminPanel.refreshData();
        updateNotificationBadge();
    }

    @Override
    public void onUserLoggedIn(User user) {
        updateUserStatus();
        refreshAllViews();
        navigateTo("DASHBOARD");
    }

    @Override
    public void onUserLoggedOut() {
        updateUserStatus();
    }
}
