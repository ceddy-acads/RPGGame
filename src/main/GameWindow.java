package main;

import javax.swing.JFrame;

public class GameWindow {
    public GameWindow() {
        JFrame window = new JFrame("Blade Quest");
        GameLoop gamePanel = new GameLoop();

        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.add(gamePanel);
        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }
}
