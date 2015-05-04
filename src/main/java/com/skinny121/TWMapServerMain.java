package com.skinny121;

import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Scanner;
import java.util.logging.Logger;

public class TWMapServerMain {
    private static final Logger logger = Logger.getLogger("TWMapServer");
    public static void main(String[] args) throws Exception{
        if(args.length!=2) {
            logger.severe("Require two arguments one for the file name the other for the port number.");
            return;
        }
        final Map map = new Map(args[0]);
        int port = Integer.valueOf(args[1]);

        MapServer mapServer = new MapServer(port, map);

        mapServer.accept();

        Scanner scanner = new Scanner(System.in);
        boolean quit = false;
        while(!quit && scanner.hasNextLine()){
           if(scanner.nextLine().matches("(q|Q)uit|(e|E)xit")){
               quit = true;
           }
        }

        mapServer.close();
        map.close();
    }
}
