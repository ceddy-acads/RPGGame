package entities;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import javax.imageio.ImageIO;
import java.io.IOException;

public class InventoryUI extends JPanel {

    enum ItemType { EQUIPMENT, CONSUMABLE, MATERIAL }

    static class Item {
        String id;
        String name;
        ItemType type;
        String description;
        String iconPath;
        Map<String, String> stats = new HashMap<>();
        int stack = 1;
        private Image icon; // Cached image

        Item(String id, String name, ItemType type, String description, String iconPath) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.description = description;
            this.iconPath = iconPath;
        }

        Image getIcon() {
            if (icon == null) {
                try {
                    icon = ImageIO.read(getClass().getResourceAsStream(iconPath));
                } catch (IOException | IllegalArgumentException e) {
                    System.err.println("Failed to load item icon: " + iconPath + " - " + e.getMessage());
                    // Return a placeholder image or null
                    return null;
                }
            }
            return icon;
        }
    }

    static class Slot {
        Item item = null;
        int amount = 0;
    }

    private final int ROWS = 5;
    private final int COLS = 4;
    private final int SLOT_SIZE = 48;

    private List<Slot> inventorySlots = new ArrayList<>();
    private Map<String, Slot> equipmentSlots = new LinkedHashMap<>();

    private Slot selectedSlot = null;
    
    private JPanel gridPanel = new JPanel();
    private JPanel equipPanel = new JPanel();
    private JTextArea detailArea = new JTextArea();
    private JButton btnEquip = new JButton("EQUIP");
    private JButton btnUse = new JButton("USE");
    private JButton btnDrop = new JButton("DROP");

    private List<Item> allItems = new ArrayList<>();
    private Player player;

    public InventoryUI(Player player) {
        this.player = player;
        setPreferredSize(new Dimension(800, 600));
        setBackground(new Color(0, 0, 0, 180)); // Semi-transparent black
        setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        loadSampleItems();

        for (int i = 0; i < ROWS * COLS; i++) {
            inventorySlots.add(new Slot());
        }

        equipmentSlots.put("Head", new Slot());
        equipmentSlots.put("Chest", new Slot());
        equipmentSlots.put("Weapon", new Slot());
        equipmentSlots.put("Ring", new Slot());

        addItemToInventory(cloneItem("flamebrand"));
        addItemToInventory(cloneItem("sword"));
        addItemToInventory(cloneItem("potion_red"), 3);
        addItemToInventory(cloneItem("ring_green"));

        // Left Column (Equipment, Details, Buttons)
        JPanel leftCol = new JPanel();
        leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.Y_AXIS));
        leftCol.setBorder(new EmptyBorder(10, 10, 10, 10));
        leftCol.setPreferredSize(new Dimension(250, 0));
        leftCol.setOpaque(false);

        JLabel equipLabel = new JLabel("Equipment");
        equipLabel.setFont(new Font("Serif", Font.BOLD, 20));
        equipLabel.setForeground(Color.WHITE);
        equipLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        equipPanel.setLayout(new BoxLayout(equipPanel, BoxLayout.Y_AXIS));
        equipPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        equipPanel.setOpaque(false);

        detailArea.setEditable(false);
        detailArea.setLineWrap(true);
        detailArea.setWrapStyleWord(true);
        detailArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        detailArea.setBackground(new Color(85, 85, 85));
        detailArea.setForeground(Color.WHITE);
        JScrollPane detailScrollPane = new JScrollPane(detailArea);
        detailScrollPane.setPreferredSize(new Dimension(0, 150));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(btnEquip);
        buttonPanel.add(btnUse);
        buttonPanel.add(btnDrop);
        
        leftCol.add(equipLabel);
        leftCol.add(Box.createRigidArea(new Dimension(0, 5)));
        leftCol.add(equipPanel);
        leftCol.add(Box.createRigidArea(new Dimension(0, 10)));
        leftCol.add(detailScrollPane);
        leftCol.add(Box.createRigidArea(new Dimension(0, 10)));
        leftCol.add(buttonPanel);
        add(leftCol, BorderLayout.WEST);

        // Right Content (Inventory Grid)
        gridPanel.setLayout(new GridLayout(ROWS, COLS, 5, 5));
        gridPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        gridPanel.setOpaque(false);
        add(gridPanel, BorderLayout.CENTER);

        refreshGrid();
        refreshEquipmentPanel();

        btnEquip.addActionListener(e -> equipItem());
        btnUse.addActionListener(e -> useItem());
        btnDrop.addActionListener(e -> dropItem());

        setVisible(false); // Initially hidden
    }

    public void refreshGrid() {
        gridPanel.removeAll();
        for (Slot slot : inventorySlots) {
            gridPanel.add(makeSlotComponent(slot));
        }
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    public void refreshEquipmentPanel() {
        equipPanel.removeAll();
        for (Map.Entry<String, Slot> entry : equipmentSlots.entrySet()) {
            String name = entry.getKey();
            Slot slot = entry.getValue();

            JPanel rowLayout = new JPanel(new BorderLayout(5, 0));
            rowLayout.setAlignmentX(Component.LEFT_ALIGNMENT);
            rowLayout.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
            rowLayout.setOpaque(false);
            
            JLabel nameLabel = new JLabel(name + ":");
            nameLabel.setForeground(Color.WHITE);
            nameLabel.setFont(new Font("Serif", Font.BOLD, 14));
            
            JLabel iconLabel = new JLabel();
            iconLabel.setPreferredSize(new Dimension(SLOT_SIZE, SLOT_SIZE));
            if (slot.item != null && slot.item.getIcon() != null) {
                iconLabel.setIcon(new ImageIcon(slot.item.getIcon().getScaledInstance(SLOT_SIZE, SLOT_SIZE, Image.SCALE_SMOOTH)));
            }

            rowLayout.add(nameLabel, BorderLayout.WEST);
            rowLayout.add(iconLabel, BorderLayout.CENTER);
            equipPanel.add(rowLayout);

            rowLayout.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    selectedSlot = slot;
                    updateDetail();
                }
            });
        }
        equipPanel.revalidate();
        equipPanel.repaint();
    }
    
    private JComponent makeSlotComponent(Slot slot) {
        // Using a JLabel as a container. Could also use JLayeredPane for more complex rendering.
        JLabel slotLabel = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Background
                g.setColor(new Color(85, 85, 85));
                g.fillRect(0, 0, getWidth(), getHeight());
                
                // Border
                g.setColor(Color.DARK_GRAY);
                g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);

                // Item Icon
                if (slot.item != null && slot.item.getIcon() != null) {
                    Image icon = slot.item.getIcon();
                    int iconSize = SLOT_SIZE - 8;
                    g.drawImage(icon, 4, 4, iconSize, iconSize, null);
                }

                // Amount text
                if (slot.amount > 1) {
                    String amountStr = String.valueOf(slot.amount);
                    g.setColor(Color.WHITE);
                    g.setFont(new Font("Arial", Font.BOLD, 12));
                    FontMetrics fm = g.getFontMetrics();
                    int x = getWidth() - fm.stringWidth(amountStr) - 5;
                    int y = getHeight() - fm.getDescent() - 2;
                    g.drawString(amountStr, x, y);
                }
            }
        };

        slotLabel.setPreferredSize(new Dimension(SLOT_SIZE, SLOT_SIZE));
        slotLabel.setOpaque(false); // We are doing custom painting

        slotLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                selectedSlot = slot;
                updateDetail();
                // Highlight selected slot
                // This requires more complex state management, for now just updating details.
            }
        });

        return slotLabel;
    }

    private void swapItems(Slot target, Item itemToSwap) {
        // Find the source slot of the itemToSwap
        Slot source = null;
        for (Slot s : inventorySlots) {
            if (s.item != null && s.item.id.equals(itemToSwap.id)) {
                source = s;
                break;
            }
        }
        if (source == null) {
            for (Slot s : equipmentSlots.values()) {
                if (s.item != null && s.item.id.equals(itemToSwap.id)) {
                    source = s;
                    break;
                }
            }
        }

        if (source == null) return; // Item not found

        Item tmp = target.item;
        int tmpAmt = target.amount;

        target.item = source.item;
        target.amount = source.amount;

        source.item = tmp;
        source.amount = tmpAmt;
    }
    
    private void updateDetail() {
        detailArea.setText("");
        if (selectedSlot == null || selectedSlot.item == null) {
            detailArea.setText("No item selected");
            btnEquip.setEnabled(false);
            btnUse.setEnabled(false);
            btnDrop.setEnabled(false);
            return;
        }

        Item it = selectedSlot.item;
        StringBuilder sb = new StringBuilder();
        sb.append(it.name).append("\n");
        sb.append(it.type).append("\n\n");
        sb.append(it.description).append("\n\n");

        for (var e : it.stats.entrySet()) {
            sb.append(e.getKey()).append(": ").append(e.getValue()).append("\n");
        }
        detailArea.setText(sb.toString());

        btnEquip.setEnabled(it.type == ItemType.EQUIPMENT);
        btnUse.setEnabled(it.type == ItemType.CONSUMABLE);
        btnDrop.setEnabled(true);
    }

    private void equipItem() {
        if (selectedSlot == null || selectedSlot.item == null || selectedSlot.item.type != ItemType.EQUIPMENT) return;
        Item it = selectedSlot.item;

        // This is a simplified logic. A real system would check item subtypes (e.g., helmet, chestplate).
        String targetSlotName = "Weapon"; // default
        if (it.name.toLowerCase().contains("ring")) targetSlotName = "Ring";
        else if (it.name.toLowerCase().contains("sword") || it.name.toLowerCase().contains("brand")) targetSlotName = "Weapon";
        // Add more else-if for Head, Chest etc.

        Slot eqSlot = equipmentSlots.get(targetSlotName);
        if (eqSlot == null) {
            System.err.println("No equipment slot found for: " + targetSlotName);
            return;
        }
        
        // Swap items
        Item oldItem = eqSlot.item;
        int oldAmt = eqSlot.amount;

        eqSlot.item = it;
        eqSlot.amount = selectedSlot.amount;

        selectedSlot.item = oldItem;
        selectedSlot.amount = oldAmt;

        refreshGrid();
        refreshEquipmentPanel();
        updateDetail();
        updatePlayerStats();
    }

    private void useItem() {
        if (selectedSlot == null || selectedSlot.item == null || selectedSlot.item.type != ItemType.CONSUMABLE) return;

        // Add logic for item effect here (e.g., player.heal(50))
        System.out.println("Used " + selectedSlot.item.name);

        selectedSlot.amount--;
        if (selectedSlot.amount <= 0) {
            selectedSlot.item = null;
        }

        refreshGrid();
        updateDetail();
    }

    private void dropItem() {
        if (selectedSlot == null || selectedSlot.item == null) return;
        
        System.out.println("Dropped " + selectedSlot.item.name);

        selectedSlot.item = null;
        selectedSlot.amount = 0;

        refreshGrid();
        refreshEquipmentPanel(); // In case it was an equipment slot
        updateDetail();
        updatePlayerStats();
    }
    
    private void updatePlayerStats() {
        int totalAttack = 0;
        int totalDefense = 0;
        for (Slot slot : equipmentSlots.values()) {
            if (slot.item != null) {
                String damageString = slot.item.stats.getOrDefault("Damage", "0");
                if (damageString.contains("–")) {
                    damageString = damageString.split("–")[0].trim();
                }
                try {
                    totalAttack += Integer.parseInt(damageString);
                } catch (NumberFormatException e) {
                    System.err.println("Could not parse damage stat for item: " + slot.item.name);
                }
                
                String defenseString = slot.item.stats.getOrDefault("Defense", "0");
                try {
                    totalDefense += Integer.parseInt(defenseString);
                } catch (NumberFormatException e) {
                    System.err.println("Could not parse defense stat for item: " + slot.item.name);
                }
            }
        }
        player.setEquippedStats(totalAttack, totalDefense);
        System.out.println("Player stats updated. Attack: " + player.getTotalAttack() + ", Defense: " + player.getTotalDefense());
    }

    private void loadSampleItems() {
        Item sword = new Item("sword", "Short Sword", ItemType.EQUIPMENT, "A basic sword", "/assets/ui/sword.png");
        sword.stats.put("Damage", "10");

        Item flame = new Item("flamebrand", "Flamebrand", ItemType.EQUIPMENT, "Adds fire damage over time.", "/assets/ui/sword.png");
        flame.stats.put("Damage", "25");
        flame.stats.put("Defense", "5");

        Item potion = new Item("potion_red", "Health Potion", ItemType.CONSUMABLE, "Restores health", "/assets/ui/story1.png");
        potion.stack = 10;

        Item ring = new Item("ring_green", "Emerald Ring", ItemType.EQUIPMENT, "A shiny ring", "/assets/ui/clouds.png");
        ring.stats.put("Defense", "2");

        allItems.addAll(Arrays.asList(sword, flame, potion, ring));
    }

    private Item cloneItem(String id) {
        for (Item it : allItems) {
            if (it.id.equals(id)) {
                Item c = new Item(it.id, it.name, it.type, it.description, it.iconPath);
                c.stats.putAll(it.stats);
                c.stack = it.stack;
                return c;
            }
        }
        return null;
    }

    private void addItemToInventory(Item item) { addItemToInventory(item, 1); }

    private void addItemToInventory(Item item, int amt) {
        if (item == null) return;
        // Try to stack with existing items
        if (item.stack > 1) {
            for (Slot s : inventorySlots) {
                if (s.item != null && s.item.id.equals(item.id) && s.amount < s.item.stack) {
                    s.amount += amt;
                    // Handle overflow if needed
                    return;
                }
            }
        }
        // Add to new slot
        for (Slot s : inventorySlots) {
            if (s.item == null) {
                s.item = item;
                s.amount = amt;
                return;
            }
        }
    }

    public List<Slot> getInventorySlots() {
        return inventorySlots;
    }

    public void reset() {
        // Clear inventory and equipment
        for (Slot s : inventorySlots) {
            s.item = null;
            s.amount = 0;
        }
        for (Slot s : equipmentSlots.values()) {
            s.item = null;
            s.amount = 0;
        }
        // Re-add initial items
        addItemToInventory(cloneItem("flamebrand"));
        addItemToInventory(cloneItem("sword"));
        addItemToInventory(cloneItem("potion_red"), 3);
        addItemToInventory(cloneItem("ring_green"));
        
        // Reset selected slot and detail area
        selectedSlot = null;
        updateDetail(); // Update to "No item selected"
        
        // Refresh UI
        refreshGrid();
        refreshEquipmentPanel();
        updatePlayerStats(); // Ensure player stats are reset to base or equipped from reset inventory
    }
}
