package com.adeptj.runtime.jetty;

import com.adeptj.runtime.kernel.AbstractServer;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContainerInitializerHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.http.HttpServlet;

public class JettyServer extends AbstractServer {

    private org.eclipse.jetty.server.Server jetty;

    @Override
    public void start(String[] args, ServletContainerInitializer sci, Class<?>... classes) {
        int maxThreads = 100;
        int minThreads = 10;
        int idleTimeout = 120;
        QueuedThreadPool threadPool = new QueuedThreadPool(maxThreads, minThreads, idleTimeout);
        this.jetty = new org.eclipse.jetty.server.Server(threadPool);
        ServerConnector connector = new ServerConnector(this.jetty);
        connector.setPort(8080);
        this.jetty.setConnectors(new Connector[]{connector});
        ServletHandler servletHandler = new ServletHandler();
        servletHandler.addServletWithMapping(GreetingServlet.class, "/greet");
        ServletContextHandler servletContextHandler = new ServletContextHandler();
        servletContextHandler.addServletContainerInitializer(new ServletContainerInitializerHolder(sci, classes));
        servletHandler.setHandler(servletContextHandler);
        this.jetty.setHandler(servletHandler);
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
    public void registerServlets(HttpServlet... servlets) {
        Handler handler = this.jetty.getHandler();
    }
}