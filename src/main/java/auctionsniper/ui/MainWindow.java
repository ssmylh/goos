package auctionsniper.ui;

import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame {
    public static final String MAIN_WINDOW_NAME = "Auction Sniper Main";
    public static final String SNIPER_STATUS_NAME = "sniper status";

    private static final String SNIPERS_TABLE_NAME = "Snipers Table";
    private final SnipersTableModel snipers;

    public MainWindow(SnipersTableModel snipers) {
        super("Auction Sniper");
        setName(MAIN_WINDOW_NAME);
        this.snipers = snipers;// makeSnipersTableの呼び出しより前に設定すること。
        fillContentPane(makeSnipersTable());
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void fillContentPane(JTable snipersTable) {
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        contentPane.add(new JScrollPane(snipersTable), BorderLayout.CENTER);
    }

    private JTable makeSnipersTable() {
        JTable sniepesJTable = new JTable(snipers);
        sniepesJTable.setName(SNIPERS_TABLE_NAME);
        return sniepesJTable;
    }
}
