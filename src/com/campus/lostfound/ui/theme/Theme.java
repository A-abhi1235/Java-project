package com.campus.lostfound.ui.theme;

import java.awt.*;

/**
 * Standard visual styling, modern color palette, and typography.
 */
public final class Theme {
    private Theme() {}

    // Color Palette
    public static final Color PRIMARY = new Color(37, 99, 235);        // Royal Blue #2563EB
    public static final Color PRIMARY_HOVER = new Color(29, 78, 216);  // #1D4ED8
    public static final Color PRIMARY_DARK = new Color(15, 23, 42);    // Slate Dark #0F172A
    public static final Color SIDEBAR_BG = new Color(30, 41, 59);      // Slate 800 #1E293B
    public static final Color SIDEBAR_HOVER = new Color(51, 65, 85);   // Slate 700
    public static final Color SIDEBAR_ACTIVE = new Color(37, 99, 235); // Active selection

    public static final Color BG_MAIN = new Color(248, 250, 252);      // Off-white / Slate 50
    public static final Color BG_CARD = Color.WHITE;
    public static final Color BORDER = new Color(226, 232, 240);       // Slate 200
    public static final Color BORDER_FOCUS = new Color(59, 130, 246);  // Blue 500

    public static final Color TEXT_PRIMARY = new Color(15, 23, 42);    // Slate 900
    public static final Color TEXT_SECONDARY = new Color(100, 116, 139);// Slate 500
    public static final Color TEXT_MUTED = new Color(148, 163, 184);   // Slate 400
    public static final Color TEXT_WHITE = Color.WHITE;

    // Semantic Status Colors
    public static final Color SUCCESS = new Color(16, 185, 129);       // Emerald #10B981
    public static final Color SUCCESS_BG = new Color(236, 253, 245);
    public static final Color WARNING = new Color(245, 158, 11);       // Amber #F59E0B
    public static final Color WARNING_BG = new Color(254, 243, 199);
    public static final Color DANGER = new Color(239, 68, 68);         // Red #EF4444
    public static final Color DANGER_BG = new Color(254, 242, 242);
    public static final Color INFO = new Color(14, 165, 233);          // Sky #0EA5E9
    public static final Color INFO_BG = new Color(240, 249, 255);
    public static final Color PURPLE = new Color(139, 92, 246);        // Purple #8B5CF6
    public static final Color PURPLE_BG = new Color(245, 243, 255);

    // Fonts
    public static final Font FONT_HEADER_LARGE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 17);
    public static final Font FONT_SUBHEADER = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_SMALL_BOLD = new Font("Segoe UI", Font.BOLD, 11);
}
