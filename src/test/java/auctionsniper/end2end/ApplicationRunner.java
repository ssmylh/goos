package auctionsniper.end2end;

import auctionsniper.Main;
import auctionsniper.ui.MainWindow;
import auctionsniper.ui.SnipersTableModel;

import static auctionsniper.SniperState.*;

public class ApplicationRunner {
    public static final String SNIPER_ID = "sniper";
    public static final String SNIPER_PASSWORD = "sniper";
    public static final String SNIPER_XMPP_ID = SNIPER_ID + "@" + FakeAuctionServer.XMPP_HOST_NAME + "/" + Main.AUCTION_RESOURCE;
    private AuctionSniperDriver driver;

    public void startBiddingIn(FakeAuctionServer auction) {
        Thread thread = new Thread("Test Application") {
            @Override
            public void run() {
                try {
                    Main.main(
                            FakeAuctionServer.XMPP_HOST_NAME,
                            String.valueOf(FakeAuctionServer.XMPP_PORT),
                            FakeAuctionServer.XMPP_DOMAIN_NAME,
                            SNIPER_ID,
                            SNIPER_PASSWORD,
                            auction.getItemId());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        thread.setDaemon(true);
        thread.start();
        driver = new AuctionSniperDriver(1000);
        driver.hasTitle(MainWindow.APPLICATION_TITLE);
        driver.hasColumnTitles();
        driver.showsSniperStatus(auction.getItemId(), 0, 0, SnipersTableModel.textFor(JOINING));
    }

    public void hasShownSniperIsBidding(FakeAuctionServer auction, int lastPlace, int lastBid) {
        driver.showsSniperStatus(auction.getItemId(), lastPlace, lastBid, SnipersTableModel.textFor(BIDDING));
    }

    public void hasShownSniperHasLostAuction(FakeAuctionServer auction, int lastPrice, int lastBid) {
        driver.showsSniperStatus(auction.getItemId(), lastPrice, lastBid, SnipersTableModel.textFor(LOST));
    }

    public void hasShownSniperIsWinning(FakeAuctionServer auction, int winningBid) {
        driver.showsSniperStatus(auction.getItemId(), winningBid, winningBid, SnipersTableModel.textFor(WINNING));
    }

    public void hasShownSniperHasWonAuction(FakeAuctionServer auction, int lastPrice) {
        driver.showsSniperStatus(auction.getItemId(), lastPrice, lastPrice, SnipersTableModel.textFor(WON));
    }

    public void stop() {
        if (driver != null) {
            driver.dispose();
        }
    }
}
