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

public class inventory extends JPanel {

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

    public inventory() {
        // JPanel setup
        setLayout(new BorderLayout()); // Set a layout for the JPanel
        setPreferredSize(new Dimension(900, 650)); // Set preferred size for the panel
        setBorder(BorderFactory.createLineBorder(Color.WHITE, 2)); // Add a border for visibility
        setBackground(new Color(0, 0, 0, 150)); // Semi-transparent black background

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

       
        JPanel root = new JPanel(new BorderLayout());
        add(root);

        
        JPanel leftCol = new JPanel();
        leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.Y_AXIS));
        leftCol.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        leftCol.setPreferredSize(new Dimension(300, 600));

        equipPanel.setLayout(new BoxLayout(equipPanel, BoxLayout.Y_AXIS));
        equipPanel.setBorder(BorderFactory.createTitledBorder("Equipment"));
        refreshEquipmentPanel();

        detailArea.setEditable(false);
        detailArea.setLineWrap(true);
        detailArea.setWrapStyleWord(true);
        detailArea.setPreferredSize(new Dimension(280, 150));
        detailArea.setBorder(BorderFactory.createTitledBorder("Details"));

        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 5, 5));
        buttonPanel.add(btnEquip);
        buttonPanel.add(btnUse);
        buttonPanel.add(btnDrop);

        leftCol.add(equipPanel);
        leftCol.add(Box.createVerticalStrut(10));
        leftCol.add(detailArea);
        leftCol.add(Box.createVerticalStrut(10));
        leftCol.add(buttonPanel);

       
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Equipment", new JScrollPane(gridPanel));
        tabs.addTab("Consumables", new JScrollPane(gridPanel));
        tabs.addTab("Materials", new JScrollPane(gridPanel));

        root.add(leftCol, BorderLayout.WEST);
        root.add(tabs, BorderLayout.CENTER);

        refreshGrid();

        btnEquip.addActionListener(e -> equipItem());
        btnUse.addActionListener(e -> useItem());
        btnDrop.addActionListener(e -> dropItem());
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
            row.add(new JLabel(name + ":"));
            JLabel icon = new JLabel();
            icon.setPreferredSize(new Dimension(40, 40));
            if (slot.item != null)
                icon.setIcon(slot.item.getIcon());
            row.add(icon);

            row.setBorder(new LineBorder(Color.DARK_GRAY));
            row.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    selectedSlot = slot;
                    updateDetail();
                }
            });

            equipPanel.add(row);
        }
        equipPanel.revalidate();
        equipPanel.repaint();
    }

    private JPanel makeSlotComponent(Slot slot) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new LineBorder(Color.DARK_GRAY));
        panel.setPreferredSize(new Dimension(80, 80));

        JLabel img = new JLabel();
        img.setHorizontalAlignment(SwingConstants.CENTER);

        if (slot.item != null)
            img.setIcon(slot.item.getIcon());

        panel.add(img, BorderLayout.CENTER);

        if (slot.amount > 1) {
            JLabel count = new JLabel(String.valueOf(slot.amount));
            panel.add(count, BorderLayout.SOUTH);
        }

       
        panel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                selectedSlot = slot;
                updateDetail();
            }
        });

        
        panel.setTransferHandler(new TransferHandler("item"));

      
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

    private void updateDetail() {
        detailArea.setText("");
        if (selectedSlot == null || selectedSlot.item == null) {
            detailArea.setText("No item selected");
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

      
        String target = "Weapon";
        if (it.name.toLowerCase().contains("ring")) target = "Ring";

        Slot eq = equipmentSlots.get(target);
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

    public java.util.List<Slot> getInventorySlots() {
        return inventorySlots;
    }

    // Removed main method as this is now a JPanel to be embedded.
}
