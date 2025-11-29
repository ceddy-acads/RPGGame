package maps;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Collision {
    private BufferedImage mask;

    public Collision(BufferedImage mask) {
        this.mask = mask;
    }

    public Collision() {
    }

    public void loadCollisionMask(String mapName) {
        this.mask = MapLoader.loadCollisionMask(mapName);
        if (this.mask != null) {
            System.out.println("Loaded collision mask for: " + mapName);
        } else {
            System.err.println("Failed to load collision mask for: " + mapName);
        }
    }

    public boolean isBlocked(int x, int y, int w, int h) {
        if (mask == null) return false;

        int mapW = mask.getWidth();
        int mapH = mask.getHeight();

        int checkX1 = Math.max(0, Math.min(mapW - 1, x));
        int checkY1 = Math.max(0, Math.min(mapH - 1, y));
        int checkX2 = Math.max(0, Math.min(mapW - 1, x + w - 1));
        int checkY2 = Math.max(0, Math.min(mapH - 1, y + h - 1));

        return isPixelBlocked(checkX1, checkY1) ||
               isPixelBlocked(checkX2, checkY1) ||
               isPixelBlocked(checkX1, checkY2) ||
               isPixelBlocked(checkX2, checkY2);
    }

    private boolean isPixelBlocked(int x, int y) {
        if (x < 0 || x >= mask.getWidth() || y < 0 || y >= mask.getHeight()) {
            return true;
        }
        int rgb = mask.getRGB(x, y);
        Color c = new Color(rgb, true);
        int brightness = c.getRed() + c.getGreen() + c.getBlue();
        return brightness < 100;
    }
}
