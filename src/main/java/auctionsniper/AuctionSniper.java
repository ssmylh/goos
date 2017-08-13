package auctionsniper;

public class AuctionSniper implements AuctionEventListener {
    private SniperListener listener;
    private Auction auction;
    public AuctionSniper(Auction auction, SniperListener listener) {
        this.auction = auction;
        this.listener = listener;
    }

    @Override
    public void auctionClosed() {
        listener.sniperLost();
    }

    @Override
    public void currentPrice(int price, int increment, PriceSource priceSource) {
        if (priceSource == PriceSource.FromSniper) {
            listener.sniperWinnig();
        } else {
            auction.bid(price + increment);
            listener.sniperBidding();
        }
    }
}
