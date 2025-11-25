package com.avianmc.sprocket.render;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TextureManager {

    private static final Map<String, BufferedImage> spriteCache = new HashMap<>();

    public static BufferedImage getSprite(String name) {
        if (name == null) return null;
        if (spriteCache.containsKey(name)) {
            return spriteCache.get(name);
        }

        try {
            BufferedImage image = ImageIO.read(
                    TextureManager.class.getResourceAsStream("/tiles/" + name + ".png")
            );
            if (image != null) {
                spriteCache.put(name, image);
                return image;
            } else {
                System.err.println("Texture not found: " + name);
            }
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Failed to load texture: " + name);
            e.printStackTrace();
        }

        BufferedImage placeholder = new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB);
        spriteCache.put(name, placeholder);
        return placeholder;
    }
}