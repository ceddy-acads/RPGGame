package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import input.KeyHandler;
import entities.Player;
import entities.Enemy;
import entities.SlashAttack;
import entities.SkillWAttack;
import entities.InventoryUI;
import entities.Hotbar;
import maps.Map;
import maps.MapLoader;

public class GameLoop extends JLayeredPane implements Runnable { 
	
    final int WIDTH = 800;
    final int HEIGHT = 600;
    final int TILE_SIZE = 48;

    private boolean inventoryOpen = false;
    private InventoryUI gameInventory;

    private Thread gameThread;
    private KeyHandler keyH;
    private Player player;
    private ArrayList<Enemy> enemies;
    private Map map;
    private Hotbar hotbar;
    private GameOverCallback gameOverCallback;

    public GameLoop(GameOverCallback gameOverCallback) {
        this.gameOverCallback = gameOverCallback;

        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setDoubleBuffered(true);

        keyH = new KeyHandler();
        this.setFocusable(true);
        this.requestFocusInWindow();
        this.addKeyListener(keyH);

        setupKeyBindings();

        BufferedImage mapImage = MapLoader.loadMapImage("forest");
        BufferedImage collisionMask = MapLoader.loadCollisionMask("forest");
        map = new Map(mapImage, collisionMask, TILE_SIZE);

        player = new Player(100, 100, keyH, map);

        enemies = new ArrayList<>();
        spawnEnemies();

        gameInventory = new InventoryUI(WIDTH, HEIGHT);

        hotbar = new Hotbar(WIDTH, HEIGHT, gameInventory);
        gameInventory.setBounds(0, 0, WIDTH, HEIGHT);
        gameInventory.setVisible(false);
        this.add(gameInventory, JLayeredPane.PALETTE_LAYER);
    }

    public void start() {
        startGameThread();
    }

    public void reset() {
        player = new Player(100, 100, keyH, map);
        
        enemies.clear();
        spawnEnemies();

        gameInventory.reset();

        inventoryOpen = false;
        gameInventory.setVisible(false);

        if (gameThread != null) {
            gameThread = null;
        }
    }

    private void setupKeyBindings() {
        InputMap im = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = this.getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_I, 0), "toggleInventory");
        am.put("toggleInventory", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleInventory();
            }
        });
    }

    private void toggleInventory() {
        inventoryOpen = !inventoryOpen;
        gameInventory.setVisible(inventoryOpen);
        if (inventoryOpen) {
            gameInventory.requestFocusInWindow();
        } else {
            this.requestFocusInWindow();
        }
    }

    private void spawnEnemies() {
        enemies.add(new Enemy(400, 300, map));
        enemies.add(new Enemy(600, 200, map));
        enemies.add(new Enemy(200, 400, map));
    }

    @Override
    public void addNotify() {
        super.addNotify();
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        double drawInterval = 1000000000.0 / 60.0;
        double delta = 0;
        long lastTime = System.nanoTime();

        while (gameThread != null) {
            long now = System.nanoTime();
            delta += (now - lastTime) / drawInterval;
            lastTime = now;

            if (delta >= 1) {
                update();
                repaint();
                delta--;
            }
        }
    }

    public void update() {
        if (inventoryOpen) {
            return;
        }

        float deltaTime = 1.0f / 60.0f;

    	for (SlashAttack slash : player.getSlashes()) {
    	    if (!slash.active) continue;
    	    Rectangle slashBounds = slash.getBounds();
    	    for (Enemy enemy : enemies) {
    	        if (enemy.isAlive() && slashBounds.intersects(enemy.getBounds())) {
                    if (!slash.hasHit(enemy)) {
                        enemy.takeDamage(slash.getDamage());
                        slash.addHitEnemy(enemy);
                    }
    	        }
    	    }
    	}

    	for (SkillWAttack skillW : player.getSkillWAttacks()) {
    	    if (!skillW.active) continue;
    	    Rectangle skillWBounds = skillW.getBounds();
    	    for (Enemy enemy : enemies) {
    	        if (enemy.isAlive() && skillWBounds.intersects(enemy.getBounds())) {
                    if (!skillW.hasHit(enemy)) {
                        enemy.takeDamage(skillW.getDamage());
                        skillW.addHitEnemy(enemy);
                    }
    	        }
    	    }
    	}

        player.update(deltaTime); 

        if (!player.isAlive() && player.isDeathAnimationFinished()) {
            gameThread = null;
            BufferedImage screenshot = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = screenshot.createGraphics();
            paintComponent(g2d);
            g2d.dispose();
            
            gameOverCallback.onGameOver(screenshot);
            return;
        }

        for (Enemy enemy : enemies) {
            enemy.update(player.getX(), player.getY(), player);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        int cameraX = (int) player.px - WIDTH / 2;
        int cameraY = (int) player.py - HEIGHT / 2;

        cameraX = Math.max(0, Math.min(cameraX, map.getMapWidth() - WIDTH));
        cameraY = Math.max(0, Math.min(cameraY, map.getMapHeight() - HEIGHT));

        map.render(g2d, cameraX, cameraY, WIDTH, HEIGHT);

        int playerScreenX = (int) player.px - cameraX;
        int playerScreenY = (int) player.py - cameraY;

        player.draw(g, playerScreenX, playerScreenY);

        for (Enemy enemy : enemies) {
            int enemyScreenX = enemy.getX() - cameraX;
            int enemyScreenY = enemy.getY() - cameraY;
            enemy.draw(g, enemyScreenX, enemyScreenY);
        }

        for (SlashAttack s : player.getSlashes()) {
            int slashScreenX = s.x - cameraX;
            int slashScreenY = s.y - cameraY;
            s.draw(g, slashScreenX, slashScreenY);
        }
        for (SkillWAttack s : player.getSkillWAttacks()) {
            int skillWScreenX = s.x - cameraX;
            int skillWScreenY = s.y - cameraY;
            s.draw(g, skillWScreenX, skillWScreenY);
        }

        hotbar.draw(g2d);
        drawHotbarKeys(g2d);
    }

    private void drawHotbarKeys(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));

        int slotSize = 48;
        int numSlots = 5;
        int hotbarWidth = numSlots * slotSize;
        int hotbarX = (WIDTH - hotbarWidth) / 2;
        int hotbarY = HEIGHT - slotSize - 10;

        String[] keys = {"", "", "B", "N", "M"};
        for (int i = 2; i < numSlots; i++) {
            String key = keys[i];
            FontMetrics fm = g2d.getFontMetrics();
            int stringWidth = fm.stringWidth(key);
            int x = hotbarX + i * slotSize + (slotSize - stringWidth) / 2;
            int y = hotbarY - 5;
            g2d.drawString(key, x, y);
        }
    }

    public interface GameOverCallback {
        void onGameOver(BufferedImage screenshot);
    }
}
