package auctionsniper.end2end;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class FakeAuctionServer {
    public static final String ITEM_ID_AS_LOGIN = "auction-%s";
    public static final String AUCTION_RESOURCE = "Auction";
    public static final String AUCTION_PASSWORD = "auction";

    static final String XMPP_DOMAIN_NAME = "localhost";
    static final String XMPP_HOST_NAME = "localhost";
    static final int XMPP_PORT = 5222;

    private String itemId;
    private AbstractXMPPConnection connection;
    private SingleIncomingListener incomingListener = new SingleIncomingListener();

    public FakeAuctionServer(String itemId) throws XmppStringprepException, UnknownHostException {
        this.itemId = itemId;
        XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                .setUsernameAndPassword(String.format(ITEM_ID_AS_LOGIN, itemId), AUCTION_PASSWORD)
                .setResource(AUCTION_RESOURCE)
                .setXmppDomain(XMPP_DOMAIN_NAME)
                .setHostAddress(InetAddress.getByName(XMPP_HOST_NAME))
                .setPort(XMPP_PORT)
                .setDebuggerEnabled(true)
                .build();
        connection = new XMPPTCPConnection(config);
    }

    public void stop() {
        if (connection != null) {
            connection.disconnect();
        }
    }

    public void startSellingItem() throws InterruptedException, XMPPException, SmackException, IOException {
        connection.connect();
        connection.login();

        ChatManager manager = ChatManager.getInstanceFor(connection);
        manager.addIncomingListener(incomingListener);
    }

    public void announceClosed() {
        incomingListener.getChat().ifPresent((chat) -> {
            try {
                chat.send("");
            } catch (SmackException.NotConnectedException ignored) {
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void hasReceivedJoinRequestFromSniper() throws InterruptedException {
        incomingListener.receiveMessage();
    }

    public String getItemId() {
        return itemId;
    }

    class SingleIncomingListener implements IncomingChatMessageListener {

        private ArrayBlockingQueue<Message> messages = new ArrayBlockingQueue<>(1);
        private List<Chat> chats = new ArrayList<>();

        public void receiveMessage() throws InterruptedException {
            assertThat(messages.poll(5, TimeUnit.SECONDS), is(notNullValue()));
        }

        @Override
        public void newIncomingMessage(EntityBareJid entityBareJid, Message message, Chat chat) {
            messages.add(message);
            synchronized (chats) {
                chats.add(chat);
            }
        }

        public Optional<Chat> getChat() {
            synchronized (chats) {
                return chats.stream().findFirst();
            }
        }
    }
}
