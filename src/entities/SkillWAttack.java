package entities;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SkillWAttack {
    public int x, y;
    public boolean active = true;
    private int direction;
    
    private BufferedImage[] frames;
    private int frame = 0;
    private float accumulatedTime = 0f;
    private final int frameDelay = 6;
    
    private final float secondsPerFrame;
    private int width = 50;  // Match character size
    private int height = 50; // Match character size

    private List<Enemy> hitEnemies = new ArrayList<>();

    public Rectangle getBounds() {
        return new Rectangle((int)x, (int)y, width, height);
    }
    
    private int damage;
    public int getDamage() { return damage; }

    public boolean hasHit(Enemy enemy) {
        return hitEnemies.contains(enemy);
    }

    public void addHitEnemy(Enemy enemy) {
        hitEnemies.add(enemy);
    }
    
    public SkillWAttack(int x, int y, int direction, int playerAttack) {
        this.x = x;
        this.y = y;
        this.direction = direction;
        // Randomize damage: ±20% variation
        double variation = 0.8 + Math.random() * 0.4;
        this.damage = Math.max(1, (int)(playerAttack * variation));

        this.secondsPerFrame = (float) frameDelay / 60.0f;

        frames = new BufferedImage[4];
    }
    
    public void update(float deltaTime) {
        if (!active) return;
        accumulatedTime += deltaTime;
        if (accumulatedTime >= secondsPerFrame) {
            frame++;
            accumulatedTime -= secondsPerFrame;
            if (frame >= frames.length) {
                active = false;
            }
        }
    }
    
    public void draw(Graphics g, int screenX, int screenY) {
        if (!active || frame >= frames.length || frames[frame] == null) return;
        BufferedImage currentFrame = frames[frame];
        boolean facingLeft = (direction == SlashAttack.LEFT || direction == SlashAttack.UP_LEFT || direction == SlashAttack.DOWN_LEFT);
        
        if (facingLeft) {
            g.drawImage(currentFrame, screenX + width, screenY, -width, height, null);
        } else {
            g.drawImage(currentFrame, screenX, screenY, width, height, null);
        }
    }
}
