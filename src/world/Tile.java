package world;

import java.awt.image.BufferedImage;

public class Tile {
    public BufferedImage image;
    public boolean collision = false;
    public int type; // Added to differentiate tile types for map generation

    public Tile(BufferedImage image, boolean collision, int type) {
        this.image = image;
        this.collision = collision;
        this.type = type;
    }
}
