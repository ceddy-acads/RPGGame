package entities;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;
import javax.imageio.ImageIO;
import java.io.IOException;
import maps.Map;

public class Enemy {
    
    private int attackDamage = 10;
    
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
    
    private boolean retreating = false;
    private double retreatTargetX, retreatTargetY;
    private final double RETREAT_DISTANCE = 300;
    private final double RETREAT_SPEED_MULTIPLIER = 0.5;
    
    private Map currentMap;

    private BufferedImage[] attackFrames;
    private int attackFrame = 0;
    private int attackTimer = 0;
    private int attackDelay = 4;
    private boolean attacking = false;
    private int attackCooldown = 0;
    private int attackFrameDelay = 4;
    private int attackFrameTimer = 0;



    private void loadSprites() {
        try {
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
            sprite = idleImg;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public Enemy(int x, int y, Map map) {
        this.x = (double) x;
        this.y = (double) y;
        this.width = 60;
        this.height = 60;
        this.hp = 400;
        this.speed = 0.8;
        this.currentMap = map;

        loadSprites();
        
        sprite = null;
    }


    public void update(int playerX, int playerY, Player player) {
        if (!alive) return;

        if (!player.isAlive()) {
            retreating = true;
            
            double dx = x - playerX;
            double dy = y - playerY;
            double dist = Math.sqrt(dx * dx + dy * dy);

            if (dist > 0.1) {
                double moveX = (dx / dist) * speed * RETREAT_SPEED_MULTIPLIER;
                double moveY = (dy / dist) * speed * RETREAT_SPEED_MULTIPLIER;
                
                int nextX = (int) (x + moveX);
                if (currentMap.isWalkable(nextX, (int) y, width, height)) {
                    x += moveX;
                }
                
                int nextY = (int) (y + moveY);
                if (currentMap.isWalkable((int) x, nextY, width, height)) {
                    y += moveY;
                }
                
                facingLeft = dx < 0;
                
                frameTimer++;
                if (frameTimer >= frameDelay) {
                    currentFrame = (currentFrame + 1) % walkFrames.length;
                    frameTimer = 0;
                }
                sprite = walkFrames[currentFrame];
            } else {
                Random rand = new Random();
                double angle = rand.nextDouble() * 2 * Math.PI;
                double moveX = speed * RETREAT_SPEED_MULTIPLIER * Math.cos(angle);
                double moveY = speed * RETREAT_SPEED_MULTIPLIER * Math.sin(angle);
                
                int nextX = (int) (x + moveX);
                int nextY = (int) (y + moveY);
                if (currentMap.isWalkable(nextX, nextY, width, height)) {
                    x += moveX;
                    y += moveY;
                }
            }
            return;
        } else {
            retreating = false;
        }
        
        double dx = playerX - x;
        double dy = playerY - y;
        double dist = Math.sqrt(dx * dx + dy * dy);

        facingLeft = dx < 0;

        if (dist > 10 && !attacking) {
            double moveX = (dx / dist) * speed;
            double moveY = (dy / dist) * speed;
            
            int nextX = (int) (x + moveX);
            if (currentMap.isWalkable(nextX, (int) y, width, height)) {
                x += moveX;
            }
            
            int nextY = (int) (y + moveY);
            if (currentMap.isWalkable((int) x, nextY, width, height)) {
                y += moveY;
            }

            frameTimer++;
            if (frameTimer >= frameDelay) {
                currentFrame = (currentFrame + 1) % walkFrames.length;
                frameTimer = 0;
            }
            sprite = walkFrames[currentFrame];
        } else {
            if (!attacking && attackCooldown <= 0) {
                attacking = true;
                attackFrame = 0;
                attackCooldown = 90;
                System.out.println("Enemy Attacking!");
                Random rand = new Random();
                int minEnemyDamage = 5;
                int maxEnemyDamage = attackDamage + 5;
                int randomizedDamage = minEnemyDamage + rand.nextInt(maxEnemyDamage - minEnemyDamage + 1);
                player.takeDamage(randomizedDamage);
            }

            if (attacking) {
                attackFrameTimer++;
                if (attackFrameTimer >= attackFrameDelay) {
                    attackFrame++;
                    attackFrameTimer = 0;

                    if (attackFrame >= attackFrames.length) {
                        attackFrame = 0;
                        attacking = false;
                    }
                }
                
                if (attacking) {
                    sprite = attackFrames[attackFrame];
                }
            } else {
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
            g.drawImage(sprite, screenX + width, screenY, -width, height, null);
        } else {
            g.drawImage(sprite, screenX, screenY, width, height, null);
        }

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
        flashRed = 5;
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
