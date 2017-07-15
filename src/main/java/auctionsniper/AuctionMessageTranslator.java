package auctionsniper;

import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jxmpp.jid.EntityBareJid;

public class AuctionMessageTranslator implements IncomingChatMessageListener {
    private AuctionEventListener listener;
    public AuctionMessageTranslator(AuctionEventListener listener) {
        this.listener = listener;
    }

    @Override
    public void newIncomingMessage(EntityBareJid entityBareJid, Message message, Chat chat) {
        listener.auctionClosed();
    }
}
