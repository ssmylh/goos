package auctionsniper.end2end;

import auctionsniper.Main;
import auctionsniper.ui.MainWindow;
import auctionsniper.ui.SnipersTableModel;

import static auctionsniper.SniperState.*;

public class ApplicationRunner {
    public static final String SNIPER_ID = "sniper";
    public static final String SNIPER_PASSWORD = "sniper";
    public static final String SNIPER_XMPP_ID_FORMAT = SNIPER_ID + "@" + FakeAuctionServer.XMPP_HOST_NAME + "/%s" ;
    private AuctionSniperDriver driver;

    public void startBiddingIn(FakeAuctionServer... auctions) {
        Thread thread = new Thread("Test Application") {
            @Override
            public void run() {
                try {
                    Main.main(arguments(auctions));
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

        for (FakeAuctionServer auction : auctions) {
            driver.showsSniperStatus(auction.getItemId(), 0, 0, SnipersTableModel.textFor(JOINING));
        }
    }

    static String[] arguments(FakeAuctionServer... auctions) {
        String[] arguments = new String[auctions.length + 5];
        arguments[0] = FakeAuctionServer.XMPP_HOST_NAME;
        arguments[1] = String.valueOf(FakeAuctionServer.XMPP_PORT);
        arguments[2] = FakeAuctionServer.XMPP_DOMAIN_NAME;
        arguments[3] = SNIPER_ID;
        arguments[4] = SNIPER_PASSWORD;
        for (int i = 0; i < auctions.length; i++) {
            arguments[i + 5] = auctions[i].getItemId();
        }
        return arguments;
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
