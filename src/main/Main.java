package main;

import javax.swing.*;
import java.awt.*;
import entities.GameLandingPage;
import entities.StoryScreen; 

public class Main {
    private static JFrame window;
    private static CardLayout cardLayout;
    private static JPanel mainPanel;
    private static GameLoop gameLoop;
    private static GameLandingPage landingPage; 
    private static StoryScreen storyScreen; 

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
        new FadeTransition(window, FadeTransition.FadeType.FADE_OUT, () -> {
            cardLayout.show(mainPanel, "STORY");
            storyScreen.requestFocusInWindow();
            new FadeTransition(window, FadeTransition.FadeType.FADE_IN, null);
        });
    }

    public static void startGame() {
        new FadeTransition(window, FadeTransition.FadeType.FADE_OUT, () -> {
            cardLayout.show(mainPanel, "GAME");
            gameLoop.requestFocusInWindow();
            gameLoop.start();
            new FadeTransition(window, FadeTransition.FadeType.FADE_IN, null);
        });
    }
}
