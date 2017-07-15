package auctionsniper;

import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.packet.Message;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;
import org.jxmpp.jid.EntityBareJid;

public class AuctionMessageTranslatorTest {
    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();

    public static final Chat UNUSED_CHAT = null;
    public static final EntityBareJid UNUSED_JID = null;
    private final AuctionEventListener listener = context.mock(AuctionEventListener.class);
    private final AuctionMessageTranslator translator = new AuctionMessageTranslator(listener);

    @Test
    public void notifiesAuctionClosedWhenCloseMessageReceived() {
        context.checking(new Expectations() {
            {
                oneOf(listener).auctionClosed();
            }
        });

        Message message = new Message();
        message.setBody("SOLVersion: 1.1; Event: CLOSE;");

        translator.newIncomingMessage(UNUSED_JID, message, UNUSED_CHAT);
    }

    @Test
    public void notifiesBidDetailsWhenCurrentPriceMessageReceived() {
        int price = 192;
        int increment = 7;
        context.checking(new Expectations(){
            {
                oneOf(listener).currentPrice(price, increment);
            }
        });

        Message message = new Message();
        message.setBody(String.format(
                "SOLVersion: 1.1; Event: PRICE; CurrentPrice: %d; Increment: %d; Bidder: Someoene else;",
                price, increment));

        translator.newIncomingMessage(UNUSED_JID, message, UNUSED_CHAT);
    }
}
