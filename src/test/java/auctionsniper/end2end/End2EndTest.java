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
            auction.hasReceivedJoinRequestFromSniper();
            auction.announceClosed();
            runner.showsSniperHasLostAuction();
        } finally {
            auction.stop();
            runner.stop();
        }
    }
}
