package main;

import javax.swing.*;
import java.awt.*;
import entities.GameLandingPage;
import entities.StoryScreen;
import entities.GameOverScreen;
import java.awt.image.BufferedImage;

public class Main {
    private static JFrame window;
    private static CardLayout cardLayout;
    private static JPanel mainPanel;
    private static GameLoop gameLoop;
    private static GameLandingPage landingPage;
    private static StoryScreen storyScreen;
    private static GameOverScreen gameOverScreen;

    public static void main(String[] args) {
        window = new JFrame("Blade Quest");
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        landingPage = new GameLandingPage(Main::showStoryScreen);
        storyScreen = new StoryScreen(Main::startGame);
        gameLoop = new GameLoop(Main::showGameOverScreenWithScreenshot);
        gameOverScreen = new GameOverScreen(Main::resetGame);

        mainPanel.add(landingPage, "LANDING");
        mainPanel.add(storyScreen, "STORY");
        mainPanel.add(gameLoop, "GAME");
        mainPanel.add(gameOverScreen, "GAME_OVER");

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

    public static void showGameOverScreenWithScreenshot(BufferedImage screenshot) {
        gameOverScreen.setBackgroundImage(screenshot);
        new FadeTransition(window, FadeTransition.FadeType.FADE_OUT, () -> {
            cardLayout.show(mainPanel, "GAME_OVER");
            gameOverScreen.requestFocusInWindow();
            new FadeTransition(window, FadeTransition.FadeType.FADE_IN, null);
        });
    }

    public static void resetGame() {
        new FadeTransition(window, FadeTransition.FadeType.FADE_OUT, () -> {
            gameLoop.reset();
            cardLayout.show(mainPanel, "GAME");
            gameLoop.requestFocusInWindow();
            gameLoop.start();
            new FadeTransition(window, FadeTransition.FadeType.FADE_IN, null);
        });
    }
}
