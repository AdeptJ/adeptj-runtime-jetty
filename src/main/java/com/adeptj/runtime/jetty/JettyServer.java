package com.adeptj.runtime.jetty;

import com.adeptj.runtime.jetty.handler.ContextPathHandler;
import com.adeptj.runtime.jetty.handler.HealthCheckHandler;
import com.adeptj.runtime.kernel.AbstractServer;
import com.adeptj.runtime.kernel.SciInfo;
import com.adeptj.runtime.kernel.ServerRuntime;
import com.adeptj.runtime.kernel.ServletDeployment;
import com.adeptj.runtime.kernel.ServletInfo;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.servlet.ServletContainerInitializerHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import static org.eclipse.jetty.servlet.ServletContextHandler.SECURITY;
import static org.eclipse.jetty.servlet.ServletContextHandler.SESSIONS;

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
        HttpConfiguration httpConfig = new HttpConfiguration();
        httpConfig.setOutputBufferSize(32768);
        httpConfig.setRequestHeaderSize(8192);
        httpConfig.setResponseHeaderSize(8192);
        httpConfig.setSendServerVersion(true);
        httpConfig.setSendDateHeader(false);
        ServerConnector connector = new ServerConnector(this.jetty, new HttpConnectionFactory(httpConfig));
        connector.setPort(8080);
        connector.setIdleTimeout(30000);
        this.jetty.addConnector(connector);
        this.context = new ServletContextHandler(SESSIONS | SECURITY);
        this.context.setContextPath("/");
        SciInfo sciInfo = deployment.getSciInfo();
        this.context.addServletContainerInitializer(new ServletContainerInitializerHolder(sciInfo.getSciInstance(),
                sciInfo.getHandleTypesArray()));
        this.registerServlets(deployment.getServletInfos());
        new SecurityConfigurer().configure(this.context);
        this.jetty.setHandler(this.createRootHandler(this.context));
        try {
            this.jetty.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Handler createRootHandler(ServletContextHandler servletContextHandler) {
        ResourceHandler resourceHandler = this.createResourceHandler();
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[]{new HealthCheckHandler(), new GzipHandler(), resourceHandler, servletContextHandler});
        ContextPathHandler contextPathHandler = new ContextPathHandler();
        contextPathHandler.setHandler(handlers);
        return contextPathHandler;
    }

    private ResourceHandler createResourceHandler() {
        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setDirectoriesListed(false);
        resourceHandler.setBaseResource(Resource.newClassPathResource("/WEB-INF"));
        return resourceHandler;
    }

    @Override
    public void postStart() {
        super.postStart();
        try {
            this.jetty.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
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