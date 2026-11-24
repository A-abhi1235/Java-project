package com.campus.lostfound.ui.panels;

import com.campus.lostfound.model.Notification;
import com.campus.lostfound.model.User;
import com.campus.lostfound.service.NotificationService;
import com.campus.lostfound.util.DateUtil;
import com.campus.lostfound.util.SessionManager;
import com.campus.lostfound.ui.components.CardPanel;
import com.campus.lostfound.ui.components.ModernButton;
import com.campus.lostfound.ui.theme.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class NotificationsDialog extends JDialog {
    private final NotificationService notificationService;
    private final Runnable onDismissCallback;

    public NotificationsDialog(Frame parent, NotificationService notificationService, Runnable onDismissCallback) {
        super(parent, "Campus Notifications", true);
        this.notificationService = notificationService;
        this.onDismissCallback = onDismissCallback;
        initUI();
    }

    private void initUI() {
        setSize(540, 520);
        setLocationRelativeTo(getParent());
        getContentPane().setBackground(Theme.BG_MAIN);
        setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel title = new JLabel("Your Campus Notifications");
        title.setFont(Theme.FONT_HEADER);
        title.setForeground(Theme.PRIMARY_DARK);

        User curUser = SessionManager.getInstance().getCurrentUser();
        ModernButton markAllBtn = new ModernButton("Mark All Read", ModernButton.Style.SECONDARY);
        markAllBtn.addActionListener(e -> {
            if (curUser != null) {
                notificationService.markAllAsRead(curUser.getId());
                if (onDismissCallback != null) onDismissCallback.run();
                dispose();
            }
        });

        header.add(title, BorderLayout.WEST);
        header.add(markAllBtn, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBackground(Theme.BG_MAIN);
        list.setBorder(new EmptyBorder(16, 20, 16, 20));

        if (curUser != null) {
            List<Notification> notifs = notificationService.getUserNotifications(curUser.getId());
            if (notifs.isEmpty()) {
                JPanel empty = new CardPanel(12, 24);
                JLabel noMsg = new JLabel("You have no notifications.", SwingConstants.CENTER);
                noMsg.setFont(Theme.FONT_BODY);
                noMsg.setForeground(Theme.TEXT_MUTED);
                empty.add(noMsg, BorderLayout.CENTER);
                list.add(empty);
            } else {
                for (Notification n : notifs) {
                    list.add(createNotificationCard(n));
                    list.add(Box.createVerticalStrut(10));
                }
            }
        }

        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 12));
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER));

        ModernButton closeBtn = new ModernButton("Close", ModernButton.Style.SECONDARY);
        closeBtn.addActionListener(e -> dispose());
        footer.add(closeBtn);

        add(footer, BorderLayout.SOUTH);
    }

    private JPanel createNotificationCard(Notification n) {
        CardPanel card = new CardPanel(10, 14);
        card.setLayout(new BorderLayout(8, 4));

        JLabel t = new JLabel(n.getTitle());
        t.setFont(Theme.FONT_BODY_BOLD);
        t.setForeground(n.isRead() ? Theme.TEXT_SECONDARY : Theme.PRIMARY_DARK);

        JLabel time = new JLabel(DateUtil.formatDateTime(n.getCreatedAt()));
        time.setFont(Theme.FONT_SMALL);
        time.setForeground(Theme.TEXT_MUTED);

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Color.WHITE);
        top.add(t, BorderLayout.WEST);
        top.add(time, BorderLayout.EAST);

        JLabel msg = new JLabel("<html>" + n.getMessage() + "</html>");
        msg.setFont(Theme.FONT_BODY);
        msg.setForeground(Theme.TEXT_PRIMARY);

        card.add(top, BorderLayout.NORTH);
        card.add(msg, BorderLayout.CENTER);

        return card;
    }
}
