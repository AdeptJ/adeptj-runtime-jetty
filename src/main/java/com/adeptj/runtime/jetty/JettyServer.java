package com.adeptj.runtime.jetty;

import com.adeptj.runtime.kernel.AbstractServer;
import com.adeptj.runtime.kernel.SciInfo;
import com.adeptj.runtime.kernel.ServerName;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContainerInitializerHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import javax.servlet.http.HttpServlet;
import java.util.List;

public class JettyServer extends AbstractServer {

    private Server jetty;

    private ServletContextHandler context;

    @Override
    public ServerName getName() {
        return ServerName.JETTY;
    }

    @Override
    public void start(String[] args, SciInfo sciInfo) {
        int maxThreads = 100;
        int minThreads = 10;
        int idleTimeout = 120;
        QueuedThreadPool threadPool = new QueuedThreadPool(maxThreads, minThreads, idleTimeout);
        this.jetty = new Server(threadPool);
        ServerConnector connector = new ServerConnector(this.jetty);
        connector.setPort(8080);
        this.jetty.addConnector(connector);
        this.context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        this.context.setContextPath("/");
        this.context.addServletContainerInitializer(new ServletContainerInitializerHolder(sciInfo.getSciInstance(),
                sciInfo.getHandleTypesArray()));
        ServletHolder greetingServlet = new ServletHolder("GreetingServlet", GreetingServlet.class);
        this.context.addServlet(greetingServlet, "/greet");
        this.jetty.setHandler(this.context);
        try {
            this.jetty.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        this.doStart(args, "AdeptJ Jetty Terminator");
    }

    @Override
    public void stop() {
        try {
            this.jetty.stop();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void registerServlets(List<Class<? extends HttpServlet>> servlets) {
        int count = 0;
        for (Class<? extends HttpServlet> servlet : servlets) {
            ServletHolder greetingServlet = new ServletHolder(servlet.getSimpleName(), servlet);
            this.context.addServlet(greetingServlet, "/servlet" + count);
            count++;
        }
    }
}