package com.avianmc.sprocket.rule.rules;

import com.avianmc.sprocket.rule.Rule;
import com.avianmc.sprocket.tile.Tiles;
import com.avianmc.sprocket.world.World;

public class NotAirRule extends Rule {
    public NotAirRule() {
        super("notAir");
    }

    @Override
    public boolean test(World world, int x, int y, int layer) {
        if (world == null || !world.inBounds(x, y, layer)) return false;
        return world.get(x, y, layer) != Tiles.AIR;
    }
}
