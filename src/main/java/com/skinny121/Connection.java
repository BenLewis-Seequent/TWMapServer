package com.skinny121;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Logger;

public class Connection {
    private static final Logger logger = Logger.getLogger("TWMapServer");
    private static int counter = 1;
    private final Socket socket;
    private final Map map;

    private boolean closeRequested;
    public Connection(Socket socket, Map map){
        this.socket = socket;
        this.map = map;
    }

    public void run(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DataInputStream inputStream =
                            new DataInputStream(socket.getInputStream());
                    DataOutputStream outputStream =
                            new DataOutputStream(socket.getOutputStream());
                    while (!closeRequested){
                       readPacket(inputStream, outputStream);
                    }
                } catch (IOException e) {
                    logger.severe(e.toString());
                } finally {
                    try {
                        socket.getOutputStream().write(new byte[]{0});
                    } catch (IOException e) {}
                    try {
                        socket.close();
                    } catch (IOException e) {
                        logger.severe(e.toString());
                    }
                }
            }
        });
        thread.setDaemon(true);
        thread.setName("Connection " + counter++);
        thread.start();
    }

    private void readPacket(DataInputStream inputStream, DataOutputStream outputStream)
            throws IOException{
        byte tag = inputStream.readByte();
        switch (tag){
            case 0:
                logger.info("Close Requested");
            default:
                if(tag!=0)
                    logger.info("Unrecognised packet id");
        }


    }

    public void close(){
        closeRequested = true;
    }
}
