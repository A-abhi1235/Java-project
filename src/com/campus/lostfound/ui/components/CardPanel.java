package com.campus.lostfound.ui.components;

import com.campus.lostfound.ui.theme.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class CardPanel extends JPanel {
    private final int radius;

    public CardPanel() {
        this(12, 16);
    }

    public CardPanel(int radius, int padding) {
        super(new BorderLayout());
        this.radius = radius;
        setOpaque(false);
        setBorder(new EmptyBorder(padding, padding, padding, padding));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Quiet, elevated surface that keeps dense screens easy to scan.
        g2.setColor(Theme.BG_CARD);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);

        g2.setColor(new Color(215, 226, 240));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);

        g2.dispose();
        super.paintComponent(g);
    }
}
