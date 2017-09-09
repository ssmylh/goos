package auctionsniper;

import static auctionsniper.AuctionEventListener.PriceSource.*;

public class AuctionSniper implements AuctionEventListener {
    private String itemId;
    private SniperListener listener;
    private Auction auction;
    private boolean isWinning;

    public AuctionSniper(String itemId, Auction auction, SniperListener listener) {
        this.itemId = itemId;
        this.auction = auction;
        this.listener = listener;
    }

    @Override
    public void auctionClosed() {
        if (isWinning) {
            listener.sniperWon();
        } else {
            listener.sniperLost();
        }
    }

    @Override
    public void currentPrice(int price, int increment, PriceSource priceSource) {
        isWinning = priceSource == FromSniper;
        if (isWinning) {
            listener.sniperWinning();
        } else {
            int bid = price + increment;
            auction.bid(bid);
            listener.sniperBidding(new SniperSnapshot(itemId, price, bid));
        }
    }
}
