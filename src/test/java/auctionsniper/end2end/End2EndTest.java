package auctionsniper.end2end;

import org.junit.Test;

public class End2EndTest {
    @Test
    public void firstFailingTest() throws Exception {
        FakeAuctionServer auction = new FakeAuctionServer("item-54321");
        ApplicationRunner runner = new ApplicationRunner();
        try {

            runner.startBiddingIn(auction);
        } finally {
            auction.stop();
            runner.stop();
        }
    }
}
