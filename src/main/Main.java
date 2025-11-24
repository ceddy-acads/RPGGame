package main;

import javax.swing.*;
import java.awt.*;
import entities.GameLandingPage;
import entities.StoryScreen; // Import the correct StoryScreen

public class Main {
    private static JFrame window;
    private static CardLayout cardLayout;
    private static JPanel mainPanel;
    private static GameLoop gameLoop;
    private static GameLandingPage landingPage; // Declare as static member
    private static StoryScreen storyScreen; // Declare as static member

    public static void main(String[] args) {
        window = new JFrame("Blade Quest");
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        landingPage = new GameLandingPage(Main::showStoryScreen);
        storyScreen = new StoryScreen(Main::startGame); // Use the refactored StoryScreen
        gameLoop = new GameLoop();

        mainPanel.add(landingPage, "LANDING");
        mainPanel.add(storyScreen, "STORY");
        mainPanel.add(gameLoop, "GAME");

        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.add(mainPanel);
        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);

        cardLayout.show(mainPanel, "LANDING");
    }

    public static void showStoryScreen() {
        cardLayout.show(mainPanel, "STORY");
        storyScreen.requestFocusInWindow(); // Request focus for the StoryScreen
    }

    public static void startGame() {
        cardLayout.show(mainPanel, "GAME");
        // It's important to request focus for the game loop to receive key events.
        gameLoop.requestFocusInWindow();
        gameLoop.start(); // Start the game loop when the game screen is shown
    }
}
