package com.avianmc.sprocket.window;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class GameWindow extends JFrame {
    public GameWindow() throws IOException {
        setTitle("Sprocket");
        setIconImage(
                ImageIO.read(GameWindow.class.getResourceAsStream("/icon.png"))
        );

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);

        GamePanel panel = new GamePanel();
        add(panel, BorderLayout.CENTER);
        pack();

        setLocationRelativeTo(null);
        setVisible(true);
    }
}
