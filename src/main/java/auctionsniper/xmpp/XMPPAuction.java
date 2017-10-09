package auctionsniper.xmpp;

import auctionsniper.Auction;
import auctionsniper.AuctionEventListener;
import auctionsniper.util.Announcer;
import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.IOException;
import java.net.InetAddress;

public class XMPPAuction implements Auction {
    public static final String AUCTION_RESOURCE = "Auction";
    public static final String ITEM_ID_AS_LOGIN = "auction-%s";
    public static final String AUCTION_ID_FORMAT = ITEM_ID_AS_LOGIN + "@%s/" + AUCTION_RESOURCE;
    public static final String JOIN_COMMAND_FORMAT = "SOLVersion: 1.1; Command: JOIN;";
    public static final String BID_COMMAND_FORMAT = "SOLVersion: 1.1; Command: BID; Price: %d;";

    private Announcer<AuctionEventListener> auctionEventListeners = Announcer.to(AuctionEventListener.class);
    private Chat chat;

    public XMPPAuction(XMPPConnection connection, String itemId) throws XmppStringprepException {
        ChatManager manager = ChatManager.getInstanceFor(connection);
        chat = manager.chatWith(auctionJid(itemId, connection.getXMPPServiceDomain().toString()));

        // TODO `AuctionSniper` 内の `SniperSnapshot` と、`SnipersTableModel` 内のそれが重複している。
        manager.addIncomingListener(
                new AuctionMessageTranslator(
                        connection.getUser().toString(), // `XMPPConnection.connection` は `EntityFullJid` を返す。
                        auctionEventListeners.announce()));
    }

    @Override
    public void addAuctionEventListener(AuctionEventListener listener) {
        auctionEventListeners.addListener(listener);
    }

    private EntityBareJid auctionJid(String itemId, String xmppDomainName) throws XmppStringprepException {
        return JidCreate.entityBareFrom(String.format(AUCTION_ID_FORMAT, itemId, xmppDomainName));
    }

    @Override
    public void bid(int amount) {
        sendMessage(String.format(BID_COMMAND_FORMAT, amount));
    }

    @Override
    public void join() {
        sendMessage(JOIN_COMMAND_FORMAT);
    }

    private void sendMessage(String message) {
        try {
            chat.send(message);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
}
