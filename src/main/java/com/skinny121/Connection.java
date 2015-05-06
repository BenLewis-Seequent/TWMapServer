package com.skinny121;

import com.google.common.base.Throwables;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Logger;


/**
 * Packets:
 *
 *   bits
 *   -request 0/reply 1
 *   -indicates chunk 0/column 1
 *   -2 bits for id
 *   -result bit false 0/true 1
 *
 *   Packet id
 *
 *   close 0
 *      terminates the connection gracefully
 *
 *   contains 1
 *      followed by 2 or 3 ints
 *
 *      reply is a boolean that occurs in the packet id
 *   get      2
 *      followed by 2 or 3 ints
 *
 *      reply is a true result in the packet id and followed by array of bytes
 *      or false indicating not present
 *   save     3
 *      followed by a array of bytes
 */
public class Connection {
    private static final Logger logger = Logger.getLogger("TWMapServer");

    private static final int REQUEST_MASK = 0x10;
    private static final int CHUNK_MASK = 0x8;
    private static final int ID_MASK = 0x6;

    private static int counter = 1;
    private final Socket socket;
    private final Map map;

    private boolean closeRequested;
    public Connection(Socket socket, Map map){
        this.socket = socket;
        this.map = map;
    }

    public Connection run(){
        Thread thread = new Thread(() -> {
            try {
                DataInputStream inputStream =
                        new DataInputStream(socket.getInputStream());
                DataOutputStream outputStream =
                        new DataOutputStream(socket.getOutputStream());
                while (!closeRequested){
                   readPacket(inputStream, outputStream);
                }
            } catch (IOException e) {
                logger.severe(Throwables.getStackTraceAsString(e));
            } finally {
                try {
                    socket.getOutputStream().write(new byte[]{0});
                } catch (IOException e) {}
                try {
                    logger.info("Closing Socket");
                    socket.close();
                } catch (IOException e) {
                    logger.severe(Throwables.getStackTraceAsString(e));
                }
            }
        });
        thread.setDaemon(true);
        thread.setName("Connection " + counter++);
        thread.start();
        return this;
    }

    private void readPacket(DataInputStream in, DataOutputStream out)
            throws IOException{
        byte tag = in.readByte();
        if((tag & REQUEST_MASK) == 0) {
            boolean chunk = (tag & CHUNK_MASK) == 0;
            // packet id
            int id = (tag & ID_MASK) >> 1;
            switch (id) {
                case 0:
                    logger.info("Close Requested");
                    closeRequested = true;
                    break;
                case 1:   //contains
                    containsPacket(chunk, in, out);
                    break;
                case 2:   //get
                    getPacket(chunk, in, out);
                    break;
                case 3:   //save
                    savePacket(chunk, in, out);
                    break;
            }
        }else{
            logger.severe("Received an reply packet "+tag);
            closeRequested = true;
        }
    }

    private void containsPacket(boolean chunk, DataInputStream in, DataOutputStream out)
            throws IOException{
        int x = in.readInt();
        // the y or z coord based on chunk or column
        int yz = in.readInt();
        boolean result;
        if(chunk){
            int z = in.readInt();
            logger.info("Received packet contains("+x+", "+yz+", "+z+")");
            result = map.containsChunk(x, yz, z);
        }else{
            logger.info("Received packet contains("+x+", "+yz+")");
            result = map.containsColumn(x, yz);
        }
        out.writeByte(0x12 | (!chunk ? 0x8 : 0) | (result ? 0x1 : 0));
    }

    private void getPacket(boolean chunk, DataInputStream in, DataOutputStream out)
            throws IOException{
        int x = in.readInt();
        // the y or z coord based on chunk or column
        int yz = in.readInt();
        byte[] data=null;
        if(chunk){
            int z = in.readInt();
            logger.info("Received packet get("+x+", "+yz+", "+z+")");
            if(map.containsChunk(x, yz, z)){
                data = map.getChunk(x, yz, z);
            }
        }else if(map.containsColumn(x, yz)){
            logger.info("Received packet get("+x+", "+yz+")");
            data = map.getColumn(x, yz);
        }else{
            logger.info("Received packet get("+x+", "+yz+")");
        }
        out.writeByte(0x14 | (!chunk ? 0x8 : 0) | (data!=null ? 0x1 : 0));
        if(data != null) {
            // write out array
            out.writeInt(data.length);
            out.write(data);
        }
        logger.info("Sending " + (data == null ? 0:data.length) + " bytes");
    }

    private void savePacket(boolean chunk, DataInputStream in, DataOutputStream out)
            throws IOException{
        int x = in.readInt();
        // the y or z coord based on chunk or column
        int yz = in.readInt();
        int z = 0;
        if(chunk){
            z = in.readInt();
        }
        // read in array
        int len = in.readInt();
        byte[] data = new byte[len];
        in.readFully(data);
        // save chunk/column
        if(chunk){
            map.saveChunk(x, yz, z, data);
        }else {
            map.saveColumn(x, yz, data);
        }
    }

    public void close(){
        closeRequested = true;
    }
}
