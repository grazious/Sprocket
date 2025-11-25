package com.avianmc.sprocket.tile;

import com.avianmc.sprocket.render.TextureManager;
import com.avianmc.sprocket.rule.Rule;

import java.awt.image.BufferedImage;

public abstract class Tile {

    protected final BufferedImage texture;
    protected final Rule placementRule;

    public Tile(String textureName, Rule placementRule) {
        this.texture = TextureManager.getSprite(textureName);
        this.placementRule = placementRule;
    }

    public BufferedImage getTexture() {
        return texture;
    }

    public Rule getPlacementRule() {
        return placementRule;
    }
}