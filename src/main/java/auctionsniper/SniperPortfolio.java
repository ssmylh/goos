package auctionsniper;

import auctionsniper.util.Announcer;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

public class SniperPortfolio implements SniperCollector {
    private Announcer<PortfolioListener> announcer = Announcer.to(PortfolioListener.class);
    private List<AuctionSniper> snipers = new ArrayList<>();

    public interface PortfolioListener extends EventListener {
        void sniperAdded(AuctionSniper sniper);
    }

    @Override
    public void addSniper(AuctionSniper sniper) {
        snipers.add(sniper);
        announcer.announce().sniperAdded(sniper);
    }

    public void addPortfolioListener(PortfolioListener listener) {
        announcer.addListener(listener);
    }
}
