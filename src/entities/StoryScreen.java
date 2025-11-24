package entities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class StoryScreen extends JPanel {

    private FadePanel imagePanel;
    private JTextArea storyText;
    private JButton continueButton;

    private float textAlpha = 0f;
    private Timer textTimer;

    private int currentSlide = 0;
    private ArrayList<String> paragraphs;
    private ArrayList<Image> images;
    private Runnable onStoryEnd;

    public StoryScreen(Runnable onStoryEnd) {
        this.onStoryEnd = onStoryEnd;
        setPreferredSize(new Dimension(800, 600));
        setLayout(new BorderLayout());
        setBackground(Color.BLACK);

        imagePanel = new FadePanel();
        add(imagePanel, BorderLayout.CENTER);

        paragraphs = new ArrayList<>();
        paragraphs.add("Centuries ago, the Kingdom of Valoria was a land of peace, guarded by the sacred blade Aurelion.");
        paragraphs.add("But one night, the sky turned crimson — the Blade shattered into five fragments, and darkness spread like wildfire.");
        paragraphs.add("The once-protected lands are now overrun by monsters born from the shadow.\n\nThe king has vanished — cities have fallen, and only a few villages survive in hiding.");
        paragraphs.add("You, Kael, must find the fragments… restore the Blade… and bring light back to Valoria.");

        images = new ArrayList<>();
        images.add(loadImage("/assets/ui/story1.png"));
        images.add(loadImage("/assets/ui/story2.png"));
        images.add(loadImage("/assets/ui/story3.png"));
        images.add(loadImage("/assets/ui/story4.png"));

        imagePanel.setText(paragraphs.get(0));
        imagePanel.setCurrentImage(images.get(0));

        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_ENTER) {
                    nextSlide();
                }
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    onStoryEnd.run(); // skip to game
                }
            }
        });
    }

    private void nextSlide() {
        if (currentSlide >= paragraphs.size() - 1) {
            onStoryEnd.run();
            return;
        }

        currentSlide++;
        imagePanel.fadeTo(images.get(currentSlide));
        new javax.swing.Timer(150, e -> {
            imagePanel.setText(paragraphs.get(currentSlide));
            ((Timer)e.getSource()).stop();
        }).start();
    }

    private Image loadImage(String path) {
        try {
            return new ImageIcon(getClass().getResource(path)).getImage();
        } catch (Exception e) {
            System.err.println("Failed to load image: " + path);
            return null;
        }
    }

    class FadePanel extends JPanel {
        private Image currentImage;
        private Image nextImage;
        private float alpha = 0.0f;
        private Timer fadeTimer;

        private String text = "";
        private float textAlpha = 0f;

        private boolean showHint = true;
        private Timer hintTimer;

        public FadePanel() {
            hintTimer = new Timer(500, e -> {
                showHint = !showHint;
                repaint();
            });
            hintTimer.start();
        }

        public void setCurrentImage(Image img) {
            currentImage = img;
            repaint();
        }

        public void setText(String newText) {
            this.text = newText;
            this.textAlpha = 0f;
            if (textTimer != null) textTimer.stop();

            textTimer = new Timer(30, e -> {
                textAlpha += 0.02f;
                if (textAlpha >= 1f) {
                    textAlpha = 1f;
                    textTimer.stop();
                }
                repaint();
            });

            textTimer.start();
        }

        public void fadeTo(Image img) {
            nextImage = img;
            alpha = 0.0f;

            if (fadeTimer != null && fadeTimer.isRunning()) {
                fadeTimer.stop();
            }

            fadeTimer = new Timer(30, e -> {
                alpha += 0.03f;
                if (alpha >= 1.0f) {
                    alpha = 1.0f;
                    currentImage = nextImage;
                    nextImage = null;
                    fadeTimer.stop();
                }
                repaint();
            });
            fadeTimer.start();
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2d = (Graphics2D) g.create();

            if (currentImage != null) {
                g2d.drawImage(currentImage, 0, 0, getWidth(), getHeight(), null);
            }

            if (nextImage != null && alpha > 0.0f) {
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                g2d.drawImage(nextImage, 0, 0, getWidth(), getHeight(), null);
            }

            if (text != null && !text.trim().isEmpty()) {
                g2d.setFont(new Font("Georgia", Font.ITALIC, 20));
                FontMetrics fm = g2d.getFontMetrics();

                int maxWidth = getWidth() - 100; // 50px margin on each side
                java.util.List<String> lines = getWrappedLines(text, fm, maxWidth);
                int lineHeight = fm.getHeight();
                int textBlockHeight = lineHeight * lines.size();

                // Calculate vertical center with responsive offset
                int verticalCenter = getHeight() / 2;
                int maxVisibleHeight = getHeight() - 200; // Reserve space for hints
                int textOffset = Math.max(50, (maxVisibleHeight - textBlockHeight) / 2);

                int y = verticalCenter - (textBlockHeight / 2) + textOffset;

                // Ensure text stays within visible bounds
                if (y < 80) y = 80; // Minimum top margin
                if (y + textBlockHeight > getHeight() - 60) {
                    y = getHeight() - 60 - textBlockHeight; // Adjust if too low
                }

                for (String line : lines) {
                    if (line.trim().isEmpty()) {
                        y += lineHeight; // Handle blank lines
                        continue;
                    }

                    int x = (getWidth() - fm.stringWidth(line)) / 2;

                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, textAlpha));

                    // Shadow text
                    g2d.setColor(new Color(0, 0, 0, 180));
                    g2d.drawString(line, x + 2, y + 2);

                    // Main text
                    g2d.setColor(new Color(255, 255, 255, 230));
                    g2d.drawString(line, x, y);

                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

                    y += lineHeight;
                }
            }

            if (showHint) {
                String hint1 = "Press SPACE to continue";
                String hint2 = "ESC to skip";

                g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
                FontMetrics fm = g2d.getFontMetrics();

                int x1 = (getWidth() - fm.stringWidth(hint1)) / 2;
                int x2 = (getWidth() - fm.stringWidth(hint2)) / 2;

                int y1 = getHeight() - 40;
                int y2 = getHeight() - 20;

                g2d.setColor(new Color(0, 0, 0, 180));
                g2d.drawString(hint1, x1 + 2, y1 + 2);
                g2d.drawString(hint2, x2 + 2, y2 + 2);

                g2d.setColor(Color.WHITE);
                g2d.drawString(hint1, x1, y1);
                g2d.drawString(hint2, x2, y2);
            }

            g2d.dispose();
        }

        private java.util.List<String> getWrappedLines(String text, FontMetrics fm, int maxWidth) {
            java.util.List<String> lines = new java.util.ArrayList<>();
            String[] rawLines = text.split("\n");

            for (String rawLine : rawLines) {
                if (fm.stringWidth(rawLine) <= maxWidth) {
                    lines.add(rawLine);
                    continue;
                }

                String[] words = rawLine.split(" ");
                StringBuilder currentLine = new StringBuilder();
                for (String word : words) {
                    if (fm.stringWidth(currentLine.toString() + word) < maxWidth) {
                        currentLine.append(word).append(" ");
                    } else {
                        lines.add(currentLine.toString().trim());
                        currentLine = new StringBuilder(word + " ");
                    }
                }
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString().trim());
                }
            }
            return lines;
        }
    }

}
