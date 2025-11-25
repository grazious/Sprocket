package com.avianmc.sprocket.window;

import com.avianmc.sprocket.render.IsoProjector;
import com.avianmc.sprocket.render.OverlayRenderer;
import com.avianmc.sprocket.render.RenderConfig;
import com.avianmc.sprocket.render.WorldRenderer;
import com.avianmc.sprocket.tile.Tile;
import com.avianmc.sprocket.tile.Tiles;
import com.avianmc.sprocket.world.Chunk;
import com.avianmc.sprocket.world.World;
import com.avianmc.sprocket.world.WorldGenerator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GamePanel extends JPanel implements Runnable {

    // Rendering helpers
    private final RenderConfig renderCfg = new RenderConfig(32, 16, 8);
    private final IsoProjector projector = new IsoProjector(renderCfg);

    private Thread gameThread;
    private boolean running = false;

    private final Camera camera = new Camera();
    private final World world = WorldGenerator.generateFlatWorld();
    private final WorldRenderer worldRenderer = new WorldRenderer(renderCfg, world);
    private final OverlayRenderer overlayRenderer = new OverlayRenderer(renderCfg, projector);

    private int fps = 0;
    private int frames = 0;
    private long lastTime;

    private int mouseX, mouseY;
    private boolean leftDown, rightDown;
    private boolean prevRightDown;
    private boolean prevLeftDown;

    private static final int PLACE_LAYER_COOLDOWN_TICKS = 16;
    private static final int BREAK_LAYER_COOLDOWN_TICKS = 16;
    private int placeCooldown = 0;
    private int lastPlaceX = -1, lastPlaceY = -1, lastPlaceLayer = -1;

    // Anchor used while holding right mouse to continue placing at same tile
    private int placeAnchorX = -1, placeAnchorY = -1;

    private int breakCooldown = 0;
    private int lastBreakX = -1, lastBreakY = -1, lastBreakLayer = -1;

    public GamePanel() {
        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.BLACK);
        setFocusable(true);

        setupInput();
        startGameThread();
    }

    private void setupInput() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                camera.onKeyPressed(e.getKeyCode());
            }

            @Override
            public void keyReleased(KeyEvent e) {
                camera.onKeyReleased(e.getKeyCode());
            }
        });

        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                if (SwingUtilities.isLeftMouseButton(e)) {
                    leftDown = true;
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    rightDown = true;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    leftDown = false;
                    // reset break anchor/cooldown on release
                    breakCooldown = 0;
                    lastBreakX = lastBreakY = lastBreakLayer = -1;
                }
                if (SwingUtilities.isRightMouseButton(e)) {
                    rightDown = false;
                    // release placement anchor and reset cooldown so subsequent press is immediate
                    placeAnchorX = placeAnchorY = -1;
                    placeCooldown = 0;
                    lastPlaceX = lastPlaceY = lastPlaceLayer = -1;
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
            }
        };
        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);

        addMouseWheelListener(e -> camera.onScroll(e.getWheelRotation()));
    }

    private void startGameThread() {
        running = true;
        gameThread = new Thread(this, "SprocketGameThread");
        gameThread.start();
    }

    @Override
    public void run() {
        double drawInterval = 1_000_000_000.0 / 60.0;
        double nextDraw = System.nanoTime() + drawInterval;

        lastTime = System.nanoTime();

        while (running) {
            camera.update();

            // Tick down cooldowns
            if (placeCooldown > 0) placeCooldown--;
            if (breakCooldown > 0) breakCooldown--;

            // Breaking: edge-triggered + hold behavior with anchoring (existing logic)
            boolean newLeftPress = leftDown && !prevLeftDown;
            if (newLeftPress) {
                breakAtCursor(true);
            } else if (leftDown) {
                breakAtCursor(false);
            }

            // Placement: hold-to-place behavior
            boolean newRightPress = rightDown && !prevRightDown;

            if (newRightPress) {
                // On initial press, attempt to anchor on current highlight and place immediately
                Hover hi = computeHighlightTarget(mouseX, mouseY);
                if (hi != null) {
                    placeAnchorX = hi.x;
                    placeAnchorY = hi.y;
                    // immediate place attempt
                    placeAtAnchor();
                } else {
                    // no valid highlight -> clear any anchor
                    placeAnchorX = placeAnchorY = -1;
                }
            }

            if (rightDown) {
                // continue placing while held
                placeAtAnchor();
            } else {
                // when released we already cleared cooldowns in mouseReleased handler
            }

            prevRightDown = rightDown;
            prevLeftDown = leftDown;

            repaint();
            frames++;

            long now = System.nanoTime();
            if (now - lastTime >= 1_000_000_000L) {
                fps = frames;
                frames = 0;
                lastTime = now;
            }

            try {
                double remaining = nextDraw - System.nanoTime();
                if (remaining < 0) remaining = 0;
                Thread.sleep((long) (remaining / 1_000_000));
                nextDraw += drawInterval;
            } catch (InterruptedException ignored) {}
        }
    }

    /**
     * Attempt to place a tile at the anchored x/y (if any). Uses placeCooldown.
     * Anchor is set on initial right-press and remains while the button is held.
     * The function verifies the cursor still points to the anchored tile so accidental drift won't place.
     */
    private void placeAtAnchor() {
        if (placeCooldown > 0) return;

        Hover hi = computeHighlightTarget(mouseX, mouseY);
        if (hi == null) return;

        // If no anchor yet (first tick of holding RMB), anchor immediately
        if (placeAnchorX < 0 || placeAnchorY < 0) {
            placeAnchorX = hi.x;
            placeAnchorY = hi.y;
        }

        // Detect movement while holding RMB:
        // If the hovered tile changed, update the anchor and allow placement
        boolean movedToNewTile =
                hi.x != placeAnchorX || hi.y != placeAnchorY;

        if (movedToNewTile) {
            placeAnchorX = hi.x;
            placeAnchorY = hi.y;
        }

        int baseX = placeAnchorX;
        int baseY = placeAnchorY;

        // Determine layer using your existing placement rule logic
        Integer targetLayer = findPlacementLayerAt(baseX, baseY, Tiles.GRASS);
        if (targetLayer == null) return;

        Tile current = world.get(baseX, baseY, targetLayer);
        if (current != Tiles.GRASS) {
            world.set(baseX, baseY, targetLayer, Tiles.GRASS);
            worldRenderer.markTileDirty(baseX, baseY);
        }

        lastPlaceX = baseX;
        lastPlaceY = baseY;
        lastPlaceLayer = targetLayer;
        placeCooldown = PLACE_LAYER_COOLDOWN_TICKS;
    }


    private void breakAtCursor(boolean newPress) {
        // Anchor breaking to the currently highlighted tile's x/y
        Hover hi = computeHighlightTarget(mouseX, mouseY);
        if (hi == null) return;

        int baseX = hi.x;
        int baseY = hi.y;

        Integer targetLayer = findBreakLayerAt(baseX, baseY);
        if (targetLayer == null) return;

        if (!newPress) {
            if (breakCooldown > 0) return;
            if (baseX != lastBreakX || baseY != lastBreakY) return;
        }

        Tile current = world.get(baseX, baseY, targetLayer);
        if (current != Tiles.AIR) {
            world.set(baseX, baseY, targetLayer, Tiles.AIR);
            worldRenderer.markTileDirty(baseX, baseY);
        }

        lastBreakX = baseX;
        lastBreakY = baseY;
        lastBreakLayer = targetLayer;
        breakCooldown = BREAK_LAYER_COOLDOWN_TICKS;
    }

    private Point screenToTile(int sx, int sy, int layer) {
        return projector.screenToTile(sx, sy, layer, getWidth(), getHeight(), camera, world);
    }

    private Hover computeHighlightTarget(int sx, int sy) {
        // From top layer down, stop at first non-air; if none, use layer 0 if valid
        for (int z = Chunk.LAYERS - 1; z >= 0; z--) {
            Point p = screenToTile(sx, sy, z);
            if (p == null) continue;
            if (world.get(p.x, p.y, z) != Tiles.AIR) {
                return new Hover(p.x, p.y, z);
            }
            if (z == 0) {
                return new Hover(p.x, p.y, 0);
            }
        }
        return null;
    }

    private Hover findBreakTarget(int sx, int sy) {
        for (int z = Chunk.LAYERS - 1; z >= 0; z--) {
            Point p = screenToTile(sx, sy, z);
            if (p == null) continue;
            if (world.get(p.x, p.y, z) != Tiles.AIR) {
                return new Hover(p.x, p.y, z);
            }
            if (z == 0) {
                // nothing to break on layer 0 if it's air
                return null;
            }
        }
        return null;
    }

    private Integer findBreakLayerAt(int x, int y) {
        for (int z = Chunk.LAYERS - 1; z >= 0; z--) {
            if (!world.inBounds(x, y, z)) continue;
            if (world.get(x, y, z) != Tiles.AIR) return z;
        }
        return null;
    }

    private Integer findPlacementLayerAt(int x, int y, Tile toPlace) {
        if (toPlace == null || toPlace.getPlacementRule() == null) return null;
        for (int z = Chunk.LAYERS - 1; z >= 0; z--) {
            if (!world.inBounds(x, y, z)) continue;
            if (toPlace.getPlacementRule().test(world, x, y, z)) {
                return z;
            }
        }
        return null;
    }

    private static class Hover {
        final int x, y, layer;
        Hover(int x, int y, int layer) { this.x = x; this.y = y; this.layer = layer; }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Update world buffer if needed and draw it
        worldRenderer.renderIfNeeded();
        worldRenderer.draw(g2, camera, getWidth(), getHeight());

        Hover hi = computeHighlightTarget(mouseX, mouseY);
        if (hi != null) {
            overlayRenderer.drawHighlight(g2, hi.x, hi.y, hi.layer, getWidth(), getHeight(), camera);
        }

        // HUD: FPS and layer info
        overlayRenderer.drawHUD(g2, fps, camera.getZoom());

        g2.dispose();
    }
}
