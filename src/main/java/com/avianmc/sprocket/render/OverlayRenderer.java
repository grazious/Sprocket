package com.avianmc.sprocket.render;

import com.avianmc.sprocket.window.Camera;

import java.awt.*;
import java.awt.geom.AffineTransform;

public class OverlayRenderer {
    private final RenderConfig cfg;
    private final IsoProjector projector;

    public OverlayRenderer(RenderConfig cfg, IsoProjector projector) {
        this.cfg = cfg;
        this.projector = projector;
    }

    public void drawHighlight(Graphics2D g2, int tx, int ty, int layer, int panelW, int panelH, Camera camera) {
        if (tx < 0 || ty < 0) return;
        Point p = projector.worldToScreenTopCenter(tx, ty, layer, panelW, panelH, camera);
        int drawW = (int) Math.round(cfg.tileW * camera.getZoom());
        int drawH = (int) Math.round(cfg.tileH * camera.getZoom());
        int baseY = p.y + drawH / 2;
        Polygon poly = new Polygon();
        poly.addPoint(p.x, baseY);
        poly.addPoint(p.x + drawW / 2, baseY + drawH / 2);
        poly.addPoint(p.x, baseY + drawH);
        poly.addPoint(p.x - drawW / 2, baseY + drawH / 2);
        g2.setColor(new Color(255, 255, 255, 160));
        g2.setStroke(new BasicStroke(2f));
        g2.drawPolygon(poly);
    }

    public void drawHUD(Graphics2D g2, int fps, double zoom) {
        g2.setTransform(new AffineTransform());
        g2.setColor(Color.WHITE);
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        g2.drawString("FPS: " + fps, 10, 24);
        g2.drawString("Zoom: " + String.format("%.2f", zoom), 10, 44);
    }
}
