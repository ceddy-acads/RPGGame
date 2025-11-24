package entities;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;
import javax.imageio.ImageIO;
import java.io.IOException;
import maps.Map; // Import the Map class

public class Enemy {
    
    //FOR DAMAGE
    private int attackDamage = 10;
    
    // Use doubles for precise position tracking
    private double x, y;
    public int width, height;
    public int hp;
    public double speed;
    private BufferedImage sprite;
    private boolean alive = true;
    private int flashRed = 0;
    private BufferedImage idleImg;
    private BufferedImage[] walkFrames;
    private int currentFrame = 0;
    private int frameDelay = 10;
    private int frameTimer = 0;
    
    private boolean facingLeft = false;
    
    // Retreat logic
    private boolean retreating = false;
    private double retreatTargetX, retreatTargetY;
    private final double RETREAT_DISTANCE = 300; // How far enemies will try to spread
    private final double RETREAT_SPEED_MULTIPLIER = 0.5; // Retreat slower
    
    // Map reference for collision detection
    private Map currentMap;

    //FOR ATTACKING
    private BufferedImage[] attackFrames;
    private int attackFrame = 0;
    private int attackTimer = 0;
    private int attackDelay = 4; // lower = faster animation
    private boolean attacking = false;
    private int attackCooldown = 0; // prevents constant attacking
    private int attackFrameDelay = 4; // lower = faster attack animation
    private int attackFrameTimer = 0;



    private void loadSprites() {
        try {
            //FOR WALKING
            idleImg = ImageIO.read(getClass().getResourceAsStream("/assets/characters/enemies/enemy_idle.png"));
            walkFrames = new BufferedImage[] {
                
                ImageIO.read(getClass().getResourceAsStream("/assets/characters/enemies/Golem_01_Walking_000.png")),
                ImageIO.read(getClass().getResourceAsStream("/assets/characters/enemies/Golem_01_Walking_001.png")),
                ImageIO.read(getClass().getResourceAsStream("/assets/characters/enemies/Golem_01_Walking_002.png")),
                ImageIO.read(getClass().getResourceAsStream("/assets/characters/enemies/Golem_01_Walking_003.png")),
                ImageIO.read(getClass().getResourceAsStream("/assets/characters/enemies/Golem_01_Walking_004.png")),
                ImageIO.read(getClass().getResourceAsStream("/assets/characters/enemies/Golem_01_Walking_005.png")),
                ImageIO.read(getClass().getResourceAsStream("/assets/characters/enemies/Golem_01_Walking_006.png")),
                ImageIO.read(getClass().getResourceAsStream("/assets/characters/enemies/Golem_01_Walking_007.png")),
                ImageIO.read(getClass().getResourceAsStream("/assets/characters/enemies/Golem_01_Walking_008.png")),
                ImageIO.read(getClass().getResourceAsStream("/assets/characters/enemies/Golem_01_Walking_009.png")),
                ImageIO.read(getClass().getResourceAsStream("/assets/characters/enemies/Golem_01_Walking_010.png")),
                ImageIO.read(getClass().getResourceAsStream("/assets/characters/enemies/Golem_01_Walking_011.png")),
                ImageIO.read(getClass().getResourceAsStream("/assets/characters/enemies/Golem_01_Walking_012.png")),
                ImageIO.read(getClass().getResourceAsStream("/assets/characters/enemies/Golem_01_Walking_013.png")),
                ImageIO.read(getClass().getResourceAsStream("/assets/characters/enemies/Golem_01_Walking_014.png")),
                ImageIO.read(getClass().getResourceAsStream("/assets/characters/enemies/Golem_01_Walking_015.png")),
                ImageIO.read(getClass().getResourceAsStream("/assets/characters/enemies/Golem_01_Walking_016.png")),
                ImageIO.read(getClass().getResourceAsStream("/assets/characters/enemies/Golem_01_Walking_017.png")),
            };
            
          //FOR ATTACKING
           attackFrames = new BufferedImage[] {  
                
                ImageIO.read(getClass().getResourceAsStream("/assets/characters/enemies/Golem_01_Attacking_000.png")),
                ImageIO.read(getClass().getResourceAsStream("/assets/characters/enemies/Golem_01_Attacking_001.png")),
                ImageIO.read(getClass().getResourceAsStream("/assets/characters/enemies/Golem_01_Attacking_002.png")),
                ImageIO.read(getClass().getResourceAsStream("/assets/characters/enemies/Golem_01_Attacking_003.png")),
                ImageIO.read(getClass().getResourceAsStream("/assets/characters/enemies/Golem_01_Attacking_004.png")),
                ImageIO.read(getClass().getResourceAsStream("/assets/characters/enemies/Golem_01_Attacking_005.png")),
                ImageIO.read(getClass().getResourceAsStream("/assets/characters/enemies/Golem_01_Attacking_006.png")),
                ImageIO.read(getClass().getResourceAsStream("/assets/characters/enemies/Golem_01_Attacking_007.png")),
                ImageIO.read(getClass().getResourceAsStream("/assets/characters/enemies/Golem_01_Attacking_008.png")),
                ImageIO.read(getClass().getResourceAsStream("/assets/characters/enemies/Golem_01_Attacking_009.png")),
                ImageIO.read(getClass().getResourceAsStream("/assets/characters/enemies/Golem_01_Attacking_010.png")),
                ImageIO.read(getClass().getResourceAsStream("/assets/characters/enemies/Golem_01_Attacking_011.png")),

            };
            sprite = idleImg; // default image
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public Enemy(int x, int y, Map map) {
        this.x = (double) x;
        this.y = (double) y;
        this.width = 60; // reduced width
        this.height = 60; // reduced height
        this.hp = 400;
        this.speed = 0.8; // Further reduced speed
        this.currentMap = map; // Store map reference

        loadSprites();
        
        // Placeholder red square (replace with image later)
        sprite = null;
    }


    public void update(int playerX, int playerY, Player player) {
        if (!alive) return;

        if (!player.isAlive()) {
            // Player is dead, retreat by moving away from player's position
            retreating = true;
            
            // Calculate direction away from player
            double dx = x - playerX; // Reversed: away from player
            double dy = y - playerY; // Reversed: away from player
            double dist = Math.sqrt(dx * dx + dy * dy);

            if (dist > 0.1) { // Avoid division by zero
                // Move away from player - normalize and multiply by speed
                double moveX = (dx / dist) * speed * RETREAT_SPEED_MULTIPLIER;
                double moveY = (dy / dist) * speed * RETREAT_SPEED_MULTIPLIER;
                
                // Check collision before moving horizontally
                int nextX = (int) (x + moveX);
                if (currentMap.isWalkable(nextX, (int) y, width, height)) {
                    x += moveX;
                }
                
                // Check collision before moving vertically
                int nextY = (int) (y + moveY);
                if (currentMap.isWalkable((int) x, nextY, width, height)) {
                    y += moveY;
                }
                
                facingLeft = dx < 0; // Face the direction of retreat
                
                // Animate walking during retreat
                frameTimer++;
                if (frameTimer >= frameDelay) {
                    currentFrame = (currentFrame + 1) % walkFrames.length;
                    frameTimer = 0;
                }
                sprite = walkFrames[currentFrame];
            } else {
                // Enemy is on the exact same position, move in a random direction
                Random rand = new Random();
                double angle = rand.nextDouble() * 2 * Math.PI;
                double moveX = speed * RETREAT_SPEED_MULTIPLIER * Math.cos(angle);
                double moveY = speed * RETREAT_SPEED_MULTIPLIER * Math.sin(angle);
                
                // Check collision before moving
                int nextX = (int) (x + moveX);
                int nextY = (int) (y + moveY);
                if (currentMap.isWalkable(nextX, nextY, width, height)) {
                    x += moveX;
                    y += moveY;
                }
            }
            return; // Stop further updates if retreating
        } else {
            retreating = false; // Reset retreating flag if player is alive again
        }
        
        // Normal enemy behavior (move toward player and attack)
        double dx = playerX - x;
        double dy = playerY - y;
        double dist = Math.sqrt(dx * dx + dy * dy);

        facingLeft = dx < 0;  // face left if player is to the left

        if (dist > 10 && !attacking) { // Move toward player only if not in the middle of an attack
            double moveX = (dx / dist) * speed;
            double moveY = (dy / dist) * speed;
            
            // Check collision before moving horizontally
            int nextX = (int) (x + moveX);
            if (currentMap.isWalkable(nextX, (int) y, width, height)) {
                x += moveX;
            }
            
            // Check collision before moving vertically
            int nextY = (int) (y + moveY);
            if (currentMap.isWalkable((int) x, nextY, width, height)) {
                y += moveY;
            }

            // Animate walking
            frameTimer++;
            if (frameTimer >= frameDelay) {
                currentFrame = (currentFrame + 1) % walkFrames.length;
                frameTimer = 0;
            }
            sprite = walkFrames[currentFrame];
        } else { 
            // Close enough to attack or already attacking
            if (!attacking && attackCooldown <= 0) {
                attacking = true;
                attackFrame = 0;
                attackCooldown = 90; 
                System.out.println("Enemy Attacking!");
                player.takeDamage(attackDamage); // Deal damage at the start of the attack animation
            }

            if (attacking) {
                attackFrameTimer++;
                if (attackFrameTimer >= attackFrameDelay) {
                    attackFrame++;
                    attackFrameTimer = 0;

                    // End attack if finished all frames
                    if (attackFrame >= attackFrames.length) {
                        attackFrame = 0;
                        attacking = false;
                    }
                }
                
                if (attacking) {
                    sprite = attackFrames[attackFrame];
                }
            } else {
                // If not attacking and close, set to idle and manage cooldown
                sprite = idleImg;
                if (attackCooldown > 0) {
                    attackCooldown--;
                }
            }
        }
    }


    public void draw(Graphics g, int screenX, int screenY) {
        if (!alive) return;

        if (facingLeft) {
            g.drawImage(sprite, screenX + width, screenY, -width, height, null);  // flip horizontally
        } else {
            g.drawImage(sprite, screenX, screenY, width, height, null);
        }

        // HP bar (using 400 as max HP)
        g.setColor(Color.WHITE);
        g.fillRect(screenX, screenY - 10, width, 5);
        g.setColor(Color.GREEN);
        g.fillRect(screenX, screenY - 10, (int) (width * (hp / 400.0)), 5);
    }

 
    public Rectangle getBounds() {
        return new Rectangle((int) x, (int) y, width, height);
    }

    public void takeDamage(int amount) {
        hp -= amount;
        flashRed = 5; // for hit flash effect (optional)
        if (hp <= 0) {
            alive = false;
            System.out.println("Enemy defeated!");
        }
        System.out.println("Enemy HP: " + hp);

    }

    public boolean isAlive() {
        return alive;
    }

    public boolean isRetreating() {
        return retreating;
    }

    public int getX() {
        return (int) x;
    }

    public int getY() {
        return (int) y;
    }
}
