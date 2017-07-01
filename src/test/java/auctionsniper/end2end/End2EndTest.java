package auctionsniper.end2end;

import org.junit.Test;

public class End2EndTest {

    @Test
    public void sniperJoinsAuctionUntilAuctionCloses() throws Exception {
        FakeAuctionServer auction = new FakeAuctionServer("item-54321");
        ApplicationRunner runner = new ApplicationRunner();
        try {
            auction.startSellingItem();
            runner.startBiddingIn(auction);
            auction.hasReceivedJoinRequestFromSniper(ApplicationRunner.SNIPER_XMPP_ID);
            auction.announceClosed();
            runner.showsSniperHasLostAuction();
        } finally {
            auction.stop();
            runner.stop();
        }
    }

    @Test
    public void sniperMakesAHigherBidButLoses() throws Exception {
        FakeAuctionServer auction = new FakeAuctionServer("item-54321");
        ApplicationRunner runner = new ApplicationRunner();

        try {
            auction.startSellingItem();
            runner.startBiddingIn(auction);
            auction.hasReceivedJoinRequestFromSniper(ApplicationRunner.SNIPER_XMPP_ID);

            auction.reportPrice(1000, 98, "other bidder");

            runner.showsSniperIsBidding();
            auction.hasReceivedBid(1098, ApplicationRunner.SNIPER_XMPP_ID);

            auction.announceClosed();
            runner.showsSniperHasLostAuction();
        } finally {
            auction.stop();
            runner.stop();
        }
    }
}
