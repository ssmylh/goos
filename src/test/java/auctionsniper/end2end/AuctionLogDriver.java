package auctionsniper.end2end;

import org.apache.commons.io.FileUtils;
import org.hamcrest.Matcher;

import java.io.File;
import java.io.IOException;
import java.util.logging.LogManager;

import static auctionsniper.xmpp.XMPPAuctionHouse.*;
import static org.junit.Assert.*;

public class AuctionLogDriver {
    private final File logFile = new File(LOG_FILE_NAME);

    public void hasEntry(Matcher<String> matcher) throws IOException {
        assertThat(FileUtils.readFileToString(logFile, "UTF-8"), matcher);
    }

    public void clearLog() {
        logFile.delete();
        LogManager.getLogManager().reset();
    }
}
