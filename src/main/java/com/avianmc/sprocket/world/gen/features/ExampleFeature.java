package com.avianmc.sprocket.world.gen.features;

import com.avianmc.sprocket.tile.Tiles;
import com.avianmc.sprocket.tile.Tile;
import com.avianmc.sprocket.world.World;
import com.avianmc.sprocket.world.WorldSeed;
import com.avianmc.sprocket.world.gen.FeatureContext;
import com.avianmc.sprocket.world.gen.WorldFeature;
import com.raylabz.opensimplex.OpenSimplexNoise;

public class ExampleFeature extends WorldFeature {

    private final OpenSimplexNoise noise;
    private final double baseScale;
    private final double threshold;
    private final int octaves = 4;
    private final double persistence = 0.5;

    private static final double SCALE_MULTIPLIER = 20.0;

    public ExampleFeature(double threshold, double scale) {
        this.noise = new OpenSimplexNoise(WorldSeed.getSeed());
        this.threshold = threshold;
        this.baseScale = scale;
    }

    @Override
    public void apply(World world, FeatureContext ctx) {
        int width  = ctx.worldWidth;
        int height = ctx.worldHeight;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                double nx = x * baseScale * SCALE_MULTIPLIER;
                double ny = y * baseScale * SCALE_MULTIPLIER;

                double n = fbm(nx, ny);

                // move -1..1 -> 0..1
                n = (n + 1.0) * 0.5;

                if (n > threshold) {
                    // in your engine: apply to world, not raw 2D array
                    world.set(x, y, 1, Tiles.GRASS);
                }
            }
        }
    }

    private double fbm(double x, double y) {
        double total = 0;
        double frequency = 1.0;
        double amplitude = 1.0;
        double maxValue = 0.0;

        for (int i = 0; i < octaves; i++) {

            double sample = noise.getNoise2D(x * frequency, y * frequency).getValue();
            total += sample * amplitude;

            maxValue += amplitude;
            amplitude *= persistence;
            frequency *= 2.0;
        }

        return total / maxValue;
    }
}
