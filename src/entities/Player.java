package entities;
import java.awt.*;
import java.awt.Graphics2D;
import java.awt.Image;
import javax.swing.ImageIcon;
import input.KeyHandler;
import maps.Map; 
import java.util.ArrayList;
import java.util.Iterator;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;

public class Player {
    private int qCooldown = 0;
    private final int Q_COOLDOWN_MAX = 30; // 30 frames = 0.5 sec at 60FPS
    private int wCooldown = 0;
    private final int W_COOLDOWN_MAX = 60; // 1 sec

    //HP of the character
    private int maxHp = 100;
    private int hp = 100;
    private boolean alive = true;
    private boolean takingDamage = false;
    private int flashTimer = 0;

    // Player stats
    private int baseAttack = 100;
    private int baseDefense = 5;
    private int equippedAttack = 0;
    private int equippedDefense = 0;

    // Position stored as doubles to support diagonal normalization cleanly
    public double px, py; // Made public for direct access in GameLoop for camera
    private double speed;
    private KeyHandler keyH;
    private Map currentMap; // Reference to the current map for collision

    // State constants
    private static final int IDLE = 0;
    private static final int WALKING = 1;
    private static final int ATTACKING = 2;
    private static final int DYING = 3;
    private static final int HURT = 4; // New state for taking damage
    private int state = IDLE;  // Start in idle state

    // Animation frames [direction][frameIndex]
    private Image[][] frames;
    private Image[][] dieFrames; // New array for death animation frames
    private Image[][] attackFrames;
    private Image[][] idleFrames;
    private Image[][] hurtFrames;
    private Image currentImg;  // General image
    private int deathDirection = DOWN;
    private int frameIndex = 0;        // Default for walking
    private float accumulatedAnimationTime = 0f;  // For time-based animation
    private final float playerFrameDuration = 0.1f;  // Time per frame
    private boolean deathAnimationFinished = false; // Flag to indicate if death animation is done
    private static final int HURT_FRAMES = 5;

    // Direction constants
    private static final int DOWN = 0;
    private static final int LEFT = 1;
    private static final int RIGHT = 2;
    private static final int UP = 3;
    private static final int UP_LEFT = 4;
    private static final int UP_RIGHT = 5;
    private static final int DOWN_LEFT = 6;
    private static final int DOWN_RIGHT = 7;
    private int currentDirection = DOWN;
    
    // Player dimensions for collision
    public final int playerWidth = 80; // Increased width to match TILE_SIZE
    public final int playerHeight = 80; // Increased height to match TILE_SIZE

    // Slash attacks for SkillQ
    private final ArrayList<SlashAttack> slashes = new ArrayList<>();
    // SkillW attacks
    private final ArrayList<SkillWAttack> skillWAttacks = new ArrayList<>();

    public ArrayList<SlashAttack> getSlashes() {
        return slashes;
    }

    public ArrayList<SkillWAttack> getSkillWAttacks() {
        return skillWAttacks;
    }

    public int getTotalAttack() {
        return baseAttack + equippedAttack;
    }

    public int getTotalDefense() {
        return baseDefense + equippedDefense;
    }

    public void setEquippedStats(int attack, int defense) {
        this.equippedAttack = attack;
        this.equippedDefense = defense;
    }

    public void takeDamage(int amount) {
        if (!alive || state == DYING || state == HURT) return;

        int damageTaken = Math.max(0, amount - getTotalDefense());
        hp -= damageTaken;
        state = HURT;
        frameIndex = 0;
        accumulatedAnimationTime = 0f;

        if (hp <= 0) {
            hp = 0;
            alive = false;
            this.deathDirection = this.currentDirection; // Store direction at time of death
            state = DYING; // Set state to DYING
            frameIndex = 0; // Start death animation from first frame
            accumulatedAnimationTime = 0f; // Reset animation timer
            System.out.println("Player defeated!");
        } else {
            System.out.println("Player HP: " + hp);
        }
    }

    public Player(int startX, int startY, KeyHandler keyH, Map map) { // Modified constructor
        this.keyH = keyH;
        this.currentMap = map; // Store map reference
        this.px = startX;
        this.py = startY;
        this.speed = 4.0;
        this.hp = 100;
        loadFrames();  // Load all frames, including idle and die frames
        currentImg = idleFrames[DOWN][0];  // Start with idle image
        // Initial facing based on keyH is removed, will be set by movement
    }

    // Load frames for 4 directions and reuse for diagonals if needed
    private void loadFrames() {
        frames = new Image[8][6]; // 8 directions, 6 frames each
        attackFrames = new Image[8][6];
        idleFrames = new Image[8][6];
        BufferedImage spriteSheet = loadSpriteSheet("/assets/characters/player_walk.png");

        if (spriteSheet != null) {
            
            for (int i = 0; i < 6; i++) { 
                frames[DOWN][i] = getSubImage(spriteSheet, i, 0);
                frames[LEFT][i] = getSubImage(spriteSheet, i, 1); 
                frames[RIGHT][i] = getSubImage(spriteSheet, i, 2);
                frames[UP][i] = getSubImage(spriteSheet, i, 3);
            }
        }

        // Diagonals reuse vertical/horizontal frames
        frames[UP_LEFT] = frames[UP];
        frames[UP_RIGHT] = frames[UP];
        frames[DOWN_LEFT] = frames[DOWN];
        frames[DOWN_RIGHT] = frames[DOWN];

        // Load death animation frames from sprite sheet
        dieFrames = new Image[8][6]; // Match frames structure
        BufferedImage dieSpriteSheet = loadSpriteSheet("/assets/characters/player_death.png");
        if (dieSpriteSheet != null) {
            // Row 0: Die Down
            // Row 1: Die Left
            // Row 2: Die Right
            // Row 3: Die Up (Assuming typo from Row 4)
            for (int i = 0; i < 6; i++) { // 6 frames per direction
                dieFrames[DOWN][i] = getSubImage(dieSpriteSheet, i, 0);
                dieFrames[LEFT][i] = getSubImage(dieSpriteSheet, i, 1);
                dieFrames[RIGHT][i] = getSubImage(dieSpriteSheet, i, 2);
                dieFrames[UP][i] = getSubImage(dieSpriteSheet, i, 3);
            }
             // Map diagonals
            dieFrames[UP_LEFT] = dieFrames[LEFT];
            dieFrames[UP_RIGHT] = dieFrames[RIGHT];
            dieFrames[DOWN_LEFT] = dieFrames[LEFT];
            dieFrames[DOWN_RIGHT] = dieFrames[RIGHT];
        } else {
            System.err.println("Player: missing die sprite sheet.");
        }
        
        BufferedImage attackSpriteSheet = loadSpriteSheet("/assets/characters/playerwalk_attack.png");
        if(attackSpriteSheet != null){
            for (int i = 0; i < 6; i++) { 
                attackFrames[DOWN][i] = getSubImage(attackSpriteSheet, i, 0);
                attackFrames[LEFT][i] = getSubImage(attackSpriteSheet, i, 1); 
                attackFrames[RIGHT][i] = getSubImage(attackSpriteSheet, i, 2);
                attackFrames[UP][i] = getSubImage(attackSpriteSheet, i, 3);
            }
        }
        
        attackFrames[UP_LEFT] = attackFrames[UP];
        attackFrames[UP_RIGHT] = attackFrames[UP];
        attackFrames[DOWN_LEFT] = attackFrames[DOWN];
        attackFrames[DOWN_RIGHT] = attackFrames[DOWN];

        // Use the first frame of walking animation as the idle frame
        for (int i = 0; i < 6; i++) { // Assign the first frame to all idle frames to stop animation
            idleFrames[DOWN][i] = frames[DOWN][0];
            idleFrames[LEFT][i] = frames[LEFT][0];
            idleFrames[RIGHT][i] = frames[RIGHT][0];
            idleFrames[UP][i] = frames[UP][0];
        }

        idleFrames[UP_LEFT] = idleFrames[UP];
        idleFrames[UP_RIGHT] = idleFrames[UP];
        idleFrames[DOWN_LEFT] = idleFrames[DOWN];
        idleFrames[DOWN_RIGHT] = idleFrames[DOWN];

        // Load hurt animation frames
        hurtFrames = new Image[8][HURT_FRAMES];
        BufferedImage hurtSpriteSheet = loadSpriteSheet("/assets/characters/player_hurt.png");
        if (hurtSpriteSheet != null) {
            for (int i = 0; i < HURT_FRAMES; i++) {
                hurtFrames[DOWN][i] = getSubImage(hurtSpriteSheet, i, 0);
                hurtFrames[LEFT][i] = getSubImage(hurtSpriteSheet, i, 1);
                hurtFrames[RIGHT][i] = getSubImage(hurtSpriteSheet, i, 2);
                hurtFrames[UP][i] = getSubImage(hurtSpriteSheet, i, 3);
            }
        }
        hurtFrames[UP_LEFT] = hurtFrames[UP];
        hurtFrames[UP_RIGHT] = hurtFrames[UP];
        hurtFrames[DOWN_LEFT] = hurtFrames[DOWN];
        hurtFrames[DOWN_RIGHT] = hurtFrames[DOWN];
    }
    
    private BufferedImage loadSpriteSheet(String path) {
        try {
            java.net.URL res = getClass().getResource(path);
            if (res != null) {
                return ImageIO.read(res);
            }
        } catch (IOException e) {
            System.err.println("Could not load sprite sheet: " + path);
        }
        return null;
    }

    private Image getSubImage(BufferedImage spriteSheet, int col, int row) {
        int spriteWidth = 64;
        int spriteHeight = 64;
        return spriteSheet.getSubimage(col * spriteWidth, row * spriteHeight, spriteWidth, spriteHeight);
    }

    private Image loadImg(String path) {
        String[] candidates = { path, "/sprites/" + path.substring(path.lastIndexOf('/') + 1), "/assets/sprites/" + path.substring(path.lastIndexOf('/') + 1) };
        for (String p : candidates) {
            try {
                java.net.URL res = getClass().getResource(p);
                if (res != null) {
                    return new ImageIcon(res).getImage();
                }
            } catch (Exception e) {
                // Ignore and try next
                System.err.println("Could not load image: " + p);
            }
        }
        return null;  // Return null if not found
    }

    public void update(float deltaTime) { // Removed map parameter, now uses stored map
        if (!alive && deathAnimationFinished) {
            return; // Stop updating if dead and animation finished
        }

        if (state == DYING) {
            accumulatedAnimationTime += deltaTime;
            if (accumulatedAnimationTime >= playerFrameDuration) {
                frameIndex++;
                accumulatedAnimationTime -= playerFrameDuration;
                if (frameIndex >= dieFrames[0].length) { // Check against number of frames in one sequence
                    frameIndex = dieFrames[0].length - 1; // Stay on the last frame
                    deathAnimationFinished = true;
                }
            }
            // If dying, skip all movement and attack logic
            return;
        }

        if (state == HURT) {
            accumulatedAnimationTime += deltaTime;
            if (accumulatedAnimationTime >= playerFrameDuration) {
                frameIndex++;
                accumulatedAnimationTime -= playerFrameDuration;
                if (frameIndex >= HURT_FRAMES) {
                    frameIndex = 0;
                    state = IDLE; // Return to idle after hurt animation
                }
            }
            // Skip other logic when hurt
            return;
        }

        if (qCooldown > 0) qCooldown--;
        if (wCooldown > 0) wCooldown--;

        // --- Q Attack: cooldown-limited, one press = one attack ---
        if (keyH.skillSPACE && qCooldown == 0) { // Changed to skillSPACE
            useSkillQ();
            qCooldown = Q_COOLDOWN_MAX;
            keyH.skillSPACE = false; // Reset the skill key after use
        }

        // --- W Attack: cooldown-limited, one press = one attack ---
        if (keyH.skillW && wCooldown == 0) {
            useSkillW();
            wCooldown = W_COOLDOWN_MAX;
            keyH.skillW = false; // Reset the skill key after use
        }

        // Movement input aggregated
        double dx = 0.0, dy = 0.0;
        if (keyH.upPressed) dy -= speed;
        if (keyH.downPressed) dy += speed;
        if (keyH.leftPressed) dx -= speed;
        if (keyH.rightPressed) dx += speed;

        // Normalize diagonal
        if (dx != 0 && dy != 0) {
            dx *= 0.7071067811865476; // 1/sqrt(2)
            dy *= 0.7071067811865476;
        }

        // Apply movement with collision detection
        int nextX = (int) (px + dx);
        int nextY = (int) (py + dy);

        // Check horizontal movement
        if (dx != 0) {
            if (currentMap.isWalkable(nextX, (int) py, playerWidth, playerHeight)) {
                px += dx;
            }
        }

        // Check vertical movement
        if (dy != 0) {
            if (currentMap.isWalkable((int) px, nextY, playerWidth, playerHeight)) {
                py += dy;
            }
        }
        
        boolean isAttacking = !slashes.isEmpty() || !skillWAttacks.isEmpty();
        if (isAttacking) {
            state = ATTACKING;
        } else {
            if (dx != 0 || dy != 0) {
                state = WALKING;
            } else {
                state = IDLE;
            }
        }
        // Update active attacks
        Iterator<SlashAttack> it = slashes.iterator();
        while (it.hasNext()) {
            SlashAttack s = it.next();
            s.update(deltaTime);
            if (!s.active) it.remove();
        }
        Iterator<SkillWAttack> skillWIt = skillWAttacks.iterator();
        while (skillWIt.hasNext()) {
            SkillWAttack s = skillWIt.next();
            s.update(deltaTime);
            if (!s.active) skillWIt.remove();
        }
        
        // Determine current facing based on last input vector, but only if not attacking
        if (!isAttacking) { 
            if (dx > 0 && dy < 0) currentDirection = UP_RIGHT;
            else if (dx > 0 && dy > 0) currentDirection = DOWN_RIGHT;
            else if (dx < 0 && dy < 0) currentDirection = UP_LEFT;
            else if (dx < 0 && dy > 0) currentDirection = DOWN_LEFT;
            else if (dx > 0) currentDirection = RIGHT;
            else if (dx < 0) currentDirection = LEFT;
            else if (dy < 0) currentDirection = UP;
            else if (dy > 0) currentDirection = DOWN;
        }

        // Animation logic based on state
        switch (state) {
            case HURT:
                currentImg = hurtFrames[currentDirection][frameIndex];
                break;
            case ATTACKING:
                accumulatedAnimationTime += deltaTime;
                while (accumulatedAnimationTime >= playerFrameDuration) {
                    frameIndex++;
                    if (frameIndex > 5) frameIndex = 0;
                    accumulatedAnimationTime -= playerFrameDuration;
                }
                currentImg = attackFrames[currentDirection][frameIndex];
                break;
            case WALKING:
                accumulatedAnimationTime += deltaTime;
                while (accumulatedAnimationTime >= playerFrameDuration) {
                    frameIndex++;
                    if (frameIndex > 5) frameIndex = 0;
                    accumulatedAnimationTime -= playerFrameDuration;
                }
                currentImg = frames[currentDirection][frameIndex];
                break;
            case IDLE:
                accumulatedAnimationTime += deltaTime;
                while (accumulatedAnimationTime >= playerFrameDuration) {
                    frameIndex++;
                    if (frameIndex > 5) frameIndex = 0;
                    accumulatedAnimationTime -= playerFrameDuration;
                }
                currentImg = idleFrames[currentDirection][frameIndex];
                break;
        }

        // Skills E/R/T — dummy for now
        if (keyH.skillE) {
            state = ATTACKING;
            useSkillE();
            keyH.skillE = false; // Reset to prevent continuous skill use
        }
        if (keyH.skillR) {
            state = ATTACKING;
            useSkillR();
            keyH.skillR = false; // Reset to prevent continuous skill use
        }
        if (keyH.skillT) {
            state = ATTACKING;
            useSkillT();
            keyH.skillT = false; // Reset to prevent continuous skill use
        }
    }

    public void draw(Graphics g, int screenX, int screenY) {
        if (!alive && deathAnimationFinished) {
            return; // Don't draw anything if dead and animation finished
        }

        Graphics2D g2 = (Graphics2D) g;
        int drawX = screenX - playerWidth / 2;
        int drawY = screenY - playerHeight / 2;
        int width = playerWidth;
        int height = playerHeight;

        // Draw death animation if dying
        if (state == DYING && !deathAnimationFinished) {
            Image currentDieFrame = dieFrames[deathDirection][frameIndex];
            if (currentDieFrame != null) {
                g2.drawImage(currentDieFrame, drawX, drawY, width, height, null);
            } else {
                g2.setColor(Color.DARK_GRAY); // Fallback for missing death frame
                g2.fillRect(drawX, drawY, width, height);
            }
            return; // Don't draw anything else if dying
        }


        if (currentImg != null) {
            g2.drawImage(currentImg, drawX, drawY, width, height, null);
        } else {
            g2.setColor(Color.BLUE);
            g2.fillRect(drawX, drawY, 32, 32);
        }

        // Draw HP bar above player
        g.setColor(Color.GRAY);
        g.fillRect(drawX, drawY - 10, width, 5);
        g.setColor(Color.GREEN);
        g.fillRect(drawX, drawY - 10, (int) (width * ((double) hp / maxHp)), 5);

        // Skill animations are drawn by GameLoop, not Player itself.
    }

    public void useSkillQ() {
        int drawX = (int) Math.round(px);
        int drawY = (int) Math.round(py);
        int offset = 20;
        int sx = drawX;
        int sy = drawY;
        switch (currentDirection) {
            case RIGHT: sx = drawX + offset; sy = drawY; break;
            case LEFT: sx = drawX - offset; sy = drawY; break;
            case UP: sx = drawX; sy = drawY - offset; break;
            case DOWN: sx = drawX; sy = drawY + offset; break;
            case UP_LEFT: sx = drawX - offset; sy = drawY - offset; break;
            case UP_RIGHT: sx = drawX + offset; sy = drawY - offset; break;
            case DOWN_LEFT: sx = drawX - offset; sy = drawY + offset; break;
            case DOWN_RIGHT: sx = drawX + offset; sy = drawY + offset; break;
        }
        slashes.add(new SlashAttack(sx, sy, currentDirection, getTotalAttack()));
        state = ATTACKING;
    }

    public void useSkillW() {
        int drawX = (int) Math.round(px);
        int drawY = (int) Math.round(py);
        int offset = 20;
        int sx = drawX;
        int sy = drawY;
        switch (currentDirection) {
            case RIGHT: sx = drawX + offset; sy = drawY; break;
            case LEFT: sx = drawX - offset; sy = drawY; break;
            case UP: sx = drawX; sy = drawY - offset; break;
            case DOWN: sx = drawX; sy = drawY + offset; break;
            case UP_LEFT: sx = drawX - offset; sy = drawY - offset; break;
            case UP_RIGHT: sx = drawX + offset; sy = drawY - offset; break;
            case DOWN_LEFT: sx = drawX - offset; sy = drawY + offset; break;
            case DOWN_RIGHT: sx = drawX + offset; sy = drawY + offset; break;
        }
        skillWAttacks.add(new SkillWAttack(sx, sy, currentDirection, getTotalAttack()));
        state = ATTACKING;
    }

    // Getters
    public int getX() {
        return (int) Math.round(this.px);
    }

    public int getY() {
        return (int) Math.round(this.py);
    }

    public boolean isAlive() {
        return alive;
    }
    public void useSkillE() { System.out.println("Skill E used"); }
    public void useSkillR() { System.out.println("Skill R used"); }
    public void useSkillT() { System.out.println("Skill T used"); }
}
