package com.avianmc.sprocket.tile.tiles;

import com.avianmc.sprocket.rule.Rule;
import com.avianmc.sprocket.rule.Rules;
import com.avianmc.sprocket.tile.Tile;

public class RockTile extends Tile {
    public RockTile() {
        super("rock", Rules.WITH_SUPPORT);
    }
}
