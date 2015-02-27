package net.year4000.servermenu;

import lombok.Getter;
import net.year4000.ducktape.bukkit.module.BukkitModule;
import net.year4000.ducktape.bukkit.module.ModuleListeners;
import net.year4000.ducktape.bukkit.utils.SchedulerUtil;
import net.year4000.ducktape.module.ModuleInfo;
import net.year4000.servermenu.menus.MenuManager;
import net.year4000.servermenu.message.Message;
import net.year4000.utilities.Callback;
import net.year4000.utilities.bukkit.ItemUtil;
import net.year4000.utilities.bukkit.MessagingChannel;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

@ModuleInfo(
    name = "ServerMenu",
    version = "1.0",
    description = "The menu that lets you connect to the servers",
    authors = {"Year4000"}
)
@ModuleListeners({MenuListener.class, BungeeSender.class})
public class ServerMenu extends BukkitModule {
    @Getter
    private static ServerMenu inst;
    @Getter
    private MessagingChannel connector;

    @Override
    public void load() {
        inst = this;
    }

    @Override
    public void enable() {
        // async thread that pull the data and updates the menus
        SchedulerUtil.repeatAsync(new APIFetcher(), 2, TimeUnit.SECONDS);

        connector = MessagingChannel.get();
    }

    /** Generate the close button in the player's locale */
    public static ItemStack closeButton(Player player) {
        return closeButton(new Locale(player.getLocale()));
    }

    /** Generate the close button in the specified locale */
    public static ItemStack closeButton(Locale locale) {
        return ItemUtil.makeItem("redstone_block", "{'display':{'name':'" + new Message(locale.toString()).get("menu.close") + "'}}");
    }
}
