package auctionsniper.ui;

import javax.swing.table.AbstractTableModel;

import static auctionsniper.ui.MainWindow.*;

public class SnipersTableModel extends AbstractTableModel {
    private String statusText = STATUS_JOINING;

    @Override
    public int getColumnCount() {
        return 1;
    }

    @Override
    public int getRowCount() {
        return 1;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return statusText;
    }

    public void setStatusText(String newStatusText) {
        this.statusText = newStatusText;
        fireTableRowsUpdated(0, 0);
    }
}
