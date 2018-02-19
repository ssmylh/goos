package auctionsniper.xmpp;

import auctionsniper.AuctionEventListener;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.packet.Message;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;
import org.jxmpp.jid.EntityBareJid;

import static auctionsniper.AuctionEventListener.PriceSource.*;

public class AuctionMessageTranslatorTest {
    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();

    private static final String SNIPER_XMPP_ID = "sniper xmpp id";
    public static final Chat UNUSED_CHAT = null;
    public static final EntityBareJid UNUSED_JID = null;
    private final AuctionEventListener listener = context.mock(AuctionEventListener.class);
    private final XMPPFailureReporter failureReporter = context.mock(XMPPFailureReporter.class);
    private final AuctionMessageTranslator translator = new AuctionMessageTranslator(SNIPER_XMPP_ID, listener, failureReporter);

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
    public void notifiesBidDetailsWhenCurrentPriceMessageReceivedFromOtherBidder() {
        int price = 192;
        int increment = 7;
        context.checking(new Expectations(){
            {
                oneOf(listener).currentPrice(price, increment, FromOtherBidder);
            }
        });

        Message message = new Message();
        message.setBody(String.format(
                "SOLVersion: 1.1; Event: PRICE; CurrentPrice: %d; Increment: %d; Bidder: Someoene else;",
                price, increment));

        translator.newIncomingMessage(UNUSED_JID, message, UNUSED_CHAT);
    }

    @Test
    public void notifiesBidDetailsWhenCurrentPriceMessageReceivedFromSniper() {
        int price = 234;
        int increment = 5;
        context.checking(new Expectations(){
            {
                oneOf(listener).currentPrice(price, increment, FromSniper);
            }
        });

        Message message = new Message();
        message.setBody(String.format(
                "SOLVersion: 1.1; Event: PRICE; CurrentPrice: %d; Increment: %d; Bidder: %s;",
                price, increment, SNIPER_XMPP_ID));

        translator.newIncomingMessage(UNUSED_JID, message, UNUSED_CHAT);
    }

    @Test
    public void notifiesAuctionFailedWhenBadMessageReceived() {
        String badMessage = "a bad message";
        expectFailureWithMessage(badMessage);

        translator.newIncomingMessage(UNUSED_JID, message(badMessage), UNUSED_CHAT);
    }

    @Test
    public void notifiesAuctionFailedWhenEventTypeMissing() {
        String badMessage = "SOLVersion: 1.1; CurrentPrice: 234; Increment: 5; Bidder: " + SNIPER_XMPP_ID + ";";
        expectFailureWithMessage(badMessage);

        translator.newIncomingMessage(UNUSED_JID, message(badMessage), UNUSED_CHAT);
    }

    private Message message(String body) {
        Message message = new Message();
        message.setBody(body);
        return message;
    }

    private void expectFailureWithMessage(final String badMessage) {
        context.checking(new Expectations() {
            {
                oneOf(listener).auctionFailed();
                oneOf(failureReporter).cannotTranslateMessage(
                        with(SNIPER_XMPP_ID), with(badMessage),
                        with(any(Exception.class)));
            }
        });
    }
}
