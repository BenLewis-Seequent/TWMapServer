package com.skinny121;

import com.google.common.base.Throwables;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class MapServer {
    private static final Logger logger = Logger.getLogger("TWMapServer");
    private final ServerSocket server;
    private final Map map;
    private volatile boolean closeRequested = false;
    private final List<Connection> connections = new ArrayList<>();
    public MapServer(int port, Map map) throws IOException{
        server = new ServerSocket(port);
        server.setSoTimeout(100);
        this.map = map;
    }

    public void accept(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(!closeRequested) {
                    try {
                        Socket socket = server.accept();
                        connections.add(new Connection(socket, map).run());
                    } catch (SocketTimeoutException e) {
                    } catch (IOException e) {
                        logger.severe(Throwables.getStackTraceAsString(e));
                    }
                }
                for(Connection connection:connections){
                    connection.close();
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public void close(){
        closeRequested = true;
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            logger.severe(Throwables.getStackTraceAsString(e));
        }
        try {
            logger.info("Closing server");
            server.close();
        } catch (IOException e) {
            logger.severe(Throwables.getStackTraceAsString(e));
        }
    }

}
