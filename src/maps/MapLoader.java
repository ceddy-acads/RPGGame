package maps;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class MapLoader {
    public static BufferedImage loadMapImage(String mapName) {
        try {
            String path = "/assets/tiles/" + mapName.toLowerCase() + ".png";
            BufferedImage image = ImageIO.read(MapLoader.class.getResourceAsStream(path));
            if (image == null) {
                System.err.println("Failed to load map image: " + path);
            }
            return image;
        } catch (IOException e) {
            System.err.println("Error loading map image " + mapName + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static BufferedImage loadCollisionMask(String mapName) {
        try {
            String path = "/assets/tiles/" + mapName.toLowerCase() + "_collision.png";
            BufferedImage mask = ImageIO.read(MapLoader.class.getResourceAsStream(path));
            if (mask == null) {
                System.err.println("Failed to load collision mask: " + path);
            }
            return mask;
        } catch (IOException e) {
            System.err.println("Error loading collision mask " + mapName + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
