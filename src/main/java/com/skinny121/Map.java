package com.skinny121;

import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.io.File;

public class Map {
    private final DB database;

    public Map(String filename){
        database = DBMaker.newFileDB(new File(filename))
                .closeOnJvmShutdown().make();
    }

    public void close(){
        database.close();
    }
}
