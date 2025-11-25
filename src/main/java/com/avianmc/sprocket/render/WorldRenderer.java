package com.avianmc.sprocket.render;

import com.avianmc.sprocket.tile.Tile;
import com.avianmc.sprocket.tile.Tiles;
import com.avianmc.sprocket.window.Camera;
import com.avianmc.sprocket.world.Chunk;
import com.avianmc.sprocket.world.World;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Renders a multi-chunk world using per-chunk offscreen buffers. Only chunks
 * marked dirty are re-rendered to their own images. Each frame, chunk images
 * are composed in isometric chunk-diagonal order and scaled by camera zoom.
 */
public class WorldRenderer {
    private final RenderConfig cfg;
    private final World world;

    private static class ChunkBuffer {
        BufferedImage image;
        boolean dirty = true;
        // World-space anchoring encoded via these origins, matching previous formula:
        // buffer screen position = (-originX - camX, -originY - camY) scaled by zoom, centered
        int originX;
        int originY;
    }

    private final ChunkBuffer[][] buffers; // [cx][cy]

    public WorldRenderer(RenderConfig cfg, World world) {
        this.cfg = cfg;
        this.world = world;
        this.buffers = new ChunkBuffer[world.getChunksX()][world.getChunksY()];
        for (int cx = 0; cx < world.getChunksX(); cx++) {
            for (int cy = 0; cy < world.getChunksY(); cy++) {
                buffers[cx][cy] = new ChunkBuffer();
            }
        }
    }

    public void markDirty() {
        for (int cx = 0; cx < buffers.length; cx++) {
            for (int cy = 0; cy < buffers[0].length; cy++) {
                buffers[cx][cy].dirty = true;
            }
        }
    }

    public void markTileDirty(int tx, int ty) {
        if (tx < 0 || ty < 0) return;
        int cx = tx / Chunk.SIZE;
        int cy = ty / Chunk.SIZE;
        if (cx < 0 || cy < 0 || cx >= world.getChunksX() || cy >= world.getChunksY()) return;
        buffers[cx][cy].dirty = true;
    }

    public void renderIfNeeded() {
        for (int cx = 0; cx < world.getChunksX(); cx++) {
            for (int cy = 0; cy < world.getChunksY(); cy++) {
                if (buffers[cx][cy].dirty) renderChunk(cx, cy);
            }
        }
    }

    private void renderChunk(int cx, int cy) {
        ChunkBuffer buf = buffers[cx][cy];
        double dx = cfg.tileW / 2.0;
        double dy = cfg.tileH / 2.0;

        int startX = cx * Chunk.SIZE;
        int startY = cy * Chunk.SIZE;
        int endX = startX + Chunk.SIZE - 1;
        int endY = startY + Chunk.SIZE - 1;

        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        boolean hasAny = false;

        // First pass: bounds for this chunk only (iterate along local diagonals)
        for (int s = 0; s <= (Chunk.SIZE - 1) * 2; s++) {
            for (int lx = 0; lx < Chunk.SIZE; lx++) {
                int ly = s - lx;
                if (ly < 0 || ly >= Chunk.SIZE) continue;
                int x = startX + lx;
                int y = startY + ly;
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
        buf.originX = (int) Math.floor(-minX);
        buf.originY = (int) Math.floor(-minY);

        buf.image = new BufferedImage(Math.max(1, imgW), Math.max(1, imgH), BufferedImage.TYPE_INT_ARGB);
        Graphics2D cg = buf.image.createGraphics();
        cg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        cg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        // Second pass: draw tiles for this chunk in correct order
        for (int s = 0; s <= (Chunk.SIZE - 1) * 2; s++) {
            for (int lx = 0; lx < Chunk.SIZE; lx++) {
                int ly = s - lx;
                if (ly < 0 || ly >= Chunk.SIZE) continue;
                int x = startX + lx;
                int y = startY + ly;
                for (int z = 0; z < Chunk.LAYERS; z++) {
                    Tile t = world.get(x, y, z);
                    if (t == Tiles.AIR) continue;
                    Image img = t.getTexture();
                    if (img == null) continue;

                    double wx = (x - y) * dx;
                    double wy = (x + y) * dy - z * cfg.layerH;

                    int drawX = buf.originX + (int) Math.round(wx) - img.getWidth(null) / 2;
                    int drawY = buf.originY + (int) Math.round(wy);
                    cg.drawImage(img, drawX, drawY, null);
                }
            }
        }
        cg.dispose();
        buf.dirty = false;
    }

    public void draw(Graphics2D g2, Camera camera, int panelW, int panelH) {
        double zoom = camera.getZoom();
        double cx = panelW / 2.0;
        double cy = panelH / 2.0;
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        int chunksX = world.getChunksX();
        int chunksY = world.getChunksY();
        int maxDiag = (chunksX - 1) + (chunksY - 1);
        for (int s = 0; s <= maxDiag; s++) {
            int minCX = Math.max(0, s - (chunksY - 1));
            int maxCX = Math.min(chunksX - 1, s);
            for (int cxIdx = minCX; cxIdx <= maxCX; cxIdx++) {
                int cyIdx = s - cxIdx;
                ChunkBuffer buf = buffers[cxIdx][cyIdx];
                if (buf.image == null) continue;
                int drawX = (int) Math.round(((-buf.originX - camera.getX()) * zoom) + cx);
                int drawY = (int) Math.round(((-buf.originY - camera.getY()) * zoom) + cy);
                int drawW = (int) Math.round(buf.image.getWidth() * zoom);
                int drawH = (int) Math.round(buf.image.getHeight() * zoom);
                g2.drawImage(buf.image, drawX, drawY, drawW, drawH, null);
            }
        }
    }
}
