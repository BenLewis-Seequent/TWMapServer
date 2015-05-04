package com.skinny121;

import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentNavigableMap;

public class Map {
    private final DB database;
    private ConcurrentNavigableMap<Long, byte[]> chunks;
    private ConcurrentNavigableMap<Long, byte[]> cols;


    public Map(String filename){
        database = DBMaker.newFileDB(new File(filename))
                .closeOnJvmShutdown().make();
        chunks = database.getTreeMap("chunks");
        cols = database.getTreeMap("columns");
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
