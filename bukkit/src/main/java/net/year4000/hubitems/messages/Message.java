package net.year4000.hubitems.messages;

import net.year4000.utilities.bukkit.BukkitLocale;
import org.bukkit.entity.Player;

import java.util.Locale;

public class Message extends BukkitLocale {
    public Message(Player player) {
        super(player);
        localeManager = MessageManager.get();
    }

    public Message(Locale localea) {
        super(null);
        localeManager = MessageManager.get();
        locale = localeManager.isLocale(localea.toString()) ? localea.toString() : DEFAULT_LOCALE;
    }
}
