package auctionsniper.ui;

import auctionsniper.SniperSnapshot;
import org.junit.Test;

import static auctionsniper.SniperState.*;
import static auctionsniper.ui.Column.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class ColumnTest {
    @Test
    public void retrievesValuesFromASniperSnapshot() {
        String itemId = "item id";
        int lastPrice = 123;
        int lastBid = 45;
        SniperSnapshot snapshot = new SniperSnapshot(itemId, lastPrice, lastBid, BIDDING);
        assertThat(ITEM_IDENTIFIER.valueIn(snapshot), is(itemId));
        assertThat(LAST_PRICE.valueIn(snapshot), is(lastPrice));
        assertThat(LAST_BID.valueIn(snapshot), is(lastBid));
        assertThat(SNIPER_STATE.valueIn(snapshot), is(SnipersTableModel.textFor(BIDDING)));
    }
}
