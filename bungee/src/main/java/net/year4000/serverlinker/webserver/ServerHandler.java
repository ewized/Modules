package net.year4000.serverlinker.webserver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.year4000.ducktape.bungee.DuckTape;
import net.year4000.serverlinker.ServerLinker;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ServerHandler extends AbstractHandler {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public void handle(String s, Request request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException, ServletException {
        httpResponse.setContentType("text/json;charset=utf-8");
        httpResponse.addHeader("Refresh", "30"); // let browsers update
        httpResponse.setStatus(HttpServletResponse.SC_OK);
        request.setHandled(true);
        gson.toJson(StatusCollection.get().getServers(), httpResponse.getWriter());
    }

    public static ScheduledTask startWebServer() {
        return ProxyServer.getInstance().getScheduler().runAsync(DuckTape.get(), () -> {
            try {
                Server webServer = new Server(5555);
                webServer.setHandler(new ServerHandler());
                webServer.start();
                webServer.join();

                // Loop to keep the server running
                while (true);
            } catch (Exception e) {
                ServerLinker.debug(e, false);
            }
        });
    }
}
