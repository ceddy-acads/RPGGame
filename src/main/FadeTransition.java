package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FadeTransition {
    public enum FadeType { FADE_IN, FADE_OUT }

    private static JPanel overlay;
    private static float alpha;
    private static Timer timer;

    public FadeTransition(JFrame frame, FadeType type, Runnable callback) {
        if (overlay == null) {
            overlay = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    g.setColor(new Color(0, 0, 0, (int) (alpha * 255)));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            };
            overlay.setBounds(0, 0, frame.getWidth(), frame.getHeight());
            overlay.setOpaque(false);
        }

        if (timer != null && timer.isRunning()) {
            timer.stop();
        }

        if (type == FadeType.FADE_OUT) {
            alpha = 0.0f;
        } else {
            alpha = 1.0f;
        }

        if (overlay.getParent() == null) {
            frame.getLayeredPane().add(overlay, JLayeredPane.PALETTE_LAYER);
        }
        frame.getLayeredPane().setComponentZOrder(overlay, 0);
        overlay.repaint();

        timer = new Timer(20, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (type == FadeType.FADE_OUT) {
                    alpha += 0.05f;
                    if (alpha >= 1.0f) {
                        alpha = 1.0f;
                        timer.stop();
                        if (callback != null) {
                            callback.run();
                        }
                    }
                } else {
                    alpha -= 0.05f;
                    if (alpha <= 0.0f) {
                        alpha = 0.0f;
                        timer.stop();
                        if (overlay.getParent() != null) {
                           frame.getLayeredPane().remove(overlay);
                           frame.repaint();
                        }
                        if (callback != null) {
                            callback.run();
                        }
                    }
                }
                overlay.repaint();
            }
        });
        timer.start();
    }
}
