package com.avianmc.sprocket;

import com.avianmc.sprocket.window.GameWindow;
import com.avianmc.sprocket.world.WorldGenerator;
import com.avianmc.sprocket.world.gen.features.ExampleFeature;

import javax.swing.*;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {

            WorldGenerator.registerFeature(new ExampleFeature(0.55, 0.05));

            try {
                new GameWindow();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}