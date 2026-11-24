package com.campus.lostfound.ui.components;

import com.campus.lostfound.ui.theme.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ModernButton extends JButton {
    public enum Style {
        PRIMARY, SECONDARY, SUCCESS, DANGER, OUTLINE
    }

    private final Style style;
    private boolean isHovered = false;
    private final int cornerRadius = 8;

    public ModernButton(String text) {
        this(text, Style.PRIMARY);
    }

    public ModernButton(String text, Style style) {
        super(text);
        this.style = style;
        setFont(Theme.FONT_BODY_BOLD);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setMargin(new Insets(8, 16, 8, 16));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color bgColor;
        Color fgColor;

        switch (style) {
            case PRIMARY:
                bgColor = isHovered ? Theme.PRIMARY_HOVER : Theme.PRIMARY;
                fgColor = Theme.TEXT_WHITE;
                break;
            case SECONDARY:
                bgColor = isHovered ? new Color(226, 232, 240) : new Color(241, 245, 249);
                fgColor = Theme.TEXT_PRIMARY;
                break;
            case SUCCESS:
                bgColor = isHovered ? new Color(5, 150, 105) : Theme.SUCCESS;
                fgColor = Theme.TEXT_WHITE;
                break;
            case DANGER:
                bgColor = isHovered ? new Color(220, 38, 38) : Theme.DANGER;
                fgColor = Theme.TEXT_WHITE;
                break;
            case OUTLINE:
            default:
                bgColor = isHovered ? new Color(241, 245, 249) : Color.WHITE;
                fgColor = Theme.PRIMARY;
                break;
        }

        // Background
        g2.setColor(bgColor);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);

        // Outline border if outline or secondary
        if (style == Style.OUTLINE) {
            g2.setColor(Theme.PRIMARY);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);
        } else if (style == Style.SECONDARY) {
            g2.setColor(Theme.BORDER);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);
        }

        // Text
        g2.setColor(fgColor);
        FontMetrics fm = g2.getFontMetrics(getFont());
        int textX = (getWidth() - fm.stringWidth(getText())) / 2;
        int textY = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
        g2.drawString(getText(), textX, textY);

        g2.dispose();
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(Math.max(d.width + 24, 80), Math.max(d.height + 10, 36));
    }
}
