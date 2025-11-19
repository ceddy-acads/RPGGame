package entities;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;

public class Projectile {
    public int x, y;
    public int speed = 8;
    public int directionX, directionY;
    public boolean active = true;
    private BufferedImage sprite;

    public Projectile(int startX, int startY, int dirX, int dirY) {
        this.x = startX;
        this.y = startY;
        this.directionX = dirX;
        this.directionY = dirY;

        try {
            sprite = ImageIO.read(getClass().getResourceAsStream("/sprites/fireball.png"));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("⚠️ Fireball sprite not found!");
        }
    }

    public void update() {
        x += directionX * speed;
        y += directionY * speed;

        // deactivate if off-screen
        if (x < 0 || x > 800 || y < 0 || y > 600) {
            active = false;
        }
    }

    public void draw(Graphics g) {
        if (sprite != null) {
            g.drawImage(sprite, x, y, 32, 32, null);
        } else {
            g.setColor(Color.ORANGE);
            g.fillOval(x, y, 10, 10);
        }
    }
}
