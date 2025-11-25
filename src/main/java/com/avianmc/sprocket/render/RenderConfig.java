package com.avianmc.sprocket.render;

/**
 * Central place for render-related constants and sizes.
 */
public class RenderConfig {
    public final int tileW;
    public final int tileH;
    public final int layerH;

    public RenderConfig(int tileW, int tileH, int layerH) {
        this.tileW = tileW;
        this.tileH = tileH;
        this.layerH = layerH;
    }
}
