package auctionsniper;

import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jxmpp.jid.EntityBareJid;

import java.util.HashMap;
import java.util.Map;

import static auctionsniper.AuctionEventListener.*;
import static auctionsniper.AuctionEventListener.PriceSource.*;

public class AuctionMessageTranslator implements IncomingChatMessageListener {
    private String sniperXMPPId;
    private AuctionEventListener listener;

    public AuctionMessageTranslator(String sniperXMPPId, AuctionEventListener listener) {
        this.sniperXMPPId = sniperXMPPId;
        this.listener = listener;
    }

    @Override
    public void newIncomingMessage(EntityBareJid entityBareJid, Message message, Chat chat) {
        AuctionEvent event = AuctionEvent.from(message.getBody());

        String type = event.type();
        if ("CLOSE".equals(type)) {
            listener.auctionClosed();
        } else if ("PRICE".equals(type)) {
            listener.currentPrice(event.currentPrice(), event.increment(), event.isFrom(sniperXMPPId));
        }
    }

    private static class AuctionEvent {
        private Map<String, String> fields = new HashMap<>();

        public String type() {
            return fields.get("Event");
        }

        public int currentPrice() {
            return Integer.parseInt(fields.get("CurrentPrice"));
        }

        public int increment() {
            return Integer.parseInt(fields.get("Increment"));
        }

        public String bidder() {
            return fields.get("Bidder");
        }

        public PriceSource isFrom(String sniperXMPPId) {
            return sniperXMPPId.equals(bidder()) ? FromSniper : FromOtherBidder;
        }

        private void addField(String field) {
            String[] pair = field.split(":");
            fields.put(pair[0].trim(), pair[1].trim());
        }

        static AuctionEvent from(String messageBody) {
            AuctionEvent event = new AuctionEvent();
            for (String field : fieldsIn(messageBody)) {
                event.addField(field);
            }
            return event;
        }

        private static String[] fieldsIn(String messageBody) {
            return messageBody.split(";");
        }
    }
}