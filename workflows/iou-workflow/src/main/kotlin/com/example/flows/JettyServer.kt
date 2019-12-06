package com.example.flows

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

class JettyServer {

    private val server: Server? = null

    @Throws(Exception::class)
    fun start(port: Int) {
        val maxThreads = 100
        val minThreads = 10
        val idleTimeout = 120
        val threadPool = QueuedThreadPool(maxThreads, minThreads, idleTimeout)
        val server = Server(threadPool)
        val connector = ServerConnector(server)
        connector.setPort(port)
        server.setConnectors(arrayOf<Connector>(connector))
        val servletHandler = ServletHandler()
        server.setHandler(servletHandler)
        server.start()
    }

    @Throws(Exception::class)
    fun stop() {
        server?.stop()
    }
}