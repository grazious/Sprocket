package com.avianmc.sprocket.rule.rules;

import com.avianmc.sprocket.rule.Rule;
import com.avianmc.sprocket.tile.Tiles;
import com.avianmc.sprocket.world.Chunk;
import com.avianmc.sprocket.world.World;

public class WithSupportRule extends Rule {
    public WithSupportRule() {
        super("with_support");
    }

    @Override
    public boolean test(World world, int x, int y, int layer) {
        if (world == null || !world.inBounds(x, y, layer)) return false;
        if (world.get(x, y, layer) != Tiles.AIR) return false;
        if (layer == 0) return true;
        int below = layer - 1;
        if (below < 0 || below >= Chunk.LAYERS) return false;
        return world.get(x, y, below) != Tiles.AIR;
    }
}
