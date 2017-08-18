package gui;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;

public class CustomTableCellRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table,
            Object obj, boolean isSelected, boolean hasFocus, int row, int column) {
        Component cell = super.getTableCellRendererComponent(
                table, obj, isSelected, hasFocus, row, column);
        this.setOpaque(true);
        if (isSelected) {
            cell.setBackground(new Color(65, 86, 197));
        } else {
            if (row % 2 == 0) {
                if ("Noire".equals(UIManager.getLookAndFeel().getName())||"HiFi".equals(UIManager.getLookAndFeel().getName())) {
                    cell.setBackground(new Color(20, 20, 20));
                } else {
                    cell.setBackground(new Color(232, 232, 232));
                }
            } else {
                if ("Noire".equals(UIManager.getLookAndFeel().getName())||"HiFi".equals(UIManager.getLookAndFeel().getName())) {
                    cell.setBackground(new Color(33, 33, 33));
                } else {
                    cell.setBackground(new Color(221, 221, 221));
                }
            }
        }
        return cell;
    }
}
