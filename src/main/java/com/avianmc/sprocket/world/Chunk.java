package com.avianmc.sprocket.world;

import com.avianmc.sprocket.tile.Tile;
import com.avianmc.sprocket.tile.Tiles;

/**
 * Represents a fixed-size chunk of tiles with multiple vertical layers.
 */
public class Chunk {
    public static final int SIZE = 16;
    public static final int LAYERS = 8;

    private final Tile[][][] tiles; // [layer][x][y]

    public Chunk() {
        tiles = new Tile[LAYERS][SIZE][SIZE];
        for (int z = 0; z < LAYERS; z++) {
            for (int x = 0; x < SIZE; x++) {
                for (int y = 0; y < SIZE; y++) {
                    tiles[z][x][y] = Tiles.AIR;
                }
            }
        }
    }

    public boolean inBounds(int x, int y, int layer) {
        return layer >= 0 && layer < LAYERS && x >= 0 && x < SIZE && y >= 0 && y < SIZE;
    }

    public Tile get(int x, int y, int layer) {
        if (!inBounds(x, y, layer)) return Tiles.AIR;
        return tiles[layer][x][y];
    }

    public void set(int x, int y, int layer, Tile tile) {
        if (!inBounds(x, y, layer)) return;
        tiles[layer][x][y] = tile == null ? Tiles.AIR : tile;
    }
}
