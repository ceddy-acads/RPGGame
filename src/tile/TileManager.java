package tile;

import java.awt.Graphics2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.imageio.ImageIO;

public class TileManager {
    private Tile[] tile;
    private int tileSize = 48; // Default tile size
    private int mapWidth = 37; // Expanded map width (1776 pixels)
    private int mapHeight = 32; // Expanded map height (1536 pixels)
    private int[][] tileMap; // 2D array to store tile IDs

    public TileManager(Object gameLoop) {
        tile = new Tile[50]; // Support up to 50 tile types
        loadTileConfig(); // Load tile properties from config file
        createExampleMap(); // Create a 5x5 example map
    }

    public void loadTileConfig() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                getClass().getResourceAsStream("/maps/tiles.txt")));

            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();

                // Skip comments and empty lines
                if (line.startsWith("#") || line.isEmpty()) {
                    continue;
                }

                // Parse tile configuration: tileID,imagePath,collision,name
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    int tileID = Integer.parseInt(parts[0].trim());
                    String imagePath = parts[1].trim();
                    boolean collision = Integer.parseInt(parts[2].trim()) == 1;

                    // Load tile image and properties
                    if (tileID >= 0 && tileID < tile.length) {
                        tile[tileID] = new Tile();
                        try {
                            tile[tileID].image = ImageIO.read(getClass().getResourceAsStream(imagePath));
                            tile[tileID].collision = collision;
                            System.out.println("Loaded tile " + tileID + ": " + imagePath + " (collision: " + collision + ")");
                        } catch (IOException e) {
                            System.err.println("Failed to load tile image: " + imagePath);
                        }
                    }
                }
            }

            br.close();
            System.out.println("Tile configuration loaded successfully");

        } catch (Exception e) {
            System.err.println("Failed to load tile configuration");
            e.printStackTrace();
            // Fallback: load default tiles
            loadDefaultTiles();
        }
    }

    private void loadDefaultTiles() {
        System.out.println("Loading default tile configuration");
        try {
            // Tile 0: Grass (walkable)
            tile[0] = new Tile();
            tile[0].image = ImageIO.read(getClass().getResourceAsStream("/assets/tiles/grass00.png"));
            tile[0].collision = false;

            // Tile 1: Wall (solid)
            tile[1] = new Tile();
            tile[1].image = ImageIO.read(getClass().getResourceAsStream("/assets/tiles/wall.png"));
            tile[1].collision = true;

            // Tile 2: Water (solid)
            tile[2] = new Tile();
            tile[2].image = ImageIO.read(getClass().getResourceAsStream("/assets/tiles/water01.png"));
            tile[2].collision = true;

            // Tile 3: earth (walkable)
            tile[3] = new Tile();
            tile[3].image = ImageIO.read(getClass().getResourceAsStream("/assets/tiles/earth.png"));
            tile[3].collision = false;
                        
            // Tile 4: floor (walkable)
            tile[4] = new Tile();
               tile[4].image = ImageIO.read(getClass().getResourceAsStream("/assets/tiles/floor01.png"));
             tile[4].collision = false;
            
            // Tile 5: hut (solid)
            tile[5] = new Tile();
            tile[5].image = ImageIO.read(getClass().getResourceAsStream("/assets/tiles/hut.png"));
            tile[5].collision = true;
        } catch (IOException e) {
            System.err.println("Failed to load default tiles");
        }
    }

    public void createExampleMap() {
        // Initialize the 5x5 tile map
        tileMap = new int[mapHeight][mapWidth];

        // Load map from text file
        loadMapFromFile("world01");
    }

    public void loadMapFromFile(String mapName) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                getClass().getResourceAsStream("/maps/" + mapName + ".txt")));

            String line;
            int row = 0;

            while ((line = br.readLine()) != null && row < mapHeight) {
                // Split by comma and parse tile IDs
                String[] tileNumbers = line.split(",");
                for (int col = 0; col < tileNumbers.length && col < mapWidth; col++) {
                    tileMap[row][col] = Integer.parseInt(tileNumbers[col].trim());
                }
                row++;
            }

            br.close();
            System.out.println("Loaded map: " + mapName + " (" + mapWidth + "x" + mapHeight + ")");

        } catch (Exception e) {
            System.err.println("Failed to load map from file: " + mapName);
            e.printStackTrace();
            // Fallback: create a simple default map
            createDefaultMap();
        }
    }

    private void createDefaultMap() {
        // Fallback map if file loading fails
        System.out.println("Creating default fallback map");
        for (int row = 0; row < mapHeight; row++) {
            for (int col = 0; col < mapWidth; col++) {
                if (row == 0 || row == mapHeight - 1 || col == 0 || col == mapWidth - 1) {
                    tileMap[row][col] = 1; // Walls on borders
                } else {
                    tileMap[row][col] = 0; // Grass inside
                }
            }
        }
    }

    public void draw(Graphics2D g2, int cameraX, int cameraY, int screenWidth, int screenHeight) {
        // Draw only visible tiles within the screen bounds
        int startCol = Math.max(0, cameraX / tileSize);
        int startRow = Math.max(0, cameraY / tileSize);
        int endCol = Math.min(mapWidth, (cameraX + screenWidth) / tileSize + 1);
        int endRow = Math.min(mapHeight, (cameraY + screenHeight) / tileSize + 1);

        // Draw the visible tiles
        for (int row = startRow; row < endRow; row++) {
            for (int col = startCol; col < endCol; col++) {
                int tileIndex = tileMap[row][col];

                // Only draw if the tile exists and has an image
                if (tile[tileIndex] != null && tile[tileIndex].image != null) {
                    int worldX = col * tileSize;
                    int worldY = row * tileSize;
                    int screenX = worldX - cameraX;
                    int screenY = worldY - cameraY;
                    g2.drawImage(tile[tileIndex].image, screenX, screenY, tileSize, tileSize, null);
                }
            }
        }
    }

    public boolean isTileSolid(int tileIndex) {
        if (tileIndex >= 0 && tileIndex < tile.length && tile[tileIndex] != null) {
            return tile[tileIndex].collision;
        }
        return false;
    }

    public int getTileSize() {
        return tileSize;
    }

    public int getMapWidth() {
        return mapWidth;
    }

    public int getMapHeight() {
        return mapHeight;
    }

    // Method to check if a position is walkable (for collision detection)
    public boolean isWalkable(int x, int y, int width, int height) {
        // Convert world coordinates to tile coordinates
        int tileX1 = x / tileSize;
        int tileY1 = y / tileSize;
        int tileX2 = (x + width - 1) / tileSize;
        int tileY2 = (y + height - 1) / tileSize;

        // Check all tiles that the entity occupies
        for (int tileY = tileY1; tileY <= tileY2; tileY++) {
            for (int tileX = tileX1; tileX <= tileX2; tileX++) {
                if (tileX >= 0 && tileX < mapWidth && tileY >= 0 && tileY < mapHeight) {
                    int tileIndex = tileMap[tileY][tileX];
                    if (isTileSolid(tileIndex)) {
                        return false; // Solid tile found
                    }
                } else {
                    return false; // Out of bounds is solid
                }
            }
        }
        return true; // All tiles are walkable
    }
}
