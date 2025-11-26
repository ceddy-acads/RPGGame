package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*; // Import AWT event classes for KeyAdapter and KeyEvent
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
    final int TILE_SIZE = 48; // Consistent tile size

    private boolean inventoryOpen = false; // To track inventory state
    private InventoryUI gameInventory; // The inventory panel

    private Thread gameThread;
    private KeyHandler keyH;
    private Player player;
    private ArrayList<Enemy> enemies;  // ✅ Enemy list
    private Map map; // Use the updated Map object
    private Hotbar hotbar;
    private GameOverCallback gameOverCallback; // Callback for game over

    public GameLoop(GameOverCallback gameOverCallback) { // Modified constructor
        this.gameOverCallback = gameOverCallback;

        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setDoubleBuffered(true);

        keyH = new KeyHandler();
        this.setFocusable(true);
        this.requestFocusInWindow();
        this.addKeyListener(keyH);

        setupKeyBindings();

        // Load map images and create Map object
        BufferedImage mapImage = MapLoader.loadMapImage("forest"); 
        BufferedImage collisionMask = MapLoader.loadCollisionMask("forest");
        map = new Map(mapImage, collisionMask, TILE_SIZE);

        // Initialize player with KeyHandler and Map
        player = new Player(100, 100, keyH, map); 

        // ✅ Initialize enemies
        enemies = new ArrayList<>();
        spawnEnemies();

        // Initialize inventory
        gameInventory = new InventoryUI(player);

        // Initialize hotbar
        hotbar = new Hotbar(WIDTH, HEIGHT, gameInventory);
        gameInventory.setBounds(0, 0, WIDTH, HEIGHT); 
        gameInventory.setVisible(false); 
        this.add(gameInventory, JLayeredPane.PALETTE_LAYER); 
    }

    public void start() {
        startGameThread();
    }

    public void reset() {
        // Reset player state
        player = new Player(100, 100, keyH, map); // Re-initialize player at start position, full HP
        
        // Clear and re-spawn enemies
        enemies.clear();
        spawnEnemies();

        // Reset inventory (if necessary, clear items or reset state)
        gameInventory.reset();

        // Reset any other game state variables
        inventoryOpen = false;
        gameInventory.setVisible(false);

        // Ensure gameThread is stopped before restarting, or handle appropriately
        if (gameThread != null) {
            gameThread = null; // Signal thread to stop
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
            gameInventory.requestFocusInWindow(); // Give focus to inventory for hotbar input
        } else {
            this.requestFocusInWindow(); // Return focus to game loop
        }
    }

    // ✅ Create test enemies
    private void spawnEnemies() {
        enemies.add(new Enemy(400, 300, map)); // Pass map reference
        enemies.add(new Enemy(600, 200, map)); // Pass map reference
        enemies.add(new Enemy(200, 400, map)); // Pass map reference
    }

    @Override
    public void addNotify() {
        super.addNotify();
        // Removed requestFocusInWindow here to allow Main class to manage focus
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        double drawInterval = 1000000000.0 / 60.0; // 60 FPS
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
            // If inventory is open, pause game updates
            return;
        }

        float deltaTime = 1.0f / 60.0f;

    	// Check collisions between SlashAttacks and enemies
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

    	// Check collisions between SkillWAttacks and enemies
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

        // Update player
        player.update(deltaTime); 

        // Check if player is dead
        if (!player.isAlive() && player.isDeathAnimationFinished()) {
            gameThread = null; // Stop the game loop
            // Capture the current screen as a BufferedImage
            BufferedImage screenshot = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = screenshot.createGraphics();
            paintComponent(g2d); // Render the current game state to the screenshot
            g2d.dispose();
            
            gameOverCallback.onGameOver(screenshot); // Trigger game over screen with screenshot
            return; // Skip further updates
        }

        // Update enemies to follow the player
        for (Enemy enemy : enemies) {
            enemy.update(player.getX(), player.getY(), player);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // Call super.paintComponent for JLayeredPane
        Graphics2D g2d = (Graphics2D) g;

        // Only draw game world if inventory is not open (or draw behind it)
        // If inventory is a JInternalFrame, it will manage its own painting over the background.
        // For a simple JPanel, we draw the game world first.

        // Calculate camera position to center on the player
        int cameraX = (int) player.px - WIDTH / 2; // Use player.px
        int cameraY = (int) player.py - HEIGHT / 2; // Use player.py

        // Clamp camera to map boundaries
        cameraX = Math.max(0, Math.min(cameraX, map.getMapWidth() - WIDTH));
        cameraY = Math.max(0, Math.min(cameraY, map.getMapHeight() - HEIGHT));

        // Render the map
        map.render(g2d, cameraX, cameraY, WIDTH, HEIGHT);

        // Adjust player's draw position based on camera
        int playerScreenX = (int) player.px - cameraX; // Use player.px
        int playerScreenY = (int) player.py - cameraY; // Use player.py

        // Draw player
        player.draw(g, playerScreenX, playerScreenY);

        // ✅ Draw enemies
        for (Enemy enemy : enemies) {
            int enemyScreenX = enemy.getX() - cameraX;
            int enemyScreenY = enemy.getY() - cameraY;
            enemy.draw(g, enemyScreenX, enemyScreenY);
        }

        // === SKILL ANIMATIONS ===
        // Draw Slash Q skill attacks
        for (SlashAttack s : player.getSlashes()) {
            int slashScreenX = s.x - cameraX;
            int slashScreenY = s.y - cameraY;
            s.draw(g, slashScreenX, slashScreenY);
        }
        // Draw Skill W attacks
        for (SkillWAttack s : player.getSkillWAttacks()) {
            // Assuming SkillWAttack also needs screen coordinates
            int skillWScreenX = s.x - cameraX;
            int skillWScreenY = s.y - cameraY;
            s.draw(g, skillWScreenX, skillWScreenY);
        }

        // Draw hotbar
        hotbar.draw(g2d);
        // Do not dispose g2d here as JLayeredPane might manage its own children's painting.
        // The dispose will be called automatically by the Swing system.
    }

    // Define a functional interface for the game over callback
    public interface GameOverCallback {
        void onGameOver(BufferedImage screenshot);
    }
}
