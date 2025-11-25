package com.avianmc.sprocket.render;

import com.avianmc.sprocket.tile.Tile;
import com.avianmc.sprocket.tile.Tiles;
import com.avianmc.sprocket.window.Camera;
import com.avianmc.sprocket.world.Chunk;
import com.avianmc.sprocket.world.World;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Renders the entire chunk into an offscreen image and draws it.
 */
public class ChunkRenderer {
    private final RenderConfig cfg;
    private final World world;

    private BufferedImage chunkImage;
    private boolean dirty = true;
    private int originX, originY;

    public ChunkRenderer(RenderConfig cfg, World world) {
        this.cfg = cfg;
        this.world = world;
    }

    public void markDirty() {
        dirty = true;
    }

    public void renderIfNeeded() {
        if (!dirty) return;

        double dx = cfg.tileW / 2.0;
        double dy = cfg.tileH / 2.0;

        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        boolean hasAny = false;

        for (int s = 0; s <= (Chunk.SIZE - 1) * 2; s++) {
            for (int x = 0; x < Chunk.SIZE; x++) {
                int y = s - x;
                if (y < 0 || y >= Chunk.SIZE) continue;
                for (int z = 0; z < Chunk.LAYERS; z++) {
                    Tile t = world.get(x, y, z);
                    if (t == Tiles.AIR) continue;
                    Image img = t.getTexture();
                    if (img == null) continue;
                    hasAny = true;

                    double wx = (x - y) * dx;
                    double wy = (x + y) * dy - z * cfg.layerH;

                    double left = wx - img.getWidth(null) / 2.0;
                    double top = wy;
                    double right = left + img.getWidth(null);
                    double bottom = top + img.getHeight(null);

                    if (left < minX) minX = left;
                    if (top < minY) minY = top;
                    if (right > maxX) maxX = right;
                    if (bottom > maxY) maxY = bottom;
                }
            }
        }

        if (!hasAny) {
            minX = minY = 0;
            maxX = maxY = 1;
        }

        int imgW = (int) Math.ceil(maxX - minX);
        int imgH = (int) Math.ceil(maxY - minY);
        originX = (int) Math.floor(-minX);
        originY = (int) Math.floor(-minY);

        chunkImage = new BufferedImage(Math.max(1, imgW), Math.max(1, imgH), BufferedImage.TYPE_INT_ARGB);
        Graphics2D cg = chunkImage.createGraphics();
        cg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        cg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        for (int s = 0; s <= (Chunk.SIZE - 1) * 2; s++) {
            for (int x = 0; x < Chunk.SIZE; x++) {
                int y = s - x;
                if (y < 0 || y >= Chunk.SIZE) continue;
                for (int z = 0; z < Chunk.LAYERS; z++) {
                    Tile t = world.get(x, y, z);
                    if (t == Tiles.AIR) continue;
                    Image img = t.getTexture();
                    if (img == null) continue;

                    double wx = (x - y) * dx;
                    double wy = (x + y) * dy - z * cfg.layerH;

                    int drawX = originX + (int) Math.round(wx) - img.getWidth(null) / 2;
                    int drawY = originY + (int) Math.round(wy);
                    cg.drawImage(img, drawX, drawY, null);
                }
            }
        }
        cg.dispose();
        dirty = false;
    }

    public void draw(Graphics2D g2, Camera camera, int panelW, int panelH) {
        if (chunkImage == null) return;
        double zoom = camera.getZoom();
        double cx = panelW / 2.0;
        double cy = panelH / 2.0;
        int drawX = (int) Math.round(((-originX - camera.getX()) * zoom) + cx);
        int drawY = (int) Math.round(((-originY - camera.getY()) * zoom) + cy);
        int drawW = (int) Math.round(chunkImage.getWidth() * zoom);
        int drawH = (int) Math.round(chunkImage.getHeight() * zoom);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2.drawImage(chunkImage, drawX, drawY, drawW, drawH, null);
    }
}
