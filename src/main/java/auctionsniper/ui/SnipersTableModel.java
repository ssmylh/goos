package auctionsniper.ui;

import auctionsniper.SniperSnapshot;

import javax.swing.table.AbstractTableModel;

public class SnipersTableModel extends AbstractTableModel {
    private SniperSnapshot snapshot;
    private String statusText;
    private static String[] STATUS_TEXT = {
            MainWindow.STATUS_JOINING,
            MainWindow.STATUS_BIDDING,
            MainWindow.STATUS_WINNING,
            MainWindow.STATUS_LOST,
            MainWindow.STATUS_WON
    };

    public SnipersTableModel(SniperSnapshot snapshot) {
        this.snapshot = snapshot;
        this.statusText = STATUS_TEXT[snapshot.state.ordinal()];
    }

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

    public void sniperStatusChanged(SniperSnapshot newSniperSnapshot) {
        this.snapshot = newSniperSnapshot;
        this.statusText = STATUS_TEXT[newSniperSnapshot.state.ordinal()];
        fireTableRowsUpdated(0, 0);
    }

    // TODO
    public void sniperWon() {
        this.snapshot = snapshot.won();
        this.statusText = STATUS_TEXT[snapshot.state.ordinal()];
        fireTableRowsUpdated(0, 0);
    }

    // TODO
    public void sniperLost() {
        this.snapshot = snapshot.lost();
        this.statusText = STATUS_TEXT[snapshot.state.ordinal()];
        fireTableRowsUpdated(0, 0);
    }

}
