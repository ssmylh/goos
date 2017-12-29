package auctionsniper.end2end;

import auctionsniper.WindowLickerWorkaround;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class End2EndTest {
    FakeAuctionServer auction;
    FakeAuctionServer auction2;
    ApplicationRunner runner;
    static final String itemId = "item-54321";
    static final String itemId2 = "item-65432";
    static final String SNIPER_XMPP_ID = String.format(ApplicationRunner.SNIPER_XMPP_ID_FORMAT, itemId);
    static final String SNIPER_XMPP_ID2 = String.format(ApplicationRunner.SNIPER_XMPP_ID_FORMAT, itemId2);

    @BeforeClass
    public static void workaround4WindowLicker() {
        WindowLickerWorkaround.fix();
    }

    @Before
    public void constructAuction() throws Exception {
        this.auction = new FakeAuctionServer(itemId);
        this.auction2 = new FakeAuctionServer(itemId2);
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
        if (this.auction2 != null) {
            this.auction2.stop();
        }
        if (this.runner != null) {
            this.runner.stop();
        }
     }

    @Test
    public void sniperJoinsAuctionUntilAuctionCloses() throws Exception {
        auction.startSellingItem();
        runner.startBiddingIn(auction);
        auction.hasReceivedJoinRequestFrom(SNIPER_XMPP_ID);
        auction.announceClosed();
        runner.hasShownSniperHasLostAuction(auction, 0, 0);
    }

    @Test
    public void sniperMakesAHigherBidButLoses() throws Exception {
        auction.startSellingItem();
        runner.startBiddingIn(auction);
        auction.hasReceivedJoinRequestFrom(SNIPER_XMPP_ID);

        auction.reportPrice(1000, 98, "other bidder");
        runner.hasShownSniperIsBidding(auction, 1000, 1098);

        auction.hasReceivedBid(1098, SNIPER_XMPP_ID);

        auction.announceClosed();
        runner.hasShownSniperHasLostAuction(auction, 1000, 1098);
    }

    @Test
    public void sniperWinsAnAuctionByBiddingHigher() throws Exception {
        auction.startSellingItem();
        runner.startBiddingIn(auction);
        auction.hasReceivedJoinRequestFrom(SNIPER_XMPP_ID);

        auction.reportPrice(1000, 98, "other bidder");
        runner.hasShownSniperIsBidding(auction, 1000, 1098);

        auction.hasReceivedBid(1098, SNIPER_XMPP_ID);

        auction.reportPrice(1098, 97, SNIPER_XMPP_ID);
        runner.hasShownSniperIsWinning(auction, 1098);

        auction.announceClosed();
        runner.hasShownSniperHasWonAuction(auction, 1098);
    }

    @Test
    public void sniperBidsMultipleItems() throws Exception {
        auction.startSellingItem();
        auction2.startSellingItem();

        runner.startBiddingIn(auction, auction2);
        auction.hasReceivedJoinRequestFrom(SNIPER_XMPP_ID);
        auction2.hasReceivedJoinRequestFrom(SNIPER_XMPP_ID2);

        auction.reportPrice(1000, 98, "other bidder");
        auction.hasReceivedBid(1098, SNIPER_XMPP_ID);

        auction2.reportPrice(500, 21, "other bidder");
        auction2.hasReceivedBid(521, SNIPER_XMPP_ID2);

        auction.reportPrice(1098, 97, SNIPER_XMPP_ID);
        auction2.reportPrice(521, 22, SNIPER_XMPP_ID2);

        runner.hasShownSniperIsWinning(auction, 1098);
        runner.hasShownSniperIsWinning(auction2, 521);

        auction.announceClosed();
        auction2.announceClosed();

        runner.hasShownSniperHasWonAuction(auction, 1098);
        runner.hasShownSniperHasWonAuction(auction2, 521);
    }

    @Test
    public void sniperLosesAnAuctionWhenThePriceIsTooHigh() throws Exception {
        auction.startSellingItem();

        runner.startBiddingWithStopPrice(auction, 1100);
        auction.hasReceivedJoinRequestFrom(SNIPER_XMPP_ID);
        auction.reportPrice(1000, 98, "other bidder");
        runner.hasShownSniperIsBidding(auction, 1000, 1098);

        auction.hasReceivedBid(1098, SNIPER_XMPP_ID);

        auction.reportPrice(1197, 10, "third party");
        runner.hasShownSniperIsLosing(auction, 1197, 1098);

        auction.reportPrice(1207, 10, "fourth party");
        runner.hasShownSniperIsLosing(auction, 1207, 1098);
        auction.announceClosed();
        runner.hasShownSniperHasLostAuction(auction, 1207, 1098);
    }
}
