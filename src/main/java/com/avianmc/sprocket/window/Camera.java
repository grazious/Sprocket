package com.avianmc.sprocket.window;

import java.awt.event.KeyEvent;

public class Camera {
    private double x, y;
    private double targetX, targetY;
    private double speed = 10;
    private double smoothing = 0.15;
    private double zoom = 1.0;
    private double zoomSpeed = 0.1;

    private boolean up, down, left, right, shift;

    public Camera() {}

    public void update() {

        double moveX = 0, moveY = 0;
        double move_speed = speed;

        if (shift) {
            move_speed = speed * 3;
        }

        if (up) moveY -= move_speed;
        if (down) moveY += move_speed;
        if (left) moveX -= move_speed;
        if (right) moveX += move_speed;

        double len = Math.sqrt(moveX * moveX + moveY * moveY);
        if (len > 0) {
            moveX /= len;
            moveY /= len;
        }

        targetX += moveX * move_speed;
        targetY += moveY * move_speed;

        // Smooth follow
        x += (targetX - x) * smoothing;
        y += (targetY - y) * smoothing;
    }

    public void onKeyPressed(int keyCode) {
        switch (keyCode) {
            case java.awt.event.KeyEvent.VK_W -> up = true;
            case java.awt.event.KeyEvent.VK_S -> down = true;
            case java.awt.event.KeyEvent.VK_A -> left = true;
            case java.awt.event.KeyEvent.VK_D -> right = true;

            case KeyEvent.VK_SHIFT -> shift = true;

        }
    }

    public void onKeyReleased(int keyCode) {
        switch (keyCode) {
            case java.awt.event.KeyEvent.VK_W -> up = false;
            case java.awt.event.KeyEvent.VK_S -> down = false;
            case java.awt.event.KeyEvent.VK_A -> left = false;
            case java.awt.event.KeyEvent.VK_D -> right = false;

            case KeyEvent.VK_SHIFT -> shift = false;
        }
    }

    public void onScroll(int notches) {
        zoom += -notches * zoomSpeed;
        if (zoom < 0.05) zoom = 0.05;
        if (zoom > 6.0) zoom = 6.0;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getZoom() { return zoom; }
}