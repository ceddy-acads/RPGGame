package main;

import javax.swing.JFrame;

public class Game {
    public static void main(String[] args) {
        JFrame window = new JFrame("RPG Game - Arrow + QWERT Controller");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.setContentPane(new GameLoop());
        window.pack();
        window.setVisible(true);
        window.setLocationRelativeTo(null);
    }
}
