package auctionsniper;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;

public class AuctionSniperTest {
    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();
    private final Auction auction = context.mock(Auction.class);
    private final SniperListener sniperListener = context.mock(SniperListener.class);
    private final AuctionSniper sniper = new AuctionSniper(auction, sniperListener);


    @Test
    public void reportsLostWhenAuctionCloses() {
        context.checking(new Expectations() {
            {
                oneOf(sniperListener).sniperLost();
            }
        });

        sniper.auctionClosed();
    }

    @Test
    public void bidsHigherAndReportsBiddingWhenNewPriceArrives() {
        int price = 1001;
        int increment = 25;
        context.checking(new Expectations() {
            {
                oneOf(auction).bid(price + increment);
                atLeast(1).of(sniperListener).sniperBidding();
            }
        });

        sniper.currentPrice(price, increment);
    }
}