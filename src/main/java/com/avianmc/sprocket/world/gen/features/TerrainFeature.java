package com.avianmc.sprocket.world.gen.features;

import com.avianmc.sprocket.tile.Tiles;
import com.avianmc.sprocket.world.Chunk;
import com.avianmc.sprocket.world.World;
import com.avianmc.sprocket.world.WorldSeed;
import com.avianmc.sprocket.world.gen.FeatureContext;
import com.avianmc.sprocket.world.gen.WorldFeature;
import com.raylabz.opensimplex.OpenSimplexNoise;

public class TerrainFeature extends WorldFeature {

    private final OpenSimplexNoise noise;
    private final double baseScale;

    private final int octaves = 4;
    private final double persistence = 0.5;

    private static final double SCALE_MULTIPLIER = 20.0;

    public TerrainFeature(double scale) {
        this.noise = new OpenSimplexNoise(WorldSeed.getSeed());
        this.baseScale = scale;
    }

    @Override
    public void apply(World world, FeatureContext ctx) {

        int width = ctx.worldWidth;
        int height = ctx.worldHeight;

        final int layers = Chunk.LAYERS;
        final int topLayer = layers - 1;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                double nx = x * baseScale * SCALE_MULTIPLIER;
                double ny = y * baseScale * SCALE_MULTIPLIER;

                double n = fbm(nx, ny);

                int terrainHeight = (int) ((n + octaves) / (2 * octaves) * topLayer);

                if (terrainHeight < 0) terrainHeight = 0;
                if (terrainHeight > topLayer) terrainHeight = topLayer;

                for (int z = 0; z <= terrainHeight; z++) {
                    world.set(x, y, z, Tiles.GRASS);
                }
            }
        }
    }

    private double fbm(double x, double y) {
        double total = 0.0;
        double frequency = 1.0;
        double amplitude = 1.0;

        for (int i = 0; i < octaves; i++) {
            double sample = noise.getNoise2D(x * frequency, y * frequency).getValue();
            total += sample * amplitude;

            amplitude *= persistence;
            frequency *= 2.0;
        }

        return total;
    }
}
