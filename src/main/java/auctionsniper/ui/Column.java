package auctionsniper.ui;

public enum Column {
    /** これがテーブルの並び順 */
    ITEM_IDENTIFIER,
    LAST_PRICE,
    LAST_BID,
    SNIPER_STATE;

    public static Column at(int offset) {
        return values()[offset];
    }
}
