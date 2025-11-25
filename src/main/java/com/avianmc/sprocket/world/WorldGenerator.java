package com.avianmc.sprocket.world;

import com.avianmc.sprocket.tile.Tiles;
import com.avianmc.sprocket.world.gen.FeatureContext;
import com.avianmc.sprocket.world.gen.WorldFeature;

import java.util.ArrayList;
import java.util.List;

public class WorldGenerator {

    private static final List<WorldFeature> FEATURES = new ArrayList<>();

    public static void registerFeature(WorldFeature feature) {
        FEATURES.add(feature);
    }

    public static World generateFlatWorld() {
        return generateFlatWorld(64, 64);
    }

    public static World generateFlatWorld(int chunksX, int chunksY) {

        World world = new World(chunksX, chunksY);
        int width = world.getWidthInTiles();
        int height = world.getHeightInTiles();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                world.set(x, y, 0, Tiles.GRASS);
            }
        }

        FeatureContext ctx = new FeatureContext(
                WorldSeed.random(),  // deterministic random
                width,
                height
        );

        for (WorldFeature feature : FEATURES) {
            feature.apply(world, ctx);

            ctx.random = WorldSeed.random();
        }

        return world;
    }
}
