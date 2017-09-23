package auctionsniper.end2end;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class End2EndTest {
    FakeAuctionServer auction;
    ApplicationRunner runner;

    @Before
    public void constructAuction() throws Exception {
        this.auction = new FakeAuctionServer("item-54321");
    }

    @Before
    public void constructApplicationRunner() {
        this.runner = new ApplicationRunner();
    }

    @After
    public void stopAll() {
        if (this.auction != null) {
            this.auction.stop();
        }
        if (this.runner != null) {
            this.runner.stop();
        }
     }

    @Test
    public void sniperJoinsAuctionUntilAuctionCloses() throws Exception {
        auction.startSellingItem();
        runner.startBiddingIn(auction);
        auction.hasReceivedJoinRequestFrom(ApplicationRunner.SNIPER_XMPP_ID);
        auction.announceClosed();
        runner.hasShownSniperHasLostAuction(0, 0);
    }

    @Test
    public void sniperMakesAHigherBidButLoses() throws Exception {
        auction.startSellingItem();
        runner.startBiddingIn(auction);
        auction.hasReceivedJoinRequestFrom(ApplicationRunner.SNIPER_XMPP_ID);

        auction.reportPrice(1000, 98, "other bidder");
        runner.hasShownSniperIsBidding(1000, 1098);

        auction.hasReceivedBid(1098, ApplicationRunner.SNIPER_XMPP_ID);

        auction.announceClosed();
        runner.hasShownSniperHasLostAuction(1000, 1098);
    }

    @Test
    public void sniperWinsAnAuctionByBiddingHigher() throws Exception {
        auction.startSellingItem();
        runner.startBiddingIn(auction);
        auction.hasReceivedJoinRequestFrom(ApplicationRunner.SNIPER_XMPP_ID);

        auction.reportPrice(1000, 98, "other bidder");
        runner.hasShownSniperIsBidding(1000, 1098);

        auction.hasReceivedBid(1098, ApplicationRunner.SNIPER_XMPP_ID);

        auction.reportPrice(1098, 97, ApplicationRunner.SNIPER_XMPP_ID);
        runner.hasShownSniperIsWinning(1098);

        auction.announceClosed();
        runner.hasShownSniperHasWonAuction(1098);
    }
}
