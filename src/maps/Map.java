package maps;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Map {
    private BufferedImage mapImage;
    private int mapWidth;
    private int mapHeight;
    private int tileSize;
    private Collision collision;

    public Map(BufferedImage mapImage, BufferedImage collisionMask, int tileSize) {
        this.mapImage = mapImage;
        this.tileSize = tileSize;
        this.collision = new Collision(collisionMask);

        if (mapImage != null) {
            this.mapWidth = mapImage.getWidth();
            this.mapHeight = mapImage.getHeight();
        } else {
            System.err.println("Map image is null, creating fallback map dimensions.");
            this.mapWidth = 20 * tileSize;
            this.mapHeight = 15 * tileSize;
        }
    }

    public void render(Graphics2D g2d, int cameraX, int cameraY, int screenWidth, int screenHeight) {
        if (mapImage != null) {
            cameraX = Math.max(0, Math.min(cameraX, mapWidth - screenWidth));
            cameraY = Math.max(0, Math.min(cameraY, mapHeight - screenHeight));

            g2d.drawImage(mapImage,
                         0, 0, screenWidth, screenHeight,
                         cameraX, cameraY, cameraX + screenWidth, cameraY + screenHeight,
                         null);
        } else {
            g2d.setColor(new Color(34, 139, 34));
            g2d.fillRect(0, 0, screenWidth, screenHeight);
        }
    }

    public boolean isWalkable(int x, int y, int width, int height) {
        if (collision != null) {
            return !collision.isBlocked(x, y, width, height);
        }
        return true;
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
}
