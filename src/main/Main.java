package main;

import javax.swing.JFrame;
import entities.GameLandingPage; // Revert to importing GameLandingPage

public class Main {
    public static void main(String[] args) {
        JFrame window = new JFrame("Blade Quest"); // Revert to original title
        GameLandingPage landingPage = new GameLandingPage(); // Revert to original instantiation

        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.add(landingPage); // Revert to adding landingPage
        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);
        landingPage.requestFocusInWindow(); // Ensure the landing page has focus for input
    }
}
