package auctionsniper;

public interface SniperListener extends java.util.EventListener {
    void sniperLost();
    void sniperWinning();
    void sniperWon();
    void sniperStateChanged(SniperSnapshot snapshot);
}
