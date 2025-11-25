package com.avianmc.sprocket.world;

import java.util.Random;

public class WorldSeed {
    private static long seed = System.currentTimeMillis();
    private static long lastRandom = seed;

    public static void setSeed(long newSeed) {
        seed = newSeed;
        lastRandom = newSeed;
    }

    public static long getSeed() {
        return seed;
    }

    public static Random random() {
        Random r = new Random(lastRandom);
        lastRandom = r.nextLong();
        return r;
    }
}
