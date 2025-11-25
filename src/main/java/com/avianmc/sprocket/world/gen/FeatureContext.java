package com.avianmc.sprocket.world.gen;

import java.util.Random;

public class FeatureContext {

    public Random random;
    public final int worldWidth;
    public final int worldHeight;

    public FeatureContext(Random random, int worldWidth, int worldHeight) {
        this.random = random;
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
    }
}
