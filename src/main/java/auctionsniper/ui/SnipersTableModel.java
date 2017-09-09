package auctionsniper.ui;

import auctionsniper.SniperSnapshot;

import javax.swing.table.AbstractTableModel;

import static auctionsniper.ui.MainWindow.*;

public class SnipersTableModel extends AbstractTableModel {
    private final static SniperSnapshot STARTING_UP = new SniperSnapshot("", 0, 0);
    private SniperSnapshot snapshot = STARTING_UP;
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
                return snapshot.itemId;
            case LAST_PRICE:
                return snapshot.lastPrice;
            case LAST_BID:
                return snapshot.lastBid;
            case SNIPER_STATE:
                return statusText;
            default:
                throw new IllegalArgumentException("No column at " + columnIndex);
        }
    }

    public void sniperStatusChanged(SniperSnapshot newSniperSnapshot, String newStatusText) {
        if (newSniperSnapshot != null) {
            this.snapshot = newSniperSnapshot;
        }
        this.statusText = newStatusText;
        fireTableRowsUpdated(0, 0);
    }
}
