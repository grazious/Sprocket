package com.avianmc.sprocket.rule;

import com.avianmc.sprocket.world.World;

public abstract class Rule {
    private final String id;

    public Rule(String id) {
        this.id = id;
    }

    public abstract boolean test(World world, int x, int y, int layer);
}
