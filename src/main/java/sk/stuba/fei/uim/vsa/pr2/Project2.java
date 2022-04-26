package sk.stuba.fei.uim.vsa.pr2;


import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.UriBuilder;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Project2 {

    public static final Logger LOGGER = Logger.getLogger(Project2.class.getName());
    public static final String BASE_URI = "http://localhost/";
    public static final int PORT = 8080;
    public static final Class<? extends Application> APPLICATION_CLASS = null; // TODO sem dosaď vlastnú triedu

    public static void main(String[] args) {
        try {
            final HttpServer server = startServer();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    System.out.println("Shutting down the application...");
                    server.shutdownNow();
                    System.out.println("Exiting");
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, null, e);
                }
            }));
            System.out.println("Last steps of setting up the application...");
            postStart();
            System.out.println(String.format("Application started.%nStop the application using CRL+C"));
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            LOGGER.log(Level.SEVERE, null, e);
        }
    }

    public static HttpServer startServer() {
        final ResourceConfig config = ResourceConfig.forApplicationClass(APPLICATION_CLASS);
        URI baseUri = UriBuilder.fromUri(BASE_URI).port(PORT).build();
        LOGGER.info("Starting Grizzly2 HTTP server...");
        LOGGER.info("Server listening on " + BASE_URI + ":" + PORT);
        return GrizzlyHttpServerFactory.createHttpServer(baseUri, config);
    }

    public static void postStart() {
        // TODO sem napíš akékoľvek nastavenia, či volania, ktoré sa majú udiať ihneď po štarte
    }

}
