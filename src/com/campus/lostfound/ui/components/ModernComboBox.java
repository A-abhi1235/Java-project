package com.campus.lostfound.ui.components;

import com.campus.lostfound.ui.theme.Theme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ModernComboBox<T> extends JComboBox<T> {
    public ModernComboBox() {
        super();
        initStyle();
    }

    public ModernComboBox(T[] items) {
        super(items);
        initStyle();
    }

    private void initStyle() {
        setFont(Theme.FONT_BODY);
        setBackground(Color.WHITE);
        setForeground(Theme.TEXT_PRIMARY);
        setFocusable(false);
        setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER, 1, true),
            new EmptyBorder(3, 8, 3, 8)
        ));
        setMaximumRowCount(10);
    }
}
