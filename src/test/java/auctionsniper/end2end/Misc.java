package auctionsniper.end2end;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.junit.Ignore;
import org.junit.Test;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class Misc {
    @Ignore
    @Test
    public void xxx() throws Exception {
        AbstractXMPPConnection conn1 = null;
        AbstractXMPPConnection conn2 = null;
        try {
            XMPPTCPConnectionConfiguration config1 = XMPPTCPConnectionConfiguration.builder()
                    .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                    .setUsernameAndPassword("auction-item-54321", "auction")
                    .setResource("Auction")
                    .setXmppDomain("localhost")
                    .setHostAddress(InetAddress.getLocalHost())
                    .setPort(5222)
                    .setDebuggerEnabled(true)
                    .build();
            conn1 = new XMPPTCPConnection(config1);
            conn1.connect();
            conn1.login();

            ChatManager chatManager1 = ChatManager.getInstanceFor(conn1);
            List<Chat> chats = new ArrayList<>();
            chatManager1.addIncomingListener((EntityBareJid entityBareJid, Message message, Chat chat) -> {
                chats.add(chat);
                System.out.println(message.getBody());
            });


            XMPPTCPConnectionConfiguration config2 = XMPPTCPConnectionConfiguration.builder()
                    .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                    .setUsernameAndPassword("sniper", "sniper")
                    .setResource("Sniper")
                    .setXmppDomain("localhost")
                    .setHostAddress(InetAddress.getLocalHost())
                    .setPort(5222)
                    .setDebuggerEnabled(true)
                    .build();
            conn2 = new XMPPTCPConnection(config2);
            conn2.connect();
            conn2.login();
            ChatManager chatManager2 = ChatManager.getInstanceFor(conn2);
            EntityBareJid jid = JidCreate.entityBareFrom("auction-item-54321@localhost");
            Chat chat = chatManager2.chatWith(jid);
            chat.send("hoge");

            Thread.sleep(1000);
        } finally {
            if (conn1 != null) {
                conn1.disconnect();
            }
            if (conn2 != null) {
                conn2.disconnect();
            }
        }
    }
}