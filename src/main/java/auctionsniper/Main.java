package auctionsniper;

import auctionsniper.ui.MainWindow;
import auctionsniper.ui.SnipersTableModel;
import auctionsniper.ui.SwingThreadSniperListener;
import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static final int ARG_HOST_NAME = 0;
    private static final int ARG_PORT = 1;
    private static final int ARG_XMPP_DOMAIN_NAME = 2;
    private static final int ARG_USERNAME = 3;
    private static final int ARG_PASSWORD = 4;
    public static final String AUCTION_RESOURCE = "Auction";
    public static final String ITEM_ID_AS_LOGIN = "auction-%s";
    public static final String AUCTION_ID_FORMAT = ITEM_ID_AS_LOGIN + "@%s/" + AUCTION_RESOURCE;
    public static final String JOIN_COMMAND_FORMAT = "SOLVersion: 1.1; Command: JOIN;";
    public static final String BID_COMMAND_FORMAT = "SOLVersion: 1.1; Command: BID; Price: %d;";

    private final SnipersTableModel snipers;
    private volatile MainWindow ui;
    @SuppressWarnings("unused")
    private List<Chat> notToBeGCd = new ArrayList<>();

    public Main(String itemId) throws Exception {
        snipers = new SnipersTableModel(SniperSnapshot.joining(itemId));
        startUserInterface(snipers);
    }

    public static void main(String... args) throws Exception {
        int itemIdStartIndex = 5;
        Main main = new Main(args[itemIdStartIndex]);// TODO 本では引数が存在しない。
        AbstractXMPPConnection connection = connectTo(
                args[ARG_HOST_NAME],
                Integer.parseInt(args[ARG_PORT]),
                args[ARG_XMPP_DOMAIN_NAME],
                args[ARG_USERNAME],
                args[ARG_PASSWORD]);

        main.disconnectWhenUICloses(connection);

        for (int i = itemIdStartIndex; i < args.length; i++) {
            main.joinAuction(connection, auctionJid(args[i], args[ARG_XMPP_DOMAIN_NAME]), args[i]);
        }
    }

    private void startUserInterface(SnipersTableModel snipers) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            this.ui = new MainWindow(snipers);
        });
    }

    private static AbstractXMPPConnection connectTo(String hostName, int port, String xmpppDomainName, String username, String password) throws IOException, InterruptedException, SmackException, XMPPException {
        XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                .setHostAddress(InetAddress.getByName(hostName))
                .setPort(port)
                .setXmppDomain(xmpppDomainName)
                .setUsernameAndPassword(username, password)
                .setResource(AUCTION_RESOURCE)
                .build();
        AbstractXMPPConnection connection = new XMPPTCPConnection(config);
        connection.connect();
        connection.login();
        return connection;
    }

    private static EntityBareJid auctionJid(String itemId, String xmppDomainName) throws XmppStringprepException {
        return JidCreate.entityBareFrom(String.format(AUCTION_ID_FORMAT, itemId, xmppDomainName));
    }

    private void joinAuction(AbstractXMPPConnection connection, EntityBareJid auctionJid, String itemId) {
        ChatManager manager = ChatManager.getInstanceFor(connection);
        Chat chat = manager.chatWith(auctionJid);
        notToBeGCd.add(chat);

        Auction auction = new XMPPAuction(chat);

        // TODO `AuctionSniper` 内の `SniperSnapshot` と、`SnipersTableModel` 内のそれが重複している。
        manager.addIncomingListener(new AuctionMessageTranslator(connection.getUser().toString(), // `AbstractXMPPConnection.connection` は `EntityFullJid` を返す。
                new AuctionSniper(itemId, auction, new SwingThreadSniperListener(snipers))));
        auction.join();
    }

    private void disconnectWhenUICloses(AbstractXMPPConnection connection) {
        ui.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                connection.disconnect();
            }
        });
    }

    public static class XMPPAuction implements Auction {
        private Chat chat;

        public XMPPAuction(Chat chat) {
            this.chat = chat;
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
    }
}
