package net.year4000.hubitems.messages;

import com.ewized.utilities.bukkit.util.BukkitLocale;
import org.bukkit.entity.Player;

public class Message extends BukkitLocale {
    public Message(Player player) {
        super(player);
        localeManager = MessageManager.get();
    }
}
