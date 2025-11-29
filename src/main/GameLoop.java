package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*; // Import AWT event classes for KeyAdapter and KeyEvent
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import input.KeyHandler;
import entities.Player;
import entities.SlashAttack;
import entities.SkillWAttack;
import entities.InventoryUI;
import entities.Hotbar;
import tile.TileManager;

public class GameLoop extends JLayeredPane implements Runnable { 
	
    final int WIDTH = 800;
    final int HEIGHT = 600;
    final int TILE_SIZE = 48; // Consistent tile size

    private boolean inventoryOpen = false; // To track inventory state
    private InventoryUI gameInventory; // The inventory panel

    private Thread gameThread;
    private KeyHandler keyH;
    private Player player;
    private TileManager tileM; // Tile manager for rendering tiles
    private Hotbar hotbar;
    private GameOverCallback gameOverCallback; // Callback for game over

    public GameLoop(GameOverCallback gameOverCallback) { // Modified constructor
        this.gameOverCallback = gameOverCallback;

        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setBackground(Color.BLACK); // Set background to black to remove white
        this.setDoubleBuffered(true);

        keyH = new KeyHandler();
        this.setFocusable(true);
        this.requestFocusInWindow();
        this.addKeyListener(keyH);

        setupKeyBindings();

        // Initialize TileManager first
        tileM = new TileManager(this);

        // Initialize player with KeyHandler (start in walkable grass area)
        player = new Player(336, 336, keyH); // Position (336,336) = center of open grass area away from obstacles
        player.setTileManager(tileM); // Pass TileManager reference for collision

        // Initialize inventory
        gameInventory = new InventoryUI(WIDTH, HEIGHT);

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
        player = new Player(336, 336, keyH); // Re-initialize player at open grass area, full HP
        player.setTileManager(tileM); // Re-set TileManager reference

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

        // Update player
        player.update(deltaTime);

        // Handle freeze skill
        Rectangle freezeArea = player.getFreezeArea();
        if (freezeArea != null) {
            player.clearFreezeArea();
        }

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

        // Clamp camera to map bounds to prevent white background
        int mapPixelWidth = tileM.getMapWidth() * TILE_SIZE;
        int mapPixelHeight = tileM.getMapHeight() * TILE_SIZE;
        cameraX = Math.max(0, Math.min(cameraX, mapPixelWidth - WIDTH));
        cameraY = Math.max(0, Math.min(cameraY, mapPixelHeight - HEIGHT));

        // Draw tiles using TileManager with camera offset
        tileM.draw(g2d, cameraX, cameraY, WIDTH, HEIGHT);

        // Adjust player's draw position based on camera
        int playerScreenX = (int) player.px - cameraX; // Use player.px
        int playerScreenY = (int) player.py - cameraY; // Use player.py

        // Draw player
        player.draw(g, playerScreenX, playerScreenY);

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
        drawHotbarKeys(g2d);
        // Do not dispose g2d here as JLayeredPane might manage its own children's painting.
        // The dispose will be called automatically by the Swing system.
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

    // Define a functional interface for the game over callback
    public interface GameOverCallback {
        void onGameOver(BufferedImage screenshot);
    }
}
