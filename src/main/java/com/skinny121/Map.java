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

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentNavigableMap;

public class Map {
    private final DB database;
    private ConcurrentNavigableMap<Long, byte[]> chunks;
    private ConcurrentNavigableMap<Long, byte[]> cols;


    public Map(String filename){
        Path path = Paths.get(filename);
        if(Files.isDirectory(path)){
            path = path.resolve("cubes.dim0.db");
        }
        database = DBMaker.fileDB(path.toFile())
                .transactionEnable()
                .closeOnJvmShutdown().make();
        chunks = database.treeMap("chunks", Serializer.LONG, Serializer.BYTE_ARRAY).createOrOpen();
        cols = database.treeMap("columns", Serializer.LONG, Serializer.BYTE_ARRAY).createOrOpen();
    }

    /*
        This method is from the tall worlds source.
        https://github.com/TallWorlds/CubicChunks/blob/develop/src/main/cubicchunks/util/Bits.java
     */
    private static int unpackSigned(long packed, int size, int offset) {
        // first, offset to the far left and back so we can preserve the two's complement
        int complementOffset = 64 - offset - size;
        packed = packed << complementOffset >> complementOffset;

        // then unpack the integer
        packed = packed >> offset;
        return (int)packed;
    }

    private long getAddress(int x, int z){
        return getAddress(x, 0, z);
    }

    private long getAddress(int x, int y, int z){
        x = x & ((1<<22)-1);
        z = z & ((1<<22)-1);
        y = y & ((1<<20)-1);
        return ((long)y << 44) | ((long)x << 22) | ((long)z);
    }

    private int getX(long address){
        return unpackSigned(address, 22, 22);
    }

    private int getZ(long address){
        return unpackSigned(address, 22, 0);
    }

    private int getY(long address){
        return unpackSigned(address, 20, 44);
    }

    public byte[] getChunk(int x, int y, int z){
        return chunks.get(getAddress(x, y, z));
    }

    public List<Pos> getChunks(){
        List<Pos> poss = new ArrayList<>();
        for(long address:chunks.keySet()){
            poss.add(new Pos(address));
        }
        return poss;
    }

    public boolean containsChunk(int x, int y, int z){
        return chunks.containsKey(getAddress(x, y, z));
    }

    public void saveChunk(int x, int y, int z, byte[] data){
        chunks.put(getAddress(x, y, z), data);
    }

    public byte[] getColumn(int x, int z){
        return cols.get(getAddress(x, z));
    }

    public List<Pos> getColumns(){
        List<Pos> poss = new ArrayList<>();
        for(long address:cols.keySet()){
            poss.add(new Pos(address));
        }
        return poss;
    }

    public boolean containsColumn(int x, int z){
        return cols.containsKey(getAddress(x, z));
    }

    public void saveColumn(int x, int z, byte[] data){
        cols.put(getAddress(x, z), data);
    }

    public void save(){
        database.commit();
    }

    public void close(){
        database.close();
    }

    public final class Pos{
        public final int x;
        public final int y;
        public final int z;

        private Pos(long address){
            this(getX(address), getY(address), getZ(address));
        }

        public Pos(int x, int y, int z){
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Pos pos = (Pos) o;
            return pos.x == x && pos.y == y && pos.z ==z;
        }

        @Override
        public int hashCode() {
            int result = x;
            result = 31 * result + y;
            result = 31 * result + z;
            return result;
        }

        @Override
        public String toString() {
            return "("+x+","+y+","+z+")";
        }
    }
}
