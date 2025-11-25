package com.avianmc.sprocket.world;

import com.avianmc.sprocket.tile.Tile;
import com.avianmc.sprocket.tile.Tiles;

public class World {

    private final int chunksX;
    private final int chunksY;
    private final Chunk[][] chunks; // [cx][cy]

    public World(int chunksX, int chunksY) {
        if (chunksX <= 0 || chunksY <= 0) throw new IllegalArgumentException("World too small");
        this.chunksX = chunksX;
        this.chunksY = chunksY;
        this.chunks = new Chunk[chunksX][chunksY];
        for (int cx = 0; cx < chunksX; cx++) {
            for (int cy = 0; cy < chunksY; cy++) {
                this.chunks[cx][cy] = new Chunk();
            }
        }
    }

    public int getChunksX() { return chunksX; }
    public int getChunksY() { return chunksY; }

    public int getWidthInTiles() { return chunksX * Chunk.SIZE; }
    public int getHeightInTiles() { return chunksY * Chunk.SIZE; }

    public boolean inBounds(int x, int y, int layer) {
        return layer >= 0 && layer < Chunk.LAYERS && x >= 0 && x < getWidthInTiles() && y >= 0 && y < getHeightInTiles();
    }

    public Tile get(int x, int y, int layer) {
        if (!inBounds(x, y, layer)) return Tiles.AIR;
        int cx = x / Chunk.SIZE;
        int cy = y / Chunk.SIZE;
        int lx = x % Chunk.SIZE;
        int ly = y % Chunk.SIZE;
        return chunks[cx][cy].get(lx, ly, layer);
    }

    public void set(int x, int y, int layer, Tile tile) {
        if (!inBounds(x, y, layer)) return;
        int cx = x / Chunk.SIZE;
        int cy = y / Chunk.SIZE;
        int lx = x % Chunk.SIZE;
        int ly = y % Chunk.SIZE;
        chunks[cx][cy].set(lx, ly, layer, tile);
    }
}
