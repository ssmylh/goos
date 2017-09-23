package auctionsniper.end2end;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class End2EndTest {
    FakeAuctionServer auction;
    FakeAuctionServer auction2;
    ApplicationRunner runner;

    @Before
    public void constructAuction() throws Exception {
        this.auction = new FakeAuctionServer("item-54321");
        this.auction2 = new FakeAuctionServer("item-65432");
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
        auction.hasReceivedJoinRequestFrom(ApplicationRunner.SNIPER_XMPP_ID);
        auction.announceClosed();
        runner.hasShownSniperHasLostAuction(auction, 0, 0);
    }

    @Test
    public void sniperMakesAHigherBidButLoses() throws Exception {
        auction.startSellingItem();
        runner.startBiddingIn(auction);
        auction.hasReceivedJoinRequestFrom(ApplicationRunner.SNIPER_XMPP_ID);

        auction.reportPrice(1000, 98, "other bidder");
        runner.hasShownSniperIsBidding(auction, 1000, 1098);

        auction.hasReceivedBid(1098, ApplicationRunner.SNIPER_XMPP_ID);

        auction.announceClosed();
        runner.hasShownSniperHasLostAuction(auction, 1000, 1098);
    }

    @Test
    public void sniperWinsAnAuctionByBiddingHigher() throws Exception {
        auction.startSellingItem();
        runner.startBiddingIn(auction);
        auction.hasReceivedJoinRequestFrom(ApplicationRunner.SNIPER_XMPP_ID);

        auction.reportPrice(1000, 98, "other bidder");
        runner.hasShownSniperIsBidding(auction, 1000, 1098);

        auction.hasReceivedBid(1098, ApplicationRunner.SNIPER_XMPP_ID);

        auction.reportPrice(1098, 97, ApplicationRunner.SNIPER_XMPP_ID);
        runner.hasShownSniperIsWinning(auction, 1098);

        auction.announceClosed();
        runner.hasShownSniperHasWonAuction(auction, 1098);
    }

    @Test
    public void sniperBidsMultipleItems() throws Exception {
        auction.startSellingItem();
        auction2.startSellingItem();

        // テーブルに行が追加されていないので、`ApplicationRunner.startBiddingIn`内のチェック(`AuctionSniperDriver.showsSniperStatus`)で失敗する。
        // 結果、`SingleIncomingListener.receiveAMessage`は呼ばれない。
        // チェックをコメントアウトすると本通りのエラーメッセージが表示される。
        runner.startBiddingIn(auction, auction2);
        auction.hasReceivedJoinRequestFrom(ApplicationRunner.SNIPER_XMPP_ID);
        auction2.hasReceivedJoinRequestFrom(ApplicationRunner.SNIPER_XMPP_ID);

        auction.reportPrice(1000, 98, "other bidder");
        auction.hasReceivedBid(1098, ApplicationRunner.SNIPER_XMPP_ID);

        auction2.reportPrice(500, 21, "other bidder");
        auction2.hasReceivedBid(521, ApplicationRunner.SNIPER_XMPP_ID);

        auction.reportPrice(1098, 97, ApplicationRunner.SNIPER_XMPP_ID);
        auction2.reportPrice(521, 22, ApplicationRunner.SNIPER_XMPP_ID);

        runner.hasShownSniperIsWinning(auction, 1098);
        runner.hasShownSniperIsWinning(auction2, 521);

        auction.announceClosed();
        auction2.announceClosed();

        runner.hasShownSniperHasWonAuction(auction, 1098);
        runner.hasShownSniperHasWonAuction(auction2, 521);
    }
}
