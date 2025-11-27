package com.avianmc.sprocket;

import com.avianmc.sprocket.window.GameWindow;
import com.avianmc.sprocket.world.WorldGenerator;
import com.avianmc.sprocket.world.gen.features.RockFeature;
import com.avianmc.sprocket.world.gen.features.TerrainFeature;
import com.avianmc.sprocket.world.gen.features.TreeFeature;

import javax.swing.*;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {

            WorldGenerator.registerFeature(new TerrainFeature(0.05));
            WorldGenerator.registerFeature(new TreeFeature(0.025));
            WorldGenerator.registerFeature(new RockFeature(0.0025));

            try {
                new GameWindow();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}