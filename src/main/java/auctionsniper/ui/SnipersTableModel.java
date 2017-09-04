package auctionsniper.ui;

import auctionsniper.SniperState;

import javax.swing.table.AbstractTableModel;

import static auctionsniper.ui.MainWindow.*;

public class SnipersTableModel extends AbstractTableModel {
    private final static SniperState STARTING_UP = new SniperState("", 0, 0);
    private SniperState sniperState = STARTING_UP;
    private String statusText = STATUS_JOINING;

    @Override
    public int getColumnCount() {
        return Column.values().length;
    }

    @Override
    public int getRowCount() {
        return 1;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (Column.at(columnIndex)) {
            case ITEM_IDENTIFIER:
                return sniperState.itemId;
            case LAST_PRICE:
                return sniperState.lastPrice;
            case LAST_BID:
                return sniperState.lastBid;
            case SNIPER_STATUS:
                return statusText;
            default:
                throw new IllegalArgumentException("No column at " + columnIndex);
        }
    }

    public void sniperStatusChanged(SniperState newSniperState, String newStatusText) {
        if (newSniperState != null) {
            this.sniperState = newSniperState;
        }
        this.statusText = newStatusText;
        fireTableRowsUpdated(0, 0);
    }
}
