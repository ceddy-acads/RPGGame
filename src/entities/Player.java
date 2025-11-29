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
    private final int Q_COOLDOWN_MAX = 30;
    private int wCooldown = 0;
    private final int W_COOLDOWN_MAX = 60;

    private int maxHp = 100;
    private int hp = 100;
    private boolean alive = true;
    private boolean takingDamage = false;
    private int flashTimer = 0;

    private int baseAttack = 100;

    private final int initialX;
    private final int initialY;
    private int baseDefense = 5;
    private int equippedAttack = 0;
    private int equippedDefense = 0;

    public double px, py;
    private double speed;
    private KeyHandler keyH;
    private Map currentMap;

    private static final int IDLE = 0;
    private static final int WALKING = 1;
    private static final int ATTACKING = 2;
    private static final int DYING = 3;
    private static final int HURT = 4;
    private static final int FIRESPLASH = 5;
    private static final int ICEPIERCER = 6;
    private int state = IDLE;

    private Image[][] frames;
    private Image[][] dieFrames;
    private Image[][] attackFrames;
    private Image[][] idleFrames;
    private Image[][] hurtFrames;
    private Image[][] firesplashFrames;
    private Image[][] icepiercerFrames;
    private Image currentImg;
    private int deathDirection = DOWN;
    private int frameIndex = 0;
    private float accumulatedAnimationTime = 0f;
    private final float playerFrameDuration = 0.1f;
    private boolean deathAnimationFinished = false;
    private static final int HURT_FRAMES = 5;

    private static final int DOWN = 0;
    private static final int LEFT = 1;
    private static final int RIGHT = 2;
    private static final int UP = 3;
    private static final int UP_LEFT = 4;
    private static final int UP_RIGHT = 5;
    private static final int DOWN_LEFT = 6;
    private static final int DOWN_RIGHT = 7;
    private int currentDirection = DOWN;
    
    public final int playerWidth = 100;
    public final int playerHeight = 100;

    private final ArrayList<SlashAttack> slashes = new ArrayList<>();
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
            this.deathDirection = this.currentDirection;
            state = DYING;
            frameIndex = 0;
            accumulatedAnimationTime = 0f;
            System.out.println("Player defeated!");
        } else {
            System.out.println("Player HP: " + hp);
        }
    }

    public Player(int startX, int startY, KeyHandler keyH, Map map) {
        this.initialX = startX;
        this.initialY = startY;
        this.keyH = keyH;
        this.currentMap = map;
        this.px = startX;
        this.py = startY;
        this.speed = 4.0;
        this.hp = maxHp;
        this.alive = true;
        this.state = IDLE;
        this.deathAnimationFinished = false;
        loadFrames();
        currentImg = idleFrames[DOWN][0];
    }

    public void resetPlayerState() {
        this.px = initialX;
        this.py = initialY;
        this.hp = maxHp;
        this.alive = true;
        this.state = IDLE;
        this.deathAnimationFinished = false;
        this.qCooldown = 0;
        this.wCooldown = 0;
        this.slashes.clear();
        this.skillWAttacks.clear();
    }

    private void loadFrames() {
        frames = new Image[8][6];
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

        frames[UP_LEFT] = frames[UP];
        frames[UP_RIGHT] = frames[UP];
        frames[DOWN_LEFT] = frames[DOWN];
        frames[DOWN_RIGHT] = frames[DOWN];

        dieFrames = new Image[8][6];
        BufferedImage dieSpriteSheet = loadSpriteSheet("/assets/characters/player_death.png");
        if (dieSpriteSheet != null) {
            for (int i = 0; i < 6; i++) {
                dieFrames[DOWN][i] = getSubImage(dieSpriteSheet, i, 0);
                dieFrames[LEFT][i] = getSubImage(dieSpriteSheet, i, 1);
                dieFrames[RIGHT][i] = getSubImage(dieSpriteSheet, i, 2);
                dieFrames[UP][i] = getSubImage(dieSpriteSheet, i, 3);
            }
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

        for (int i = 0; i < 6; i++) {
            idleFrames[DOWN][i] = frames[DOWN][0];
            idleFrames[LEFT][i] = frames[LEFT][0];
            idleFrames[RIGHT][i] = frames[RIGHT][0];
            idleFrames[UP][i] = frames[UP][0];
        }

        idleFrames[UP_LEFT] = idleFrames[UP];
        idleFrames[UP_RIGHT] = idleFrames[UP];
        idleFrames[DOWN_LEFT] = idleFrames[DOWN];
        idleFrames[DOWN_RIGHT] = idleFrames[DOWN];

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

        firesplashFrames = new Image[8][6];
        BufferedImage firesplashSpriteSheet = loadSpriteSheet("/assets/characters/player_firesplash.png");
        if (firesplashSpriteSheet != null) {
            for (int i = 0; i < 6; i++) {
                firesplashFrames[DOWN][i] = getSubImage(firesplashSpriteSheet, i, 0);
                firesplashFrames[LEFT][i] = getSubImage(firesplashSpriteSheet, i, 1);
                firesplashFrames[RIGHT][i] = getSubImage(firesplashSpriteSheet, i, 2);
                firesplashFrames[UP][i] = getSubImage(firesplashSpriteSheet, i, 3);
            }
            firesplashFrames[UP_LEFT] = firesplashFrames[LEFT];
            firesplashFrames[UP_RIGHT] = firesplashFrames[RIGHT];
        firesplashFrames[DOWN_LEFT] = firesplashFrames[LEFT];
        firesplashFrames[DOWN_RIGHT] = firesplashFrames[RIGHT];
        }

        icepiercerFrames = new Image[8][6];
        BufferedImage icepiercerSpriteSheet = loadSpriteSheet("/assets/characters/player_icepiercer.png");
        if (icepiercerSpriteSheet != null) {
            for (int i = 0; i < 6; i++) {
                icepiercerFrames[DOWN][i] = getSubImage(icepiercerSpriteSheet, i, 0);
                icepiercerFrames[LEFT][i] = getSubImage(icepiercerSpriteSheet, i, 1);
                icepiercerFrames[RIGHT][i] = getSubImage(icepiercerSpriteSheet, i, 2);
                icepiercerFrames[UP][i] = getSubImage(icepiercerSpriteSheet, i, 3);
            }
            icepiercerFrames[UP_LEFT] = icepiercerFrames[LEFT];
            icepiercerFrames[UP_RIGHT] = icepiercerFrames[RIGHT];
            icepiercerFrames[DOWN_LEFT] = icepiercerFrames[LEFT];
            icepiercerFrames[DOWN_RIGHT] = icepiercerFrames[RIGHT];
        }
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
                System.err.println("Could not load image: " + p);
            }
        }
        return null;
    }

    public void update(float deltaTime) {
        if (!alive && deathAnimationFinished) {
            return;
        }

        if (state == DYING) {
            accumulatedAnimationTime += deltaTime;
            if (accumulatedAnimationTime >= playerFrameDuration) {
                frameIndex++;
                accumulatedAnimationTime -= playerFrameDuration;
                if (frameIndex >= dieFrames[0].length) {
                    frameIndex = dieFrames[0].length - 1;
                    deathAnimationFinished = true;
                }
            }
            return;
        }

        if (state == HURT) {
            accumulatedAnimationTime += deltaTime;
            if (accumulatedAnimationTime >= playerFrameDuration) {
                frameIndex++;
                accumulatedAnimationTime -= playerFrameDuration;
                if (frameIndex >= HURT_FRAMES) {
                    frameIndex = 0;
                    state = IDLE;
                }
            }
            return;
        }

        if (qCooldown > 0) qCooldown--;
        if (wCooldown > 0) wCooldown--;

        if (keyH.skillSPACE && qCooldown == 0) {
            useSkillQ();
            qCooldown = Q_COOLDOWN_MAX;
            keyH.skillSPACE = false;
        }

        if (keyH.skillW && wCooldown == 0) {
            useSkillW();
            wCooldown = W_COOLDOWN_MAX;
            keyH.skillW = false;
        }

        double dx = 0.0, dy = 0.0;
        if (keyH.upPressed) dy -= speed;
        if (keyH.downPressed) dy += speed;
        if (keyH.leftPressed) dx -= speed;
        if (keyH.rightPressed) dx += speed;

        if (dx != 0 && dy != 0) {
            dx *= 0.7071067811865476;
            dy *= 0.7071067811865476;
        }

        int nextX = (int) (px + dx);
        int nextY = (int) (py + dy);

        if (dx != 0) {
            if (currentMap.isWalkable(nextX, (int) py, playerWidth, playerHeight)) {
                px += dx;
            }
        }

        if (dy != 0) {
            if (currentMap.isWalkable((int) px, nextY, playerWidth, playerHeight)) {
                py += dy;
            }
        }
        
        boolean isAttacking = !slashes.isEmpty() || !skillWAttacks.isEmpty();
        if (state != FIRESPLASH && state != HURT && state != DYING && state != ICEPIERCER) {
            boolean isMoving = (dx != 0 || dy != 0);

            if (isAttacking) {
                state = ATTACKING;
            } else if (isMoving) {
                state = WALKING;
            } else {
                state = IDLE;
                
            }
        }
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

        switch (state) {
            case ICEPIERCER:
                accumulatedAnimationTime += deltaTime;
                if (accumulatedAnimationTime >= playerFrameDuration) {
                    frameIndex++;
                    accumulatedAnimationTime -= playerFrameDuration;
                    if (frameIndex >= 6) {
                        frameIndex = 0;
                        state = IDLE;
                    }
                }
                if (frameIndex < 6) {
                    currentImg = icepiercerFrames[currentDirection][frameIndex];
                }
                break;
            case FIRESPLASH:
                accumulatedAnimationTime += deltaTime;
                if (accumulatedAnimationTime >= playerFrameDuration) {
                    frameIndex++;
                    accumulatedAnimationTime -= playerFrameDuration;
                    if (frameIndex >= 6) {
                        frameIndex = 0;
                        state = IDLE;
                    }
                }
                if (frameIndex < 6) {
                    currentImg = firesplashFrames[currentDirection][frameIndex];
                }
                break;
            case HURT:
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

        if (keyH.skillB) {
            useSkillB();
            keyH.skillB = false;
        }
        if (keyH.skillN) {
            useSkillN();
            keyH.skillN = false;
        }
        if (keyH.skillM) {
            state = ATTACKING;
            useSkillM();
            keyH.skillM = false;
        }
    }

    public void draw(Graphics g, int screenX, int screenY) {
        if (!alive && deathAnimationFinished) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g;
        int drawX = screenX - playerWidth / 2;
        int drawY = screenY - playerHeight / 2;
        int width = playerWidth;
        int height = playerHeight;

        if (state == DYING && !deathAnimationFinished) {
            Image currentDieFrame = dieFrames[deathDirection][frameIndex];
            if (currentDieFrame != null) {
                g2.drawImage(currentDieFrame, drawX, drawY, width, height, null);
            } else {
                g2.setColor(Color.DARK_GRAY);
                g2.fillRect(drawX, drawY, width, height);
            }
            return;
        }


        if (currentImg != null) {
            g2.drawImage(currentImg, drawX, drawY, width, height, null);
        } else {
            g2.setColor(Color.BLUE);
            g2.fillRect(drawX, drawY, 32, 32);
        }

        g.setColor(Color.GRAY);
        g.fillRect(drawX, drawY - 10, width, 5);
        g.setColor(Color.GREEN);
        g.fillRect(drawX, drawY - 10, (int) (width * ((double) hp / maxHp)), 5);
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

    public int getX() {
        return (int) Math.round(this.px);
    }

    public int getY() {
        return (int) Math.round(this.py);
    }

    public boolean isAlive() {
        return alive;
    }

    public boolean isDeathAnimationFinished() {
        return deathAnimationFinished;
    }

    public void useSkillB() {
        state = FIRESPLASH;
        frameIndex = 0;
        accumulatedAnimationTime = 0f;
    }
    public void useSkillN() {
        state = ICEPIERCER;
        frameIndex = 0;
        accumulatedAnimationTime = 0f;
    }
    public void useSkillM() { System.out.println("Skill M used"); }
}
