package entities;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

public class Hotbar {

    private final int screenWidth;
    private final int screenHeight;
    private final inventory inventory;
    private final int slotSize = 48;
    private final int numSlots = 5;
    private final int hotbarWidth = numSlots * slotSize;
    private final int hotbarHeight = slotSize;
    private final int hotbarX;
    private final int hotbarY;

    public Hotbar(int screenWidth, int screenHeight, inventory inventory) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.inventory = inventory;
        this.hotbarX = (screenWidth - hotbarWidth) / 2;
        this.hotbarY = screenHeight - hotbarHeight - 10;
    }

    public void draw(Graphics2D g2d) {
        // Draw hotbar background
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRect(hotbarX, hotbarY, hotbarWidth, hotbarHeight);

        // Draw hotbar slots
        g2d.setColor(Color.GRAY);
        for (int i = 0; i < numSlots; i++) {
            g2d.drawRect(hotbarX + i * slotSize, hotbarY, slotSize, slotSize);
        }

        // Draw items in hotbar
        List<inventory.Slot> slots = inventory.getInventorySlots();
        for (int i = 0; i < numSlots && i < slots.size(); i++) {
            inventory.Slot slot = slots.get(i);
            if (slot.item != null) {
                g2d.drawImage(slot.item.getIcon().getImage(), hotbarX + i * slotSize, hotbarY, slotSize, slotSize, null);
            }
        }
    }
}
