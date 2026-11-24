package com.campus.lostfound.ui.components;

import com.campus.lostfound.model.ClaimStatus;
import com.campus.lostfound.model.ItemStatus;
import com.campus.lostfound.model.ItemType;
import com.campus.lostfound.ui.theme.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class StatusBadge extends JLabel {
    private Color bgColor = Theme.INFO_BG;
    private Color fgColor = Theme.INFO;

    public StatusBadge() {
        this("");
    }

    public StatusBadge(String text) {
        super(text, SwingConstants.CENTER);
        setFont(Theme.FONT_SMALL_BOLD);
        setOpaque(false);
        setBorder(new EmptyBorder(4, 10, 4, 10));
    }

    public void setItemType(ItemType type) {
        if (type == ItemType.LOST) {
            setText("LOST");
            bgColor = new Color(254, 226, 226); // Red 100
            fgColor = new Color(185, 28, 28);   // Red 700
        } else {
            setText("FOUND");
            bgColor = new Color(219, 234, 254); // Blue 100
            fgColor = new Color(29, 78, 216);   // Blue 700
        }
        repaint();
    }

    public void setItemStatus(ItemStatus status) {
        if (status == null) {
            setText("UNKNOWN");
            bgColor = new Color(241, 245, 249);
            fgColor = Theme.TEXT_SECONDARY;
            repaint();
            return;
        }

        switch (status) {
            case OPEN:
                setText("OPEN");
                bgColor = Theme.SUCCESS_BG;
                fgColor = new Color(4, 120, 87);
                break;
            case CLAIM_PENDING:
                setText("PENDING REVIEW");
                bgColor = Theme.WARNING_BG;
                fgColor = new Color(180, 83, 9);
                break;
            case CLAIMED:
                setText("CLAIMED");
                bgColor = Theme.PURPLE_BG;
                fgColor = new Color(109, 40, 217);
                break;
            case RETURNED:
                setText("RETURNED");
                bgColor = new Color(209, 250, 229);
                fgColor = new Color(6, 95, 70);
                break;
            case CLOSED:
            default:
                setText("CLOSED");
                bgColor = new Color(241, 245, 249);
                fgColor = Theme.TEXT_MUTED;
                break;
        }
        repaint();
    }

    public void setClaimStatus(ClaimStatus status) {
        if (status == null) return;
        switch (status) {
            case PENDING:
                setText("PENDING");
                bgColor = Theme.WARNING_BG;
                fgColor = new Color(180, 83, 9);
                break;
            case APPROVED:
                setText("APPROVED");
                bgColor = Theme.SUCCESS_BG;
                fgColor = new Color(4, 120, 87);
                break;
            case REJECTED:
                setText("REJECTED");
                bgColor = Theme.DANGER_BG;
                fgColor = new Color(185, 28, 28);
                break;
            case CANCELLED:
            default:
                setText("CANCELLED");
                bgColor = new Color(241, 245, 249);
                fgColor = Theme.TEXT_MUTED;
                break;
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(bgColor);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

        setForeground(fgColor);
        super.paintComponent(g);
        g2.dispose();
    }
}
