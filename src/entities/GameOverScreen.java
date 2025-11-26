package entities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

public class GameOverScreen extends JPanel {
    private Runnable continueAction;
    private BufferedImage backgroundImage;

    public GameOverScreen(Runnable continueAction) {
        this.continueAction = continueAction;
        setLayout(new BorderLayout());
        setOpaque(false);

        JLabel gameOverLabel = new JLabel("GAME OVER", SwingConstants.CENTER);
        gameOverLabel.setForeground(Color.RED);
        gameOverLabel.setFont(new Font("Serif", Font.BOLD, 72));
        add(gameOverLabel, BorderLayout.CENTER);

        JButton continueButton = new JButton("Continue (Full HP)");
        continueButton.setFont(new Font("Serif", Font.PLAIN, 36));
        continueButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (continueAction != null) {
                    continueAction.run();
                }
            }
        });
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.add(continueButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public void setBackgroundImage(BufferedImage image) {
        if (image != null) {
            this.backgroundImage = applyGaussianBlur(image, 10);
            repaint();
        }
    }

    private BufferedImage applyGaussianBlur(BufferedImage source, int radius) {
        int size = radius * 2 + 1;
        float[] data = new float[size * size];
        float sigma = radius / 3.0f;
        float twoSigmaSquare = 2.0f * sigma * sigma;
        float sigmaRoot = (float) Math.sqrt(twoSigmaSquare * Math.PI);
        float total = 0;
        int index = 0;
        for (int y = -radius; y <= radius; y++) {
            for (int x = -radius; x <= radius; x++) {
                float distance = x * x + y * y;
                data[index] = (float) Math.exp(-distance / twoSigmaSquare) / sigmaRoot;
                total += data[index];
                index++;
            }
        }
        for (int i = 0; i < data.length; i++) {
            data[i] /= total;
        }
        Kernel kernel = new Kernel(size, size, data);
        ConvolveOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
        
        int imageType = source.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : source.getType();
        BufferedImage blurredImage = new BufferedImage(source.getWidth(), source.getHeight(), imageType);
        op.filter(source, blurredImage);
        return blurredImage;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
        g.setColor(new Color(0, 0, 0, 128));
        g.fillRect(0, 0, getWidth(), getHeight());
    }
}
