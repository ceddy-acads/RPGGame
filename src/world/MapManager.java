package world;

import java.awt.*;
import javax.swing.ImageIcon;

public class MapManager {
    private int[][] mapData = {
       
    };

    private Image grass = new ImageIcon("assets/tiles/grass.png").getImage();
    private Image wall = new ImageIcon("assets/tiles/wall.png").getImage();
    private final int TILE_SIZE = 64;

    public void draw(Graphics g) {
        for (int row = 0; row < mapData.length; row++) {
            for (int col = 0; col < mapData[0].length; col++) {
                int tile = mapData[row][col];
                Image img = (tile == 1) ? wall : grass;
                g.drawImage(img, col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE, null);
            }
        }
    }

    public boolean isWall(int col, int row) {
        return mapData[row][col] == 1;
    }

    public int getTileSize() { return TILE_SIZE; }
}
