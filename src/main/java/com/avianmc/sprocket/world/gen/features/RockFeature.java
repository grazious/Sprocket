package com.avianmc.sprocket.world.gen.features;

import com.avianmc.sprocket.tile.Tiles;
import com.avianmc.sprocket.tile.Tile;
import com.avianmc.sprocket.world.Chunk;
import com.avianmc.sprocket.world.World;
import com.avianmc.sprocket.world.WorldSeed;
import com.avianmc.sprocket.world.gen.FeatureContext;
import com.avianmc.sprocket.world.gen.WorldFeature;

import java.util.Random;

public class RockFeature extends WorldFeature {

    private final double chance;
    private final Random random;

    public RockFeature(double chance) {
        this.chance = chance;
        this.random = WorldSeed.random();
    }

    @Override
    public void apply(World world, FeatureContext ctx) {
        int width  = ctx.worldWidth;
        int height = ctx.worldHeight;
        int topLayer = Chunk.LAYERS - 1;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                if (random.nextDouble() > chance) continue;

                for (int z = topLayer; z >= 0; z--) {
                    Tile current = world.get(x, y, z);
                    if (current == Tiles.GRASS) {
                        int treeLayer = Math.min(z + 1, topLayer);
                        if (world.get(x, y, treeLayer) == Tiles.AIR) {
                            world.set(x, y, treeLayer, Tiles.ROCK);
                        }
                        break;
                    }
                }
            }
        }
    }
}
