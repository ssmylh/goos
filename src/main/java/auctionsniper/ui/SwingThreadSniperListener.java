package auctionsniper.ui;

import auctionsniper.SniperListener;
import auctionsniper.SniperSnapshot;

import javax.swing.*;

public class SwingThreadSniperListener implements SniperListener {
    private SniperListener delegate;

    public SwingThreadSniperListener(SniperListener delegate) {
        this.delegate = delegate;
    }

    @Override
    public void sniperStateChanged(SniperSnapshot snapshot) {
        SwingUtilities.invokeLater(() -> delegate.sniperStateChanged(snapshot));

    }
}
