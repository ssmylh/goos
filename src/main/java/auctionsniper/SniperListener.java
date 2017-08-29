package auctionsniper;

public interface SniperListener extends java.util.EventListener {
    void sniperLost();
    void sniperBidding(SniperState state);
    void sniperWinning();
    void sniperWon();
}
