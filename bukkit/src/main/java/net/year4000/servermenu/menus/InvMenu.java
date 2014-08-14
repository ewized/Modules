package net.year4000.servermenu.menus;

import com.google.common.base.Ascii;
import com.google.common.collect.ImmutableSet;
import lombok.Data;
import net.year4000.servermenu.BungeeSender;
import net.year4000.servermenu.Common;
import net.year4000.servermenu.ServerMenu;
import net.year4000.servermenu.Settings;
import net.year4000.servermenu.message.Message;
import net.year4000.servermenu.message.MessageManager;
import net.year4000.utilities.bukkit.BukkitUtil;
import net.year4000.utilities.bukkit.ItemUtil;
import net.year4000.utilities.bukkit.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
public class InvMenu {
    private static final Set<Material> ITEMS = ImmutableSet.of(Material.ARROW, Material.CHAINMAIL_HELMET, Material.BOW, Material.GLOWSTONE, Material.MINECART, Material.LAVA_BUCKET, Material.POTION, Material.EYE_OF_ENDER);
    private final String menu;
    private final String menuDisplay;
    private final String[] group;
    private final boolean players;
    private final boolean motd;
    private final MenuManager manager;
    private Map<Integer, Map<Locale, ItemStack[]>> pages;
    private Map<Locale, Inventory> views = new HashMap<>();
    private int serversCount;

    public InvMenu(MenuManager manager, boolean players, boolean motd, String menu, String... group) {
        this.manager = manager;
        this.players = players;
        this.motd = motd;
        this.menu = menu;
        this.group = group;

        serversCount = getServers().size();

        // The menu display / title
        String name = menu;
        try {
            name = serversCount > 0 ? getServers().get(0).getGroup().getDisplay() : menu;
        } finally {
            this.menuDisplay = name;
        }

        // create the menus for each locale
        String title = Ascii.truncate(MessageUtil.replaceColors("&8&l" + menuDisplay), 32, "...");
        Inventory invMenu = Bukkit.createInventory(null, menuSize(), title);

        MessageManager.get().getLocales().keySet().forEach(code -> views.put(code, invMenu));
        updateServers(true); // trigger update to show some servers on first view
    }

    /** Regenerate all the menus as we need to regenerate the menu size */
    public void regenerateMenuViews() {
        // get viewers

        // update menus

        // viewers get new views
        updateServers();
    }

    /** Get servers that are specific to this list */
    private List<ServerJson> getServers() {
        return getServers(false);
    }

    /** Get servers that are specific to this list */
    private List<ServerJson> getServers(boolean showHidden) {
        Predicate<? super ServerJson> hide = s -> s.getGroup().getName().equals(menu) && !s.isHidden();
        Predicate<? super ServerJson> all = s -> s.getGroup().getName().equals(menu);

        return manager.getServers().stream().filter(showHidden ? all : hide).collect(Collectors.toList());
    }

    /** Open the inventory that follows the locale code */
    public Inventory openMenu(String code) {
        return views.get(new Locale(MessageManager.get().isLocale(code) ? code : Message.DEFAULT_LOCALE));
    }

    // generate the menu size to use
    private int menuSize() {
        boolean oneGroup = group.length > 1;
        boolean shortMenu = serversCount < 9 && !oneGroup;
        return BukkitUtil.invBase(shortMenu ? serversCount : serversCount + (oneGroup ? 18 : 9));
    }

    // Update Servers //

    /** Update the inventory of the servers */
    public void updateServers() {
        updateServers(false);
    }

        /** Update the inventory of the servers */
    public void updateServers(boolean force) {
        views.forEach((locale, menu) -> {
            if (menu.getViewers().size() != 0 || force) {
                updateServers(locale, menu);
            }
        });
    }

    public void updateServers(Locale locale, Inventory menu) {
        String code = locale.toString();
        //ServerMenu.debug("UPDATE SERVERS LOCALE: " + menu.getTitle() + " " + code);
        boolean oneGroup = group.length > 1;
        boolean shortMenu = serversCount < 9 && !oneGroup;
        int invSize = menuSize();
        ItemStack[] items = new ItemStack[invSize];

        // Menu Bar
        if (oneGroup) {
            int count = -1;

            for (String item : group) {
                ServerJson.Group group = manager.getGroups().stream().filter(g -> g.getName().equals(item)).findAny().get();
                items[++count] = createItemBar(count, group, code, (int) manager.getServers().stream().filter(s -> s.getGroup().getName().equals(item)).count());
            }

            // Hub Icon
            if (Settings.get().isHub()) {
                InvMenu hubs = new InvMenu(manager, true, false, Settings.get().getHubGroup());
                ServerJson.Group group = manager.getGroups().stream().filter(g -> g.getName().equals(Settings.get().getHubGroup())).findAny().get();

                items[8] = createItemBar(ITEMS.size() - 1, group, code, hubs.serversCount);
            }
        }

        // Servers
        int servers = oneGroup ? 8 : -1;

        for (ServerJson server : getServers()) {
            items[++servers] = serverItem(code, server);
        }

        items[shortMenu ? 8 : invSize - 5] = ItemUtil.makeItem("redstone_block", "{'display':{'name':'" + new Message(code).get("menu.close") + "'}}");

        menu.setContents(items);
    }

    /** IS server size is not the same as the last one */
    public boolean needNewInventory() {
        return BukkitUtil.invBase(manager.getServers().size()) != BukkitUtil.invBase(serversCount);
    }

    // Create the menu items //

    /** Create the item in the menu bar */
    private ItemStack createItemBar(int count, ServerJson.Group menu, String code, int servers) {
        Message locale = new Message(code);
        ItemStack item = ItemUtil.makeItem(ITEMS.toArray(new Material[ITEMS.size()])[count].name());
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(MessageUtil.replaceColors("&a&l" + menu.getDisplay()));
        String menuName = this.menu;
        meta.setLore(new ArrayList<String>() {{
            add(locale.get("menu.servers", servers));

            if (!menu.getName().equals(menuName)) {
                add("");
                add(locale.get("menu.click"));
            }
        }});

        item.setItemMeta(meta);

        // glow
        if (this.menu.equals(menu.getName())) {
            return Common.addGlow(item);
        }

        return item;
    }

    /** Create the server icon */
    private ItemStack serverItem(String code, ServerJson server) {
        ItemStack item;
        Message locale = new Message(code);

        // Server is itself
        boolean self = server.getName().equals(BungeeSender.getCurrentServer());

        if (server.getStatus() != null) {
            int number = findNumber(server.getName());
            item = ItemUtil.makeItem(Material.STAINED_CLAY.name(), number, self ? (short) 5 : (short) 13);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(MessageUtil.replaceColors("&b&l" + server.getName()));
            meta.setLore(new ArrayList<String>() {{
                // Message of the day.
                if (motd) {
                    add(MessageUtil.replaceColors(server.getStatus().getDescription()));
                }

                // Player count.
                if (players) {
                    add(MessageUtil.message(
                        "&a%s&7/&6%s",
                        server.getStatus().getPlayers().getOnline(),
                        server.getStatus().getPlayers().getMax()
                    ));
                }

                // Status ect
                add(locale.get("server.online"));

                if (!self) {
                    add("");
                    add(locale.get("server.click"));
                }
            }});
            item.setItemMeta(meta);
        }
        //offline
        else {
            int number = findNumber(server.getName());
            item = ItemUtil.makeItem(Material.STAINED_CLAY.name(), number, self ? (short) 6 : (short) 14);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(MessageUtil.replaceColors("&b&l" + server.getName()));
            meta.setLore(Arrays.asList(locale.get("server.offline")));
            item.setItemMeta(meta);
        }

        return item;
    }

    /** Find the server's number */
    private int findNumber(String name) {
        int number = 1;

        for (String part : name.split(" ")) {
            try {
                number = Integer.parseInt(part) < 1 ? 1 : Integer.parseInt(part);
                break;
            } catch (Exception e) {
                // not a valid number skip
            }
        }

        return number;
    }
}
