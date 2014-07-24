package net.year4000.ramtweaks.messages;

import com.ewized.utilities.bungee.util.MessageUtil;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.year4000.ducktape.bungee.DuckTape;

import java.util.concurrent.TimeUnit;

public class ShutdownMessage implements Runnable {
    private ScheduledTask task;
    private ProxyServer proxy = ProxyServer.getInstance();
    private CommandSender console = proxy.getConsole();
    private int countdown;

    public ShutdownMessage(int time) {
        countdown = time;
        task = proxy.getScheduler().schedule(DuckTape.get(), this, 1, 1, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        String type = countdown > 1 ? "restart.countdown.plural" : "restart.countdown.single";

        if (countdown == 0) {

            console.sendMessage(MessageUtil.makeMessage(new Message(console).get("restart.message")));
            proxy.getPlayers().stream()
                .filter(p -> p != null && p.getLocale() != null && p.getServer() != null)
                .forEach(player -> player.disconnect(MessageUtil.makeMessage(new Message(player).get("restart.message"))));
            task.cancel();
            proxy.stop();
        }
        else if (countdown > 0) {
            console.sendMessage(MessageUtil.makeMessage(new Message(console).get(type, countdown)));
            proxy.getPlayers().stream()
                .filter(p -> p != null && p.getLocale() != null && p.getServer() != null)
                .forEach(player -> player.sendMessage(MessageUtil.makeMessage(new Message(player).get(type, countdown))));
        }
        countdown--;
    }
}
