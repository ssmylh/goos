package auctionsniper.xmpp;

import auctionsniper.Auction;
import auctionsniper.AuctionEventListener;
import auctionsniper.util.Announcer;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jxmpp.jid.EntityBareJid;

public class XMPPAuction implements Auction {
    public static final String JOIN_COMMAND_FORMAT = "SOLVersion: 1.1; Command: JOIN;";
    public static final String BID_COMMAND_FORMAT = "SOLVersion: 1.1; Command: BID; Price: %d;";

    private Announcer<AuctionEventListener> auctionEventListeners = Announcer.to(AuctionEventListener.class);
    private ChatManager manager;
    private Chat chat;

    public XMPPAuction(XMPPConnection connection, EntityBareJid jid) {
        manager = ChatManager.getInstanceFor(connection);
        chat = manager.chatWith(jid);

        AuctionMessageTranslator translator = new AuctionMessageTranslator(
                connection.getUser().toString(), // `XMPPConnection.connection` は `EntityFullJid` を返す。
                auctionEventListeners.announce());
        manager.addIncomingListener(translator);
        addAuctionEventListener(chatDisconnectorFor(translator));
    }

    @Override
    public void addAuctionEventListener(AuctionEventListener listener) {
        auctionEventListeners.addListener(listener);
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

    private AuctionEventListener chatDisconnectorFor(AuctionMessageTranslator translator) {
        return new AuctionEventListener() {
            @Override
            public void auctionClosed() { }
            @Override
            public void currentPrice(int price, int increment, PriceSource priceSource) { }
            @Override
            public void auctionFailed() {
                manager.removeListener(translator);
            }
        };
    }

}
