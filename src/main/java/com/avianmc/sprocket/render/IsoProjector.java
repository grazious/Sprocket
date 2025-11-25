package com.avianmc.sprocket.render;

import com.avianmc.sprocket.window.Camera;
import com.avianmc.sprocket.world.World;

import java.awt.*;

public class IsoProjector {
    private final RenderConfig cfg;

    public IsoProjector(RenderConfig cfg) {
        this.cfg = cfg;
    }

    public Point screenToTile(int sx, int sy, int layer, int panelW, int panelH, Camera camera, World world) {
        double zoom = camera.getZoom();
        double wx = (sx - panelW / 2.0) / zoom + camera.getX();
        double wy = (sy - panelH / 2.0) / zoom + camera.getY();

        wy -= (cfg.tileH / 2.0);

        double dx = cfg.tileW / 2.0;
        double dy = cfg.tileH / 2.0;

        double wyAdj = wy + layer * cfg.layerH;

        double fx = (wyAdj / dy + wx / dx) / 2.0;
        double fy = (wyAdj / dy - wx / dx) / 2.0;

        int tx = (int) Math.floor(fx);
        int ty = (int) Math.floor(fy);

        if (world != null && !world.inBounds(tx, ty, layer)) return null;
        return new Point(tx, ty);
    }

    public Point worldToScreenTopCenter(int tx, int ty, int layer, int panelW, int panelH, Camera camera) {
        double dx = cfg.tileW / 2.0;
        double dy = cfg.tileH / 2.0;
        double wx = (tx - ty) * dx;
        double wy = (tx + ty) * dy - layer * cfg.layerH;

        double zoom = camera.getZoom();
        int sx = (int) Math.round(((wx - camera.getX()) * zoom) + panelW / 2.0);
        int sy = (int) Math.round(((wy - camera.getY()) * zoom) + panelH / 2.0);
        return new Point(sx, sy);
    }
}
