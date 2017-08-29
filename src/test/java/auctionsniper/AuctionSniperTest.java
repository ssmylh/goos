package auctionsniper;

import org.jmock.Expectations;
import org.jmock.States;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;

import static auctionsniper.AuctionEventListener.PriceSource.*;

public class AuctionSniperTest {
    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();
    private final States sniperStates = context.states("sniper");
    private final Auction auction = context.mock(Auction.class);
    private final SniperListener sniperListener = context.mock(SniperListener.class);
    private static final String ITEM_ID = "item-id";
    private final AuctionSniper sniper = new AuctionSniper(ITEM_ID, auction, sniperListener);

    @Test
    public void reportsLostWhenAuctionClosesImmediately() {
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
        int bid = price + increment;
        context.checking(new Expectations() {
            {
                oneOf(auction).bid(bid);
                atLeast(1).of(sniperListener).sniperBidding(
                        new SniperState(ITEM_ID, price, bid)
                );
            }
        });

        sniper.currentPrice(price, increment, FromOtherBidder);// `PriceSource` はどちらでも OK.
    }

    @Test
    public void reportsIsWinningWhenCurrentPriceComesFromSniper() {
        context.checking(new Expectations() {
            {
                atLeast(1).of(sniperListener).sniperWinning();
            }
        });
        sniper.currentPrice(123, 45, FromSniper);
    }

    @Test
    public void reportsLostIfAuctionClosesWhenBidding() {
        context.checking(new Expectations() {
            {
                ignoring(auction);

                allowing(sniperListener).sniperBidding(with(any(SniperState.class)));
                then(sniperStates.is("bidding"));

                atLeast(1).of(sniperListener).sniperLost();
                when(sniperStates.is("bidding"));
            }
        });

        sniper.currentPrice(123, 45, FromOtherBidder);
        sniper.auctionClosed();
    }

    @Test
    public void reportsWonIfAuctionClosesWhenWinning() {
        context.checking(new Expectations() {
            {
                ignoring(auction);

                allowing(sniperListener).sniperWinning();
                then(sniperStates.is("winning"));

                atLeast(1).of(sniperListener).sniperWon();
                when(sniperStates.is("winning"));
            }
        });

        sniper.currentPrice(123, 45, FromSniper);
        sniper.auctionClosed();
    }
}