package entities;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class SlashAttack {
    public int x, y;
    public boolean active = true;
    private int direction;
    
    private BufferedImage[] frames;
    private int frame = 0;
    private float accumulatedTime = 0f;
    private final int frameDelay = 4; // Reduced from 8 to 4 for faster animation
    
    private final float secondsPerFrame;
    private int width = 50;  // Match character size
    private int height = 50; // Match character size

    private List<Enemy> hitEnemies = new ArrayList<>();
    
    public static final int DOWN = 0;
    public static final int LEFT = 1;
    public static final int RIGHT = 2;
    public static final int UP = 3;
    public static final int UP_LEFT = 4;
    public static final int UP_RIGHT = 5;
    public static final int DOWN_LEFT = 6;
    public static final int DOWN_RIGHT = 7;
    
    public Rectangle getBounds() {
        return new Rectangle((int)x, (int)y, width, height);
    }
    
    private int damage;
    public int getDamage() {
        return damage;
    }

    public boolean hasHit(Enemy enemy) {
        return hitEnemies.contains(enemy);
    }

    public void addHitEnemy(Enemy enemy) {
        hitEnemies.add(enemy);
    }
    
    public SlashAttack(int x, int y, int direction, int playerAttack) {
        this.x = x;
        this.y = y;
        this.direction = direction;
        // Randomize damage: between 5 and playerAttack + 20%
        // Ensure minimum damage is 5
        int minDamage = 5;
        int maxDamage = (int)(playerAttack * 1.2); // PlayerAttack + 20%
        this.damage = minDamage + (int)(Math.random() * (maxDamage - minDamage + 1));

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
        boolean facingLeft = (direction == LEFT || direction == UP_LEFT || direction == DOWN_LEFT);
        if (facingLeft) {
            g.drawImage(currentFrame, screenX + width, screenY, -width, height, null);
        } else {
            g.drawImage(currentFrame, screenX, screenY, width, height, null);
        }
    }
}
