package com.campus.lostfound.ui.components;

import com.campus.lostfound.model.ClaimStatus;
import com.campus.lostfound.model.ItemStatus;
import com.campus.lostfound.model.ItemType;
import com.campus.lostfound.ui.theme.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

public class CustomTableRenderer extends DefaultTableCellRenderer {
    private final StatusBadge badge = new StatusBadge();

    public static void applyStyling(JTable table) {
        table.setRowHeight(40);
        table.setFont(Theme.FONT_BODY);
        table.setSelectionBackground(new Color(238, 242, 255));
        table.setSelectionForeground(Theme.TEXT_PRIMARY);
        table.setGridColor(new Color(241, 245, 249));
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(Theme.FONT_BODY_BOLD);
        header.setBackground(new Color(241, 245, 249));
        header.setForeground(Theme.TEXT_SECONDARY);
        header.setPreferredSize(new Dimension(header.getWidth(), 38));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER));

        CustomTableRenderer renderer = new CustomTableRenderer();
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        if (value instanceof ItemType) {
            badge.setItemType((ItemType) value);
            return badge;
        } else if (value instanceof ItemStatus) {
            badge.setItemStatus((ItemStatus) value);
            return badge;
        } else if (value instanceof ClaimStatus) {
            badge.setClaimStatus((ClaimStatus) value);
            return badge;
        }

        Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        setBorder(new EmptyBorder(0, 12, 0, 12));

        if (!isSelected) {
            comp.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
            comp.setForeground(Theme.TEXT_PRIMARY);
        }

        return comp;
    }
}
