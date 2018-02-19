package auctionsniper.xmpp;

import auctionsniper.AuctionEventListener;
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
    private XMPPFailureReporter failureReporter;

    public AuctionMessageTranslator(String sniperXMPPId, AuctionEventListener listener, XMPPFailureReporter failureReporter) {
        this.sniperXMPPId = sniperXMPPId;
        this.listener = listener;
        this.failureReporter = failureReporter;
    }

    @Override
    public void newIncomingMessage(EntityBareJid entityBareJid, Message message, Chat chat) {
        try {
            AuctionEvent event = AuctionEvent.from(message.getBody());

            String type = event.type();
            if ("CLOSE".equals(type)) {
                listener.auctionClosed();
            } else if ("PRICE".equals(type)) {
                listener.currentPrice(event.currentPrice(), event.increment(), event.isFrom(sniperXMPPId));
            }
        } catch (Exception parseException) {
            failureReporter.cannotTranslateMessage(sniperXMPPId, message.getBody(), parseException);
            listener.auctionFailed();
        }
    }

    private static class AuctionEvent {
        private Map<String, String> fields = new HashMap<>();

        public String type() throws MissingValueException {
            return get("Event");
        }

        public int currentPrice() throws MissingValueException {
            return Integer.parseInt(get("CurrentPrice"));
        }

        public int increment() throws MissingValueException {
            return Integer.parseInt(get("Increment"));
        }

        public String bidder() throws MissingValueException {
            return get("Bidder");
        }

        private String get(String fieldName) throws MissingValueException {
            String value = fields.get(fieldName);
            if (value == null) {
                throw new MissingValueException(fieldName);
            }
            return value;
        }

        public PriceSource isFrom(String sniperXMPPId) throws MissingValueException {
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

        private static class MissingValueException extends Exception {
            public MissingValueException(String fieldName) {
                super("Missing value for " + fieldName);
            }
        }
    }
}