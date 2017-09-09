package auctionsniper;

public interface SniperListener extends java.util.EventListener {
    void sniperLost();
    void sniperBidding(SniperSnapshot state);
    void sniperWinning();
    void sniperWon();
}
