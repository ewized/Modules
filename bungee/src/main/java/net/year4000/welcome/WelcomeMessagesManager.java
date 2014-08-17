package net.year4000.welcome;

import net.year4000.utilities.cache.QuickCache;
import net.year4000.utilities.locale.URLLocaleManager;

public class WelcomeMessagesManager extends URLLocaleManager {
    private static QuickCache<WelcomeMessagesManager> inst = QuickCache.builder(WelcomeMessagesManager.class).build();
    private static String url = "https://git.year4000.net/year4000/locales/raw/master/net/year4000/hub/locales/";

    public WelcomeMessagesManager() {
        super(Welcome.getLog(), url, parseJson(url + LOCALES_JSON));
    }

    public static WelcomeMessagesManager get() {
        return inst.get();
    }
}
