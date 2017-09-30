package auctionsniper.ui;

import auctionsniper.WindowLickerWorkaround;
import auctionsniper.end2end.AuctionSniperDriver;
import com.objogate.wl.swing.probe.ValueMatcherProbe;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.Matchers.*;

/**
 * integration test
 */
public class MainWindowTest {
    final SnipersTableModel snipers = new SnipersTableModel();
    final MainWindow mainWindow = new MainWindow(snipers);
    final AuctionSniperDriver driver = new AuctionSniperDriver(100);

    @BeforeClass
    public static void workaround4WindowLicker() {
        WindowLickerWorkaround.fix();
    }

    @Test
    public void makeUserRequestWhenJoinButtonClicked() {
        String itemId = "an item-id";
        ValueMatcherProbe<String> buttonProbe = new ValueMatcherProbe<>(equalTo(itemId), "join request");
        mainWindow.addUserRequestListener(_itemId -> buttonProbe.setReceivedValue(_itemId));

        driver.startBiddingFor(itemId);
        driver.check(buttonProbe);
    }
}
