package auctionsniper;

/**
 * 下記警告と、P192のアクションバーへのアイテムIDの置換でエラーとなりテストが実行出来ない事へのとりあえずの回避策。
 * `WARNING: could not load keyboard layout Mac-JP, using fallback layout with reduced capabilities`
 *
 * 参考 : https://stackoverflow.com/questions/23316432/windowlicker-is-not-working-on-os-x
 */
public class WindowLickerWorkaround {
    public static void fix() {
        System.setProperty("com.objogate.wl.keyboard", "Mac-GB");
    }
}
