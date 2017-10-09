package auctionsniper.end2end;

import auctionsniper.xmpp.XMPPAuction;
import org.hamcrest.Matcher;
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
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class FakeAuctionServer {
    public static final String ITEM_ID_AS_LOGIN = "auction-%s";
    public static final String AUCTION_RESOURCE = "Auction";
    public static final String AUCTION_PASSWORD = "auction";

    public static final String XMPP_DOMAIN_NAME = "localhost";
    public static final String XMPP_HOST_NAME = "localhost";
    public static final int XMPP_PORT = 5222;

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
        incomingListener.getChatPartner().ifPresent((partner) -> {
            try {
                partner.chat.send("SOLVersion: 1.1; Event: CLOSE");
            } catch (SmackException.NotConnectedException ignored) {
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void hasReceivedJoinRequestFrom(String sniperId) throws InterruptedException {
        receivesAMessageMatching(sniperId, is(XMPPAuction.JOIN_COMMAND_FORMAT));
    }

    public void hasReceivedBid(int bid, String sniperId) throws InterruptedException {
        receivesAMessageMatching(sniperId, is(String.format(XMPPAuction.BID_COMMAND_FORMAT, bid)));
    }

    private void receivesAMessageMatching(String sniperId, Matcher<? super String> messageMatcher) throws InterruptedException {
        incomingListener.receiveAMessage(messageMatcher);

        ChatPartner partner = incomingListener.getChatPartner().get();

        // `IncomingChatMessageListener`では`EntityFullJid`を引数に取らない(`EntityBareJid`を取る)ので、`Resource Name`(itemId)を付加して比較する。
        assertThat(partner.jid.asEntityBareJidString() + "/" + itemId, is(sniperId));
    }

    public String getItemId() {
        return itemId;
    }

    public void reportPrice(int price, int increment, String bidder) {
        incomingListener.getChatPartner().ifPresent((partner) -> {
            try {
                String message = String.format(
                        "SOLVersion: 1.1; Event: PRICE; CurrentPrice: %d; Increment: %d; Bidder: %s;",
                        price, increment, bidder);
                partner.chat.send(message);
            } catch (SmackException.NotConnectedException ignored) {
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    class SingleIncomingListener implements IncomingChatMessageListener {

        private ArrayBlockingQueue<Message> messages = new ArrayBlockingQueue<>(1);
        private ChatPartner chatPartner;
        private byte[] lock = new byte[0];

        public void receiveAMessage(Matcher<? super String> messageMatcher) throws InterruptedException {
            Message message = messages.poll(5, TimeUnit.SECONDS);
            assertThat("Message", message, is(notNullValue()));
            assertThat(message, hasProperty("body", messageMatcher));
        }

        @Override
        public void newIncomingMessage(EntityBareJid entityBareJid, Message message, Chat chat) {
            synchronized (lock) {
                chatPartner = new ChatPartner(entityBareJid, chat);
            }
            messages.add(message);
        }

        public Optional<ChatPartner> getChatPartner() {
            synchronized (lock) {
                return Optional.ofNullable(chatPartner);
            }
        }
    }
}
