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
    private final AuctionSniper sniper = new AuctionSniper(auction, sniperListener);


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
        context.checking(new Expectations() {
            {
                oneOf(auction).bid(price + increment);
                atLeast(1).of(sniperListener).sniperBidding();
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

                allowing(sniperListener).sniperBidding();
                then(sniperStates.is("bidding"));

                atLeast(1).of(sniperListener).sniperLost();
                when(sniperStates.is("bidding"));
            }
        });

        sniper.currentPrice(123, 45, FromOtherBidder);
        sniper.auctionClosed();
    }

    // `AuctionSniper を実装しないと正常にテストが落ちない。
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