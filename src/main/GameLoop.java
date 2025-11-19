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
import entities.inventory; // Corrected import for the inventory class
import entities.Hotbar;
import maps.Map;
import maps.MapLoader;

public class GameLoop extends JLayeredPane implements Runnable { // Changed to extend JLayeredPane
	

    final int WIDTH = 800;
    final int HEIGHT = 600;
    final int TILE_SIZE = 48; // Consistent tile size

    private boolean inventoryOpen = false; // To track inventory state
    private inventory gameInventory; // The inventory panel

    private Thread gameThread;
    private KeyHandler keyH;
    private Player player;
    private ArrayList<Enemy> enemies;  // ✅ Enemy list
    private Map map; // Use the updated Map object
    private Hotbar hotbar;


    public GameLoop() {

        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        // Note: JLayeredPane does not directly have a background, children paint their own
        // this.setBackground(Color.WHITE); 
        this.setDoubleBuffered(true);

        // Initialize input handler
        keyH = new KeyHandler();
        this.setFocusable(true);
        this.requestFocusInWindow();
        this.addKeyListener(keyH);

        // Load map images and create Map object
        BufferedImage mapImage = MapLoader.loadMapImage("forest"); // Assuming "forest" is the map name
        BufferedImage collisionMask = MapLoader.loadCollisionMask("forest");
        map = new Map(mapImage, collisionMask, TILE_SIZE);

        // Initialize player with KeyHandler and Map
        player = new Player(100, 100, keyH, map); // Initial player position (100, 100)

        // ✅ Initialize enemies
        enemies = new ArrayList<>();
        spawnEnemies();

        // Initialize inventory
        gameInventory = new inventory();

        // Initialize hotbar
        hotbar = new Hotbar(WIDTH, HEIGHT, gameInventory);
        gameInventory.setBounds(0, 0, WIDTH, HEIGHT); // Set bounds to fill the entire GameLoop panel
        gameInventory.setVisible(false); // Start invisible
        this.add(gameInventory, JLayeredPane.PALETTE_LAYER); // Add to a higher layer

        startGameThread();
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
        requestFocusInWindow();
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

    private boolean lastInventoryToggleState = false; // Track the previous state of inventoryPressed

    public void update() {
        // Toggle inventory if 'I' key is pressed and it wasn't pressed in the last frame
        if (keyH.inventoryPressed && !lastInventoryToggleState) {
            toggleInventory();
        }
        lastInventoryToggleState = keyH.inventoryPressed; // Update the last state

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
                    System.out.println("SLASH HIT! Slash at (" + slash.x + "," + slash.y + ") hit enemy at (" + enemy.getX() + "," + enemy.getY() + "), damage: " + slash.getDamage());
                    if (!slash.hasHit()) {
                        enemy.takeDamage(slash.getDamage());
                        slash.setHasHit(true);
                    }
    	        }
    	    }
    	}

        // Update player
        player.update(deltaTime); // Player now uses its internal map reference

        // ✅ Update enemies to follow the player
        for (Enemy enemy : enemies) {
           
            	    enemy.update(player.getX(), player.getY(), player);

        }
    
        // Optional: handle collision or attacks later
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
}
