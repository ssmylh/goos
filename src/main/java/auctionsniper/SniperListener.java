package auctionsniper;

public interface SniperListener extends java.util.EventListener {
    void sniperLost();
    void sniperBidding();
    void sniperWinnig();
    void sniperWon();
}
