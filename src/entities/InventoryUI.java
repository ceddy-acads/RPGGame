package entities;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.ImageIcon;
import java.awt.image.BufferedImage;

public class InventoryUI extends JPanel { // Changed from JFrame

    enum ItemType { EQUIPMENT, CONSUMABLE, MATERIAL }

    static class Item {
        String id;
        String name;
        ItemType type;
        String description;
        String iconPath;
        Map<String, String> stats = new HashMap<>();
        int stack = 1;

        Item(String id, String name, ItemType type, String description, String iconPath) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.description = description;
            this.iconPath = iconPath;
        }

        ImageIcon getIcon() {
            try {
                return new ImageIcon(getClass().getResource(iconPath));
            } catch (Exception e) {
                return new ImageIcon(new BufferedImage(48, 48, BufferedImage.TYPE_INT_ARGB));
            }
        }
    }

    static class Slot {
        Item item = null;
        int amount = 0;
    }

    private final int ROWS = 5;
    private final int COLS = 4;

    private java.util.List<Slot> inventorySlots = new ArrayList<>();
    private Map<String, Slot> equipmentSlots = new LinkedHashMap<>();

    private Slot selectedSlot = null;

    private JPanel gridPanel = new JPanel(new GridLayout(ROWS, COLS, 5, 5));
    private JPanel equipPanel = new JPanel();
    private JTextArea detailArea = new JTextArea();
    private JButton btnEquip = new JButton("EQUIP");
    private JButton btnUse = new JButton("USE");
    private JButton btnDrop = new JButton("DROP");

    private java.util.List<Item> allItems = new ArrayList<>();

    // --- color palette (medieval parchment)
    private final Color PARCHMENT = new Color(217,195,154);
    private final Color PARCHMENT_DARK = new Color(200,175,130);
    private final Color SLOT_BG = new Color(140,111,69);
    private final Color SLOT_HOVER = new Color(160,130,85);
    private final Color SLOT_BORDER = new Color(58,46,30);
    private final Color SLOT_HIGHLIGHT = new Color(216,179,122);
    private final Color TEXT_BROWN = new Color(59,47,35);
    private final Color PANEL_BORDER = new Color(91,74,48);
    private final Color BUTTON_BROWN = new Color(152,117,78);
    private final Color BUTTON_DROP = new Color(168,92,61);
    private final Color SELECT_BORDER = new Color(206,160,98);

    // Modified constructor to match GameLoop's expectation
    public InventoryUI(int screenWidth, int screenHeight) {
        // Removed JFrame specific calls
        setPreferredSize(new Dimension(screenWidth, screenHeight)); // Set preferred size
        setBackground(PARCHMENT); // Set background for the JPanel
        setLayout(new BorderLayout()); // Use BorderLayout for the main panel

        // --- UIManager tab colors should be set BEFORE creating JTabbedPane
        UIManager.put("TabbedPane.selected", PARCHMENT);
        UIManager.put("TabbedPane.contentAreaColor", PARCHMENT_DARK);
        UIManager.put("TabbedPane.unselectedBackground", new Color(160,135,100));
        UIManager.put("TabbedPane.foreground", TEXT_BROWN);

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

        // JPanel root = new JPanel(new BorderLayout()); // No longer need a root panel, this JPanel is the root
        // add(root);

        // left column
        JPanel leftCol = new JPanel();
        leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.Y_AXIS));
        leftCol.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        leftCol.setPreferredSize(new Dimension(300, 600));

        // style main panels with parchment colors
        // root.setBackground(PARCHMENT); // Set background for this JPanel instead
        leftCol.setBackground(PARCHMENT);
        gridPanel.setBackground(PARCHMENT_DARK);
        equipPanel.setBackground(PARCHMENT_DARK);

        equipPanel.setLayout(new BoxLayout(equipPanel, BoxLayout.Y_AXIS));
        equipPanel.setBorder(BorderFactory.createTitledBorder(new LineBorder(PANEL_BORDER, 2), "Equipment", 0, 0, new Font("Serif", Font.BOLD, 12), TEXT_BROWN));
        refreshEquipmentPanel();

        detailArea.setEditable(false);
        detailArea.setLineWrap(true);
        detailArea.setWrapStyleWord(true);
        detailArea.setPreferredSize(new Dimension(280, 150));
        detailArea.setBorder(BorderFactory.createTitledBorder(new LineBorder(PANEL_BORDER, 2), "Details", 0, 0, new Font("Serif", Font.BOLD, 12), TEXT_BROWN));
        detailArea.setBackground(PARCHMENT);
        detailArea.setForeground(TEXT_BROWN);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 5, 5));
        buttonPanel.setBackground(PARCHMENT);
        // style buttons
        styleButton(btnEquip, BUTTON_BROWN);
        styleButton(btnUse, new Color(155,134,78));
        styleButton(btnDrop, BUTTON_DROP);
        buttonPanel.add(btnEquip);
        buttonPanel.add(btnUse);
        buttonPanel.add(btnDrop);

        leftCol.add(equipPanel);
        leftCol.add(Box.createVerticalStrut(10));
        leftCol.add(detailArea);
        leftCol.add(Box.createVerticalStrut(10));
        leftCol.add(buttonPanel);

        // Create tabs (gridPanel placed inside)
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(PARCHMENT);
        tabs.addTab("Equipment", new JScrollPane(gridPanel));
        tabs.addTab("Consumables", new JScrollPane(gridPanel));
        tabs.addTab("Materials", new JScrollPane(gridPanel));

        add(leftCol, BorderLayout.WEST); // Add to this JPanel
        add(tabs, BorderLayout.CENTER);   // Add to this JPanel

        refreshGrid();

        btnEquip.addActionListener(e -> equipItem());
        btnUse.addActionListener(e -> useItem());
        btnDrop.addActionListener(e -> dropItem());
    }

    private void styleButton(JButton b, Color bg) {
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(new LineBorder(PANEL_BORDER, 2));
    }

    private void refreshGrid() {
        gridPanel.removeAll();
        for (Slot slot : inventorySlots) {
            gridPanel.add(makeSlotComponent(slot));
        }
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private void refreshEquipmentPanel() {
        equipPanel.removeAll();
        for (String name : equipmentSlots.keySet()) {
            Slot slot = equipmentSlots.get(name);
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
            row.setBackground(PARCHMENT_DARK);
            row.setBorder(new LineBorder(PANEL_BORDER, 2));
            row.setMaximumSize(new Dimension(260, 50));
            row.add(new JLabel(name + ":"));
            JLabel icon = new JLabel();
            icon.setPreferredSize(new Dimension(40, 40));
            if (slot.item != null)
                icon.setIcon(slot.item.getIcon());
            row.add(icon);

            row.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    selectedSlot = slot;
                    refreshGrid(); // to repaint selection on grid slots (and equipment visual if you wanted)
                    updateDetail();
                }
                public void mouseEntered(MouseEvent e) {
                    row.setBackground(new Color(210,185,140));
                }
                public void mouseExited(MouseEvent e) {
                    row.setBackground(PARCHMENT_DARK);
                }
            });

            equipPanel.add(row);
        }
        equipPanel.revalidate();
        equipPanel.repaint();
    }

    private JPanel makeSlotComponent(Slot slot) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(SLOT_BG);
        panel.setPreferredSize(new Dimension(80, 80));

        // border: show selected border if this is selectedSlot
        if (slot == selectedSlot) {
            panel.setBorder(new LineBorder(SELECT_BORDER, 4));
        } else {
            panel.setBorder(new LineBorder(SLOT_BORDER, 3));
        }

        JLabel img = new JLabel();
        img.setHorizontalAlignment(SwingConstants.CENTER);

        if (slot.item != null)
            img.setIcon(slot.item.getIcon());

        panel.add(img, BorderLayout.CENTER);

        if (slot.amount > 1) {
            JLabel count = new JLabel(String.valueOf(slot.amount));
            count.setHorizontalAlignment(SwingConstants.CENTER);
            count.setOpaque(false);
            count.setForeground(Color.WHITE);
            panel.add(count, BorderLayout.SOUTH);
        }

        panel.addMouseListener(new MouseAdapter() {
            Color original = panel.getBackground();

            public void mouseClicked(MouseEvent e) {
                selectedSlot = slot;
                updateDetail();
                refreshGrid(); // redraw borders to show selection
                refreshEquipmentPanel();
            }

            public void mouseEntered(MouseEvent e) {
                panel.setBackground(SLOT_HOVER);
            }

            public void mouseExited(MouseEvent e) {
                panel.setBackground(original);
            }
        });

        // The TransferHandler and DropTarget setup needs to be compatible with how items are dragged and dropped.
        // This might need further adjustments depending on the game's drag-and-drop implementation.
        // For now, I'll keep them as they are from the original inventory.java.
        panel.setTransferHandler(new TransferHandler("item") {
            @Override
            public int getSourceActions(JComponent c) {
                return COPY_OR_MOVE;
            }

            @Override
            protected Transferable createTransferable(JComponent c) {
                if (selectedSlot != null && selectedSlot.item != null) {
                    return new StringSelection(selectedSlot.item.id);
                }
                return null;
            }
        });


        panel.setDropTarget(new DropTarget() {
            public synchronized void drop(DropTargetDropEvent dtde) {
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_MOVE);
                    Object dropped = dtde.getTransferable().getTransferData(DataFlavor.stringFlavor);
                    swapItems(slot, (String)dropped);
                    refreshGrid();
                    refreshEquipmentPanel();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        return panel;
    }

    private void swapItems(Slot target, String itemId) {
        Slot source = findSlotByItemId(itemId);
        if (source == null) return;

        Item tmp = target.item;
        int tmpAmt = target.amount;

        target.item = source.item;
        target.amount = source.amount;

        source.item = tmp;
        source.amount = tmpAmt;
    }

    private Slot findSlotByItemId(String id) {
        for (Slot s : inventorySlots)
            if (s.item != null && s.item.id.equals(id))
                return s;

        for (Slot s : equipmentSlots.values())
            if (s.item != null && s.item.id.equals(id))
                return s;

        return null;
    }

    public void reset() {
        // This method was added in the previous fix and needs to be implemented.
        // For this new inventory UI, we can reset selected slot and refresh panels.
        selectedSlot = null;
        updateDetail(); // Clear details
        refreshGrid();
        refreshEquipmentPanel();
        // You might also want to clear inventory/equipment contents here if 'reset' means starting fresh.
        // For now, assume it just resets the UI state.
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
        detailArea.append(it.name + "\n");
        detailArea.append(it.type + "\n\n");
        detailArea.append(it.description + "\n\n");

        for (var e : it.stats.entrySet()) {
            detailArea.append(e.getKey() + ": " + e.getValue() + "\n");
        }

        btnEquip.setEnabled(it.type == ItemType.EQUIPMENT);
        btnUse.setEnabled(it.type == ItemType.CONSUMABLE);
        btnDrop.setEnabled(true);
    }

    private void equipItem() {
        if (selectedSlot == null || selectedSlot.item == null) return;
        Item it = selectedSlot.item;

        String target = "Weapon"; // Default target
        if (it.name.toLowerCase().contains("ring")) target = "Ring";
        else if (it.type == ItemType.EQUIPMENT) {
            // Need a more robust way to determine head/chest slots
            // For now, just allow "Weapon" and "Ring" from example
            // If it's a generic equipment and not a ring/weapon, where should it go?
            // This logic needs to be expanded based on actual item types and equipment slots.
            System.out.println("Cannot equip item of type " + it.name + " to a specific slot yet.");
            return;
        } else {
             System.out.println("Cannot equip non-equipment item.");
             return;
        }


        Slot eq = equipmentSlots.get(target);
        if (eq == null) {
            System.out.println("Equipment slot " + target + " not found.");
            return;
        }

        Item old = eq.item;
        int oldAmt = eq.amount;

        eq.item = it;
        eq.amount = selectedSlot.amount;

        selectedSlot.item = old;
        selectedSlot.amount = oldAmt;

        refreshGrid();
        refreshEquipmentPanel();
        updateDetail();
    }

    private void useItem() {
        if (selectedSlot == null || selectedSlot.item == null) return;
        if (selectedSlot.item.type != ItemType.CONSUMABLE) return;

        selectedSlot.amount--;
        if (selectedSlot.amount <= 0) selectedSlot.item = null;

        refreshGrid();
        updateDetail();
    }

    private void dropItem() {
        if (selectedSlot == null) return;
        selectedSlot.item = null;
        selectedSlot.amount = 0;

        refreshGrid();
        refreshEquipmentPanel();
        updateDetail();
    }

    private void loadSampleItems() {
        Item sword = new Item("sword", "Short Sword", ItemType.EQUIPMENT, "A basic sword", "/icons/sword.png");
        sword.stats.put("Damage", "6–10");

        Item flame = new Item("flamebrand", "Flamebrand", ItemType.EQUIPMENT, "Adds fire damage over time.", "/icons/flame_sword.png");
        flame.stats.put("Damage", "34–52");

        Item potion = new Item("potion_red", "Health Potion", ItemType.CONSUMABLE, "Restores health", "/icons/potion_red.png");
        potion.stack = 10;

        Item ring = new Item("ring_green", "Emerald Ring", ItemType.EQUIPMENT, "A shiny ring", "/icons/ring.png");

        allItems.addAll(Arrays.asList(sword, flame, potion, ring));
    }

    private Item cloneItem(String id) {
        for (Item it : allItems)
            if (it.id.equals(id)) {
                Item c = new Item(it.id, it.name, it.type, it.description, it.iconPath);
                c.stats.putAll(it.stats);
                c.stack = it.stack;
                return c;
            }
        return null;
    }

    private void addItemToInventory(Item item) { addItemToInventory(item, 1); }

    private void addItemToInventory(Item item, int amt) {
        for (Slot s : inventorySlots)
            if (s.item == null) {
                s.item = item;
                s.amount = amt;
                return;
            }
    }

    // Public getter for inventorySlots, required by Hotbar
    public java.util.List<Slot> getInventorySlots() {
        return inventorySlots;
    }
}
