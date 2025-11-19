package entities;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.InputStream;
import java.io.IOException;

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

    private boolean hasHit = false; // Prevent multiple hits from same slash
    
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
    
    private int damage = 14;
    public int getDamage() {
        return damage;
    }

    public boolean hasHit() {
        return hasHit;
    }

    public void setHasHit(boolean hasHit) {
        this.hasHit = hasHit;
    }
    
    public SlashAttack(int x, int y, int direction) {
        this.x = x;
        this.y = y;
        this.direction = direction;
        
        this.secondsPerFrame = (float) frameDelay / 60.0f;
        
        loadFrames();
    }
    
    private void loadFrames() {
        String[] names = { "slash_1.png", "slash_2.png", "slash_3.png", "slash_4.png" };
        frames = new BufferedImage[names.length];
        boolean allFramesLoaded = true;
        for (int i = 0; i < names.length; i++) {
            frames[i] = tryLoad(names[i]);
            if (frames[i] == null) {
                System.err.println("SlashAttack: missing frame " + names[i]);
                allFramesLoaded = false;
            }
        }
        // Don't override width/height from frame dimensions - use character size
        if (!allFramesLoaded) {
            active = false;
        }
    }
    
    private BufferedImage tryLoad(String filename) {
        String[] candidates = { "/sprites/" + filename, "/assets/sprites/" + filename, "/assets/" + filename, "/resources/sprites/" + filename };
        for (String path : candidates) {
            try (InputStream is = getClass().getResourceAsStream(path)) {
                if (is != null) {
                    return ImageIO.read(is);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
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
