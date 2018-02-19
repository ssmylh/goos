package auctionsniper.xmpp;

import auctionsniper.Auction;
import auctionsniper.AuctionHouse;
import org.apache.commons.io.FilenameUtils;
import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.IOException;
import java.net.InetAddress;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class XMPPAuctionHouse implements AuctionHouse {
    public static final String AUCTION_RESOURCE = "Auction";
    public static final String ITEM_ID_AS_LOGIN = "auction-%s";
    public static final String AUCTION_ID_FORMAT = ITEM_ID_AS_LOGIN + "@%s/" + AUCTION_RESOURCE;
    private final AbstractXMPPConnection connection;

    private static final String LOGGER_NAME = "auction-sniper";
    public static final String LOG_FILE_NAME = "auction-sniper.log";
    private final XMPPFailureReporter failureReporter;


    public XMPPAuctionHouse(AbstractXMPPConnection connection) throws XMPPAuctionException {
        this.connection = connection;
        this.failureReporter = new LoggingXMPPFailureReporter(makeLogger());
    }

    public static XMPPAuctionHouse connect(String hostName, int port, String xmppDomainName, String username, String password, String resource) throws InterruptedException, XMPPException, SmackException, IOException, XMPPAuctionException {
        AbstractXMPPConnection connection = connection(hostName, port, xmppDomainName, username, password, resource);
        return new XMPPAuctionHouse(connection);
    }

    public static AbstractXMPPConnection connection(String hostName, int port, String xmppDomainName, String username, String password, String resource) throws IOException, InterruptedException, SmackException, XMPPException {
        XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                .setHostAddress(InetAddress.getByName(hostName))
                .setPort(port)
                .setXmppDomain(xmppDomainName)
                .setUsernameAndPassword(username, password)
                .setResource(resource)
                .build();
        AbstractXMPPConnection connection = new XMPPTCPConnection(config);
        connection.connect();
        connection.login();
        return connection;
    }

    private EntityBareJid auctionJid(String itemId) {
        try {
            return JidCreate.entityBareFrom(String.format(AUCTION_ID_FORMAT, itemId, connection.getXMPPServiceDomain().toString()));
        } catch (XmppStringprepException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public Auction auctionFor(String itemId) {
        return new XMPPAuction(connection, auctionJid(itemId), failureReporter);
    }

    public void disconnect() {
        connection.disconnect();
    }

    private Logger makeLogger() throws XMPPAuctionException {
        Logger logger = Logger.getLogger(LOGGER_NAME);
        logger.setUseParentHandlers(false);
        logger.addHandler(simpleFileHandler());
        return logger;
    }

    private FileHandler simpleFileHandler() throws XMPPAuctionException {
        try {
            FileHandler handler = new FileHandler(LOG_FILE_NAME);
            handler.setFormatter(new SimpleFormatter());
            return handler;
        } catch (Exception e) {
            throw new XMPPAuctionException("Could not create logger FileHandler "
                    + FilenameUtils.getFullPath(LOG_FILE_NAME), e);
        }
    }
}
