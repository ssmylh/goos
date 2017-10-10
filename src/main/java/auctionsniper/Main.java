package auctionsniper;

import auctionsniper.ui.MainWindow;
import auctionsniper.xmpp.XMPPAuctionHouse;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Main {
    private static final int ARG_HOST_NAME = 0;
    private static final int ARG_PORT = 1;
    private static final int ARG_XMPP_DOMAIN_NAME = 2;
    private static final int ARG_USERNAME = 3;
    private static final int ARG_PASSWORD = 4;

    private final ConnectionConfig config;
    private final SniperPortfolio portfolio = new SniperPortfolio();
    private volatile MainWindow ui;

    public Main(ConnectionConfig config) throws Exception {
        this.config = config;
        startUserInterface();
        addUserRequestListenerFor();
    }

    static class ConnectionConfig {
        public final String hostName;
        public final int port;
        public final String xmppDomainName;
        public final String userName;
        public final String password;
        public ConnectionConfig(String hostName, int port, String xmppDomainName, String userName, String password) {
            this.hostName = hostName;
            this.port = port;
            this.xmppDomainName = xmppDomainName;
            this.userName = userName;
            this.password = password;
        }
    }

    public static void main(String... args) throws Exception {
        ConnectionConfig config = new ConnectionConfig(
                args[ARG_HOST_NAME],
                Integer.parseInt(args[ARG_PORT]),
                args[ARG_XMPP_DOMAIN_NAME],
                args[ARG_USERNAME],
                args[ARG_PASSWORD]
                );
        new Main(config);
    }

    private void startUserInterface() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            this.ui = new MainWindow(portfolio);
        });
    }

    private void addUserRequestListenerFor() {
        ui.addUserRequestListener(itemId -> {
            try {
                // 本の `Smack` : `Chat`毎にリスナーを設定
                // 4.2.0 : `ChatManager`に対してリスナーを設定。`Chat`毎に対してリスナーを設定出来ない。
                // と、APIが異なり、挙動も異なる。
                // 本通りに、`Chat`毎に管理したいので、以下の様にアイテム追加毎にコネクションと`Chat`をラップする`XMPPAuctionHouse`を生成する。
                // - コネクション生成時にリソース名(アイテムID)を付加する(オークションからのメッセージの送信先を分ける)。
                // - それぞれのコネクションから生成される`ChatManager`に対して`IncomingChatMessageListener`を設定する。
                XMPPAuctionHouse auctionHouse = XMPPAuctionHouse.connect(
                        config.hostName,
                        config.port,
                        config.xmppDomainName,
                        config.userName,
                        config.password,
                        itemId);
                disconnectWhenUICloses(auctionHouse);
                new SniperLauncher(auctionHouse, portfolio).joinAuction(itemId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void disconnectWhenUICloses(XMPPAuctionHouse auctionHouse) {
        ui.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                auctionHouse.disconnect();
            }
        });
    }
}
