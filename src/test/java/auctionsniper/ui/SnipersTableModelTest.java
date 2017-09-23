package auctionsniper.ui;

import auctionsniper.SniperSnapshot;
import org.hamcrest.Matcher;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import static auctionsniper.SniperState.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class SnipersTableModelTest {
    @Rule
    public final JUnitRuleMockery context = new JUnitRuleMockery();
    private TableModelListener listener = context.mock(TableModelListener.class);
    private final SnipersTableModel model = new SnipersTableModel();

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
        SniperSnapshot joining = SniperSnapshot.joining("item id");
        SniperSnapshot bidding = joining.bidding(555, 666);

        context.checking(new Expectations() {
            {
                allowing(listener).tableChanged(with(anyInsertionEvent()));

                oneOf(listener).tableChanged(with(aRowChangedEvent()));
            }
        });


        model.addSniper(joining);
        model.sniperStateChanged(bidding);

        assertColumnEquals(Column.ITEM_IDENTIFIER, "item id");
        assertColumnEquals(Column.LAST_PRICE, 555);
        assertColumnEquals(Column.LAST_BID, 666);
        assertColumnEquals(Column.SNIPER_STATE, SnipersTableModel.textFor(BIDDING));
    }

    @Test
    public void setsUpColumnHeadings() {
        for (Column column : Column.values()) {
            assertThat(model.getColumnName(column.ordinal()), is(column.name));
        }
    }

    @Test
    public void notifiesListenersWhenAddingASniper() {
        SniperSnapshot joining = SniperSnapshot.joining("item123");
        context.checking(new Expectations() {
            {
                oneOf(listener).tableChanged(with(anInsertionAtRow(0)));
            }
        });

        assertThat(model.getRowCount(), is(0));

        model.addSniper(joining);
        assertThat(model.getRowCount(), is(1));
        assertRowMatchesSnapshot(0, joining);
    }

    private Matcher<TableModelEvent> aRowChangedEvent() {
        return samePropertyValuesAs(new TableModelEvent(model, 0));
    }

    private void assertColumnEquals(Column column, Object expected) {
        int rowIndex = 0;
        int columnIndex = column.ordinal();
        assertThat(model.getValueAt(rowIndex, columnIndex), is(expected));
    }

    private Matcher<TableModelEvent> anyInsertionEvent() {
        return hasProperty("type", equalTo(TableModelEvent.INSERT));
    }

    private Matcher<TableModelEvent> anInsertionAtRow(int row) {
        return samePropertyValuesAs(new TableModelEvent(model, row, row, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT));
    }

    private void assertRowMatchesSnapshot(int row, SniperSnapshot snapshot) {
        assertThat(cellValue(row, Column.ITEM_IDENTIFIER), is(snapshot.itemId));
        assertThat(cellValue(row, Column.LAST_PRICE), is(snapshot.lastPrice));
        assertThat(cellValue(row, Column.LAST_BID), is(snapshot.lastBid));
        assertThat(cellValue(row, Column.SNIPER_STATE), is(SnipersTableModel.textFor(snapshot.state)));
    }

    private Object cellValue(int rowIndex, Column column) {
        return model.getValueAt(rowIndex, column.ordinal());
    }
}