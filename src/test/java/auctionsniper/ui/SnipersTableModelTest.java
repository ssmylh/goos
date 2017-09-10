package auctionsniper.ui;

import auctionsniper.SniperSnapshot;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import static auctionsniper.SniperState.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class SnipersTableModelTest {
    @Rule
    public final JUnitRuleMockery context = new JUnitRuleMockery();
    private TableModelListener listener = context.mock(TableModelListener.class);
    private final SnipersTableModel model = new SnipersTableModel(SniperSnapshot.joining("item id"));

    @Before
    public void attachModelListener() {
        model.addTableModelListener(listener);
    }

    @Test
    public void hasEnoughColumns() {
        assertThat(model.getColumnCount(), is(Column.values().length));
    }

    @Test
    public void setsSniperValuesInColumns() {
        context.checking(new Expectations() {
            {
                oneOf(listener).tableChanged(with(aRowChangedEvent()));
            }
        });

        SniperSnapshot snapshot = new SniperSnapshot("item id", 555, 666, BIDDING);
        model.sniperStateChanged(snapshot);

        assertColumnEquals(Column.ITEM_IDENTIFIER, "item id");
        assertColumnEquals(Column.LAST_PRICE, 555);
        assertColumnEquals(Column.LAST_BID, 666);
        assertColumnEquals(Column.SNIPER_STATE, SnipersTableModel.textFor(BIDDING));
    }

    private Matcher<TableModelEvent> aRowChangedEvent() {
        return Matchers.samePropertyValuesAs(new TableModelEvent(model, 0));
    }

    private void assertColumnEquals(Column column, Object expected) {
        int rowIndex = 0;
        int columnIndex = column.ordinal();
        assertThat(model.getValueAt(rowIndex, columnIndex), is(expected));
    }
}