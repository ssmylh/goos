package auctionsniper.end2end;

import org.jivesoftware.smack.chat2.Chat;
import org.jxmpp.jid.EntityBareJid;

public class ChatPartner {
    public EntityBareJid jid;
    public Chat chat;
    public ChatPartner(EntityBareJid jid, Chat chat) {
        this.jid = jid;
        this.chat = chat;
    }
}
