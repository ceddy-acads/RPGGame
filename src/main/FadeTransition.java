package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FadeTransition {
    private final JFrame frame;
    private final JPanel overlay;
    private float alpha = 0.0f;
    private final Timer timer;
    private final Runnable callback;

    public FadeTransition(JFrame frame, Runnable callback) {
        this.frame = frame;
        this.callback = callback;

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
        frame.getLayeredPane().add(overlay, JLayeredPane.PALETTE_LAYER);

        timer = new Timer(20, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                alpha += 0.05f;
                if (alpha >= 1.0f) {
                    alpha = 1.0f;
                    timer.stop();
                    frame.getLayeredPane().remove(overlay);
                    frame.repaint();
                    if (callback != null) {
                        callback.run();
                    }
                }
                overlay.repaint();
            }
        });
        timer.start();
    }
}
