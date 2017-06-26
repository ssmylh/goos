package auctionsniper;

import auctionsniper.ui.MainWindow;
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
import java.io.IOException;
import java.net.InetAddress;

public class Main {
    private static final int ARG_HOST_NAME = 0;
    private static final int ARG_PORT = 1;
    private static final int ARG_XMPP_DOMAIN_NAME = 2;
    private static final int ARG_USERNAME = 3;
    private static final int ARG_PASSWORD = 4;
    private static final int ARG_ITEM_ID = 5;
    public static final String AUCTION_RESOURCE = "Auction";
    public static final String ITEM_ID_AS_LOGIN = "auction-%s";
    public static final String AUCTION_ID_FORMAT = ITEM_ID_AS_LOGIN + "@%s/" + AUCTION_RESOURCE;

    private volatile MainWindow ui;

    public Main() throws Exception {
        startUserInterface();
    }

    public static void main(String... args) throws Exception {
        Main main = new Main();
        AbstractXMPPConnection connection = connectTo(
                args[ARG_HOST_NAME],
                Integer.parseInt(args[ARG_PORT]),
                args[ARG_XMPP_DOMAIN_NAME],
                args[ARG_USERNAME],
                args[ARG_PASSWORD]);

        ChatManager manager = ChatManager.getInstanceFor(connection);
        EntityBareJid auctionJid = auctionJid(args[ARG_ITEM_ID], args[ARG_XMPP_DOMAIN_NAME]);
        Chat chat = manager.chatWith(auctionJid);
        chat.send("");
    }

    private void startUserInterface() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            this.ui = new MainWindow();
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
}
