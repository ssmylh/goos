package auctionsniper;

public interface SniperListener extends java.util.EventListener {
    void sniperStateChanged(SniperSnapshot snapshot);
}
