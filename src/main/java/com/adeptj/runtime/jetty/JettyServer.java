package com.adeptj.runtime.jetty;

import com.adeptj.runtime.kernel.AbstractServer;
import com.adeptj.runtime.kernel.SciInfo;
import com.adeptj.runtime.kernel.ServerRuntime;
import com.adeptj.runtime.kernel.ServletDeployment;
import com.adeptj.runtime.kernel.ServletInfo;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContainerInitializerHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

public class JettyServer extends AbstractServer {

    private Server jetty;

    private ServletContextHandler context;

    @Override
    public ServerRuntime getRuntime() {
        return ServerRuntime.JETTY;
    }

    @Override
    public void start(String[] args, ServletDeployment deployment) {
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
        SciInfo sciInfo = deployment.getSciInfo();
        this.context.addServletContainerInitializer(new ServletContainerInitializerHolder(sciInfo.getSciInstance(),
                sciInfo.getHandleTypesArray()));
        this.registerServlets(deployment.getServletInfos());
        this.jetty.setHandler(this.context);
        try {
            this.jetty.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
    protected void doRegisterServlet(ServletInfo info) {
        this.context.addServlet(new ServletHolder(info.getServletName(), info.getServletClass()), info.getPath());
    }
}