package entities;
import java.awt.*;
import java.awt.Graphics2D;
import java.awt.Image;
import javax.swing.ImageIcon;
import input.KeyHandler;
import maps.Map; // Import the Map class
import java.util.ArrayList;
import java.util.Iterator;

public class Player {
    private int qCooldown = 0;
    private final int Q_COOLDOWN_MAX = 30; // 30 frames = 0.5 sec at 60FPS
    private int wCooldown = 0;
    private final int W_COOLDOWN_MAX = 60; // 1 sec
    private boolean hasAttackedQ = false;
    private boolean hasAttackedW = false;

    //HP of the character
    private int maxHp = 100;
    private int hp = 100;
    private boolean alive = true;
    private boolean takingDamage = false;
    private int flashTimer = 0;

    // Position stored as doubles to support diagonal normalization cleanly
    public double px, py; // Made public for direct access in GameLoop for camera
    private double speed;
    private KeyHandler keyH;
    private Map currentMap; // Reference to the current map for collision

    // State constants
    private static final int IDLE = 0;
    private static final int WALKING = 1;
    private static final int ATTACKING = 2;
    private static final int DYING = 3; // New state for death animation
    private int state = IDLE;  // Start in idle state

    // Animation frames [direction][frameIndex] (we assume 3 frames per direction)
    private Image[][] frames;
    private Image[] dieFrames; // New array for death animation frames
    private Image currentImg;  // General image
    private Image idleImg;     // Specific idle image
    private int frameIndex = 1;        // Default for walking
    private float accumulatedAnimationTime = 0f;  // For time-based animation
    private final float playerFrameDuration = 0.1f;  // Time per frame
    private boolean deathAnimationFinished = false; // Flag to indicate if death animation is done

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
    private boolean facingRight = true;

    // Player dimensions for collision
    private final int playerWidth = 48; // Increased width to match TILE_SIZE
    private final int playerHeight = 48; // Increased height to match TILE_SIZE

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

    public void takeDamage(int amount) {
        if (!alive || state == DYING) return; // Cannot take damage if already dead or dying
        hp -= amount;
        takingDamage = true;
        flashTimer = 10; // short red flash
        if (hp <= 0) {
            hp = 0;
            alive = false;
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
        idleImg = loadImg("/assets/characters/Idle.png");  // Load your specific idle sprite
        currentImg = idleImg;  // Start with idle image
        // Initial facing based on keyH is removed, will be set by movement
    }

    // Load frames for 4 directions and reuse for diagonals if needed
    private void loadFrames() {
        frames = new Image[8][3]; // 8 directions, 3 frames each
        frames[UP][0] = loadImg("/assets/characters/player_up_1.png");
        frames[UP][1] = loadImg("/assets/characters/player_up_2.png");
        frames[UP][2] = loadImg("/assets/characters/player_up_3.png");
        frames[DOWN][0] = loadImg("/assets/characters/player_down_1.png");
        frames[DOWN][1] = loadImg("/assets/characters/player_down_2.png");
        frames[DOWN][2] = loadImg("/assets/characters/player_down_3.png");
        // We use right sprites and flip for left
        frames[RIGHT][0] = loadImg("/assets/characters/player_right_1.png");
        frames[RIGHT][1] = loadImg("/assets/characters/player_right_2.png");
        frames[RIGHT][2] = loadImg("/assets/characters/player_right_3.png");
        // Reuse right frames for left - flipping is done in draw()
        frames[LEFT][0] = frames[RIGHT][0];
        frames[LEFT][1] = frames[RIGHT][1];
        frames[LEFT][2] = frames[RIGHT][2];
        // Diagonals reuse vertical frames for now
        frames[UP_LEFT] = frames[UP];
        frames[UP_RIGHT] = frames[UP];
        frames[DOWN_LEFT] = frames[DOWN];
        frames[DOWN_RIGHT] = frames[DOWN];

        // Load death animation frames
        dieFrames = new Image[5]; // die_1.png to die_5.png
        for (int i = 0; i < 5; i++) {
            dieFrames[i] = loadImg("/assets/characters/die_" + (i + 1) + ".png");
            if (dieFrames[i] == null) {
                System.err.println("Player: missing die frame die_" + (i + 1) + ".png");
            }
        }
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
                if (frameIndex >= dieFrames.length) {
                    frameIndex = dieFrames.length - 1; // Stay on the last frame
                    deathAnimationFinished = true;
                }
            }
            // If dying, skip all movement and attack logic
            return;
        }

        if (qCooldown > 0) qCooldown--;
        if (wCooldown > 0) wCooldown--;

        // --- Q Attack: cooldown-limited, one press = one attack ---
        if (keyH.skillQ && !hasAttackedQ && qCooldown == 0) {
            useSkillQ();
            hasAttackedQ = true;
            qCooldown = Q_COOLDOWN_MAX;
        }
        if (!keyH.skillQ) {
            hasAttackedQ = false;
        }

        // --- W Attack: no cooldown in this example, but one press = one attack ---
        if (keyH.skillW && !hasAttackedW && wCooldown == 0) {
            useSkillW();
            hasAttackedW = true;
            wCooldown = W_COOLDOWN_MAX;
        }
        if (!keyH.skillW) {
            hasAttackedW = false;
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

        // Determine state based on movement
        if (dx != 0 || dy != 0) {
            state = WALKING;
        } else {
            state = IDLE;
        }

        // If any attack is active, override the state to ATTACKING
        if (!slashes.isEmpty() || !skillWAttacks.isEmpty()) {
            state = ATTACKING;
        }

        // Determine current facing based on last input vector, but only if not attacking
        if (state != ATTACKING) { // Only update direction if not attacking
            if (dx > 0 && dy < 0) currentDirection = UP_RIGHT;
            else if (dx > 0 && dy > 0) currentDirection = DOWN_RIGHT;
            else if (dx < 0 && dy < 0) currentDirection = UP_LEFT;
            else if (dx < 0 && dy > 0) currentDirection = DOWN_LEFT;
            else if (dx > 0) currentDirection = RIGHT;
            else if (dx < 0) currentDirection = LEFT;
            else if (dy < 0) currentDirection = UP;
            else if (dy > 0) currentDirection = DOWN;
        }

        // Animation (only if not attacking)
        if (state == WALKING) { // Only animate walking if in WALKING state
            accumulatedAnimationTime += deltaTime;
            while (accumulatedAnimationTime >= playerFrameDuration) {
                frameIndex++;
                if (frameIndex > 2) frameIndex = 0;  // Loop back to first frame
                accumulatedAnimationTime -= playerFrameDuration;
            }
            currentImg = frames[currentDirection][frameIndex];  // Update to walking frame
        } else if (state == IDLE) { // Only set idle image if in IDLE state
            currentImg = idleImg;  // Use the custom idle image
        }

        // Skills E/R/T — dummy for now
        if (keyH.skillE) {
            state = ATTACKING;
            useSkillE();
            keyH.skillE = false;
        }
        if (keyH.skillR) {
            state = ATTACKING;
            useSkillR();
            keyH.skillR = false;
        }
        if (keyH.skillT) {
            state = ATTACKING;
            useSkillT();
            keyH.skillT = false;
        }

        // Update slashes
        Iterator<SlashAttack> it = slashes.iterator();
        while (it.hasNext()) {
            SlashAttack s = it.next();
            s.update(deltaTime);
            if (!s.active) it.remove();
        }

        // Update skillW attacks
        Iterator<SkillWAttack> skillWIt = skillWAttacks.iterator();
        while (skillWIt.hasNext()) {
            SkillWAttack s = skillWIt.next();
            s.update(deltaTime);
            if (!s.active) skillWIt.remove();
        }

        // Handle DYING state
        if (state == DYING) {
            accumulatedAnimationTime += deltaTime;
            if (accumulatedAnimationTime >= playerFrameDuration) {
                frameIndex++;
                accumulatedAnimationTime -= playerFrameDuration;
                if (frameIndex >= dieFrames.length) {
                    frameIndex = dieFrames.length - 1; // Stay on the last frame
                    deathAnimationFinished = true;
                }
            }
            return; // Stop further updates if dying
        }

        // Return to idle or walking if no attacks are active
        if (slashes.isEmpty() && skillWAttacks.isEmpty()) {
            state = (dx != 0 || dy != 0) ? WALKING : IDLE;
        }
    }

    public void draw(Graphics g, int screenX, int screenY) {
        if (!alive && deathAnimationFinished) {
            return; // Don't draw anything if dead and animation finished
        }

        Graphics2D g2 = (Graphics2D) g;
        int drawX = screenX;
        int drawY = screenY;
        int width = playerWidth;
        int height = playerHeight;

        // Draw death animation if dying
        if (state == DYING && !deathAnimationFinished) {
            if (dieFrames[frameIndex] != null) {
                g2.drawImage(dieFrames[frameIndex], drawX, drawY, width, height, null);
            } else {
                g2.setColor(Color.DARK_GRAY); // Fallback for missing death frame
                g2.fillRect(drawX, drawY, width, height);
            }
            return; // Don't draw anything else if dying
        }


        // Flash red when hit
        if (takingDamage) {
            g2.setColor(Color.RED);
            flashTimer--;
            if (flashTimer <= 0) takingDamage = false;
            g2.fillRect(drawX, drawY, width, height);
        } else if (state != ATTACKING) { // Only draw player image if not attacking
            boolean facingLeft = (currentDirection == LEFT || currentDirection == UP_LEFT || currentDirection == DOWN_LEFT);
            if (currentImg != null) {
                if (facingLeft) {
                    g2.drawImage(currentImg, drawX + width, drawY, -width, height, null);
                } else {
                    g2.drawImage(currentImg, drawX, drawY, width, height, null);
                }
            } else {
                g2.setColor(Color.BLUE);
                g2.fillRect(drawX, drawY, 32, 32);
            }
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
        slashes.add(new SlashAttack(sx, sy, currentDirection));
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
        skillWAttacks.add(new SkillWAttack(sx, sy, currentDirection));
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
