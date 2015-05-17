/*
Copyright (c) 2015, Ben Lewis
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.skinny121;

import com.google.common.base.Throwables;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

public class MapServer {
    private static final Logger logger = LogManager.getLogger();
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
                        logger.error(Throwables.getStackTraceAsString(e));
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
            logger.error(Throwables.getStackTraceAsString(e));
        }
        try {
            logger.info("Closing server");
            server.close();
        } catch (IOException e) {
            logger.error(Throwables.getStackTraceAsString(e));
        }
    }

}
