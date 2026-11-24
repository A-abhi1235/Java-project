package com.campus.lostfound.ui.components;

import com.campus.lostfound.ui.theme.Theme;

import javax.swing.*;
import java.awt.*;

/** A compact gradient surface for primary dashboard messaging. */
public class HeroPanel extends JPanel {
    private final int radius;

    public HeroPanel(int radius) {
        this.radius = radius;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(new GradientPaint(0, 0, Theme.PRIMARY, getWidth(), getHeight(), new Color(79, 70, 229)));
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
        g2.setColor(new Color(255, 255, 255, 45));
        g2.fillOval(getWidth() - 130, -55, 180, 180);
        g2.dispose();
        super.paintComponent(g);
    }
}
