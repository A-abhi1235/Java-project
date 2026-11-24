package com.campus.lostfound.ui.components;

import com.campus.lostfound.ui.theme.Theme;

import javax.swing.*;
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
    }
}
