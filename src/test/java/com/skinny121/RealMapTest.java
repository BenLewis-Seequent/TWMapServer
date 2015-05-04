package com.skinny121;

import com.skinny121.nbt.*;
import org.junit.*;

import java.io.ByteArrayInputStream;

/**
 * Test class that uses a proper file produced by the Tall Worlds mod.
 */
public class RealMapTest {
    private Map map;
    @Before
    public void setUp(){
        map = new Map("/Users/BenLewis/Desktop/TallWorlds/saves/New World/cubes.dim0.db");
    }

    @After
    public void tearDown(){
        map.close();
    }

    @Test
    public void chunkTest(){
        System.out.println(map.getChunks());
        System.out.println(map.getChunks().size());
    }

    @Test
    public void chunkData(){
        byte[] data = map.getChunk(-7, -4, -6);
        CompoundTag t = (CompoundTag)Importer.importNBT(new ByteArrayInputStream(data));

        Assert.assertEquals(t.getTag("x"), new IntTag("x", -7));
        Assert.assertEquals(t.getTag("y"), new IntTag("y", -4));
        Assert.assertEquals(t.getTag("z"), new IntTag("z", -6));

        System.out.println(NBTToJson.toJson(t, new NBTToJson.Options(true)));
    }

    @Test
    public void columnTest(){
        System.out.println(map.getColumns());
        System.out.println(map.getColumns().size());
    }

    @Test
    public void columnData(){
        byte[] data = map.getColumn(12, 11);
        CompoundTag t = (CompoundTag)Importer.importNBT(new ByteArrayInputStream(data));

        Assert.assertEquals(t.getTag("x"), new IntTag("x", 12));
        Assert.assertEquals(t.getTag("z"), new IntTag("z", 11));

        System.out.println(NBTToJson.toJson(t, new NBTToJson.Options(true)));
    }
}
