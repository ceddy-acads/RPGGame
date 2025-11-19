package main;




import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class StoryScreen extends JFrame {

    private FadePanel imagePanel;      // custom panel for fade transition
    private JTextArea storyText;
    private JButton continueButton;

    private int currentSlide = 0;
    private ArrayList<String> paragraphs;
    private ArrayList<Image> images;

    public StoryScreen() {
        setTitle("Blade Quest");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.BLACK);

        // 🖼️ Image panel with fade
        imagePanel = new FadePanel();
        imagePanel.setPreferredSize(new Dimension(800, 400));
        add(imagePanel, BorderLayout.CENTER);

        // 📜 Bottom panel (text + button)
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.setBackground(Color.BLACK);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        add(bottomPanel, BorderLayout.SOUTH);

        // 📝 Story text
        storyText = new JTextArea();
        storyText.setEditable(false);
        storyText.setWrapStyleWord(true);
        storyText.setLineWrap(true);
        storyText.setForeground(Color.WHITE);
        storyText.setBackground(Color.BLACK);
        storyText.setFont(new Font("Serif", Font.PLAIN, 18));
        storyText.setMargin(new Insets(10, 10, 10, 10));
        bottomPanel.add(storyText, BorderLayout.CENTER);

        // ⏭️ Continue button
        continueButton = new JButton("Continue");
        continueButton.setFont(new Font("Serif", Font.BOLD, 14));
        continueButton.setBackground(Color.DARK_GRAY);
        continueButton.setForeground(Color.WHITE);
        continueButton.setFocusPainted(false);
        continueButton.setPreferredSize(new Dimension(100, 30));
        continueButton.addActionListener(e -> nextSlide());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.BLACK);
        buttonPanel.add(continueButton);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);

        // 📜 Story paragraphs
        paragraphs = new ArrayList<>();
        paragraphs.add("Centuries ago, the Kingdom of Valoria was a land of peace, guarded by the sacred blade Aurelion.");
        paragraphs.add("But one night, the sky turned crimson — the Blade shattered into five fragments, and darkness spread like wildfire.");
        paragraphs.add("The once-protected lands are now overrun by monsters born from the shadow. The king has vanished, cities have fallen, and only a few villages survive in hiding.");
        paragraphs.add("You, Kael, must find the fragments… restore the Blade… and bring light back to Valoria.");

        // 🖼️ Load images
        images = new ArrayList<>();
        images.add(loadImage("/assets/ui/story1.png"));
        images.add(loadImage("/assets/ui/story2.png"));
        images.add(loadImage("/assets/ui/story3.png"));
        images.add(loadImage("/assets/ui/story4.png"));

        // Set first slide
        storyText.setText(paragraphs.get(0));
        imagePanel.setCurrentImage(images.get(0));

        setVisible(true);
    }

    private void nextSlide() {
        currentSlide++;
        if (currentSlide < paragraphs.size()) {
            storyText.setText(paragraphs.get(currentSlide));
            imagePanel.fadeTo(images.get(currentSlide));
        } else {
            dispose();
            SwingUtilities.invokeLater(GameWindow::new);  // 👑 Launch game
        }
    }

    // 📂 Image loader
    private Image loadImage(String path) {
        try {
            return new ImageIcon(getClass().getResource(path)).getImage();
        } catch (Exception e) {
            System.err.println("Failed to load image: " + path);
            return null;
        }
    }

    // 🪄 Custom panel for fade animation
    class FadePanel extends JPanel {
        private Image currentImage;
        private Image nextImage;
        private float alpha = 0.0f;
        private Timer fadeTimer;

        public void setCurrentImage(Image img) {
            currentImage = img;
            repaint();
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

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (currentImage != null) {
                g.drawImage(currentImage, 0, 0, getWidth(), getHeight(), null);
            }
            if (nextImage != null && alpha > 0.0f) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                g2d.drawImage(nextImage, 0, 0, getWidth(), getHeight(), null);
                g2d.dispose();
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(StoryScreen::new);
    }
}
