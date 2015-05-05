package com.skinny121;

import org.junit.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class MapTest {
    private Map map;
    private Path path;
    @Before
    public void setUp() throws IOException{
        path = Files.createTempDirectory(null);
        map = new Map(path.resolve("test.db").toString());
    }

    @After
    public void tearDown() throws IOException{
        map.close();
        Files.delete(path.resolve("test.db"));
        Files.delete(path.resolve("test.db.p"));
        Files.delete(path.resolve("test.db.t"));
        Files.delete(path);
    }


    @Test
    public void saveChunkTest(){
        Assert.assertFalse(map.containsChunk(2, -1, -3));
        Assert.assertEquals(0, map.getChunks().size());
        map.saveChunk(2, -1, -3, new byte[]{0, 2});
        Assert.assertTrue(map.containsChunk(2, -1, -3));
        Assert.assertEquals(1, map.getChunks().size());
        Assert.assertEquals(map.new Pos(2, -1, -3), map.getChunks().get(0));
        Assert.assertArrayEquals(new byte[]{0, 2}, map.getChunk(2, -1, -3));
        Assert.assertFalse(map.containsChunk(2, -1, 3));
    }

    @Test
    public void saveColumnTest(){
        Assert.assertFalse(map.containsColumn(-3, 1));
        Assert.assertEquals(0, map.getColumns().size());
        map.saveColumn(-3, 1, new byte[]{0, 2, -1});
        Assert.assertTrue(map.containsColumn(-3, 1));
        Assert.assertEquals(1, map.getColumns().size());
        Assert.assertEquals(map.new Pos(-3, 0,  1), map.getColumns().get(0));
        Assert.assertArrayEquals(new byte[]{0, 2, -1}, map.getColumn(-3, 1));
        Assert.assertFalse(map.containsColumn(-3, 5));
    }
}
