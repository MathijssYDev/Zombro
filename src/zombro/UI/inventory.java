package zombro.UI;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import zombro.Game;
import zombro.Player;
import zombro.items.Items;

public class inventory extends JPanel {
    Graphics2D g;
    int w = 730;
    int h = 550;

    int Itemw = 640;
    int Itemh = 80 * 4;

    public int SizePerSlot = 80; // In pixels
    int AmountOfSlots = 32;

    BufferedImage inventoryTile;

    Game game;
    Items items;
    UI gui;

    String SelectedLocation;
    int SelectedTile = -1;
    String DropLocation;
    int DropTile = -1;
    String HoverLocation;
    int HoverTile = -1;
    boolean isDragging = false;

    int[] mousePos = { 0, 0 };

    public inventory(UI gui, Game game) {
        this.gui = gui;
        this.game = game;
        try {

            InputStream inventoryTileu = getClass().getResourceAsStream("/zombro/resources/ui/inventorytile.png");
            inventoryTile = ImageIO.read(inventoryTileu);

            long newWidth = SizePerSlot;
            long newHeight = SizePerSlot;

            BufferedImage inventoryTile_ = new BufferedImage((int) newWidth, (int) newHeight,
                    BufferedImage.TYPE_INT_ARGB);

            for (int y = 0; y < newHeight; y++) {
                for (int b = 0; b < newWidth; b++) {
                    int originalX;
                    int originalY;
                    originalX = (int) Math.min(b / ((float) newWidth / (float) inventoryTile.getWidth()),
                            inventoryTile.getWidth(gui) - 1);
                    originalY = (int) Math.min(y / ((float) newHeight / (float) inventoryTile.getHeight()),
                            inventoryTile.getHeight(gui) - 1);
                    int rgb = inventoryTile.getRGB(originalX, originalY);
                    inventoryTile_.setRGB(b, y, rgb);
                }
            }
            inventoryTile = inventoryTile_;

        } catch (Exception e) {
            System.out.println("Error while trying to load inventory: " + e);
            e.printStackTrace();
        }
    }

    public void drawTile(Graphics2D g, int x, int y) {
        g.setColor(Color.decode("#a67250"));
        g.fillRect(x, y, SizePerSlot, SizePerSlot);
        g.drawImage(inventoryTile, null, x, y);
        g.setColor(Color.decode("#CCA28C"));
        g.setStroke(new BasicStroke(3));
        g.drawRect(x, y, SizePerSlot, SizePerSlot);
    }

    public void drawItem(Graphics2D g, int x, int y) {
        g.setColor(Color.decode("#a67250"));
        g.fillRect(x, y, SizePerSlot, SizePerSlot);
        g.drawImage(inventoryTile, null, x, y);
        g.setColor(Color.decode("#261F27"));
        g.setStroke(new BasicStroke(3));
        g.drawRect(x, y, SizePerSlot, SizePerSlot);
    }

    public int[] getTileOnCoord(int[] coords) {
        int[] r = { -1, -1 };
        if (coords[0] > gui.getWidth() / 2 - w / 2 && coords[0] < gui.getWidth() / 2 + w / 2
                && coords[1] > gui.getHeight() / 2 - h / 2 && coords[1] < gui.getHeight() / 2 + h / 2) {
            if (coords[0] > gui.getWidth() / 2 - Itemw / 2 && coords[0] < gui.getWidth() / 2 + Itemw / 2
                    && coords[1] > gui.getHeight() / 2 - Itemh / 2 && coords[1] < gui.getHeight() / 2 + Itemh / 2) {
                int x = (coords[0] - (gui.getWidth() / 2 - Itemw / 2)) / SizePerSlot;
                int y = (coords[1] - (gui.getHeight() / 2 - Itemh / 2)) / SizePerSlot;
                int i = x + (y * 8);
                r[0] = i;
                r[1] = 1;
                return r;
            }
            int shiftRight = (w - 8 * SizePerSlot) / 2;
            int w_ = 8 * SizePerSlot;
            int h_ = SizePerSlot;
            if (coords[1] >= ((gui.getHeight() / 2) + (h / 2)) - SizePerSlot && coords[0] - w > -SizePerSlot
                    && coords[0] - w < w_ - shiftRight) {
                int x = (coords[0] - (gui.getWidth() / 2 - w_ / 2)) / SizePerSlot;
                if (x > 7) {
                    x = -1;
                } else if (x < 0) {
                    x = -1;
                }
                r[0] = x;
                r[1] = 0;
                return r;
            }
        }
        return r;
    }

    public void swapItems(String LocationMain, int TileMain, String LocationSecondary, int TileSecondary) {
        Player plr = game.getMainPlayer();
        Object itemMain = getItemInInventoryByTile(LocationMain, TileMain).clone();
        Object itemSecondary = getItemInInventoryByTile(LocationSecondary, TileSecondary).clone();
        if (itemSecondary == null) {
            itemSecondary = items.getBlankInventoryItem(TileSecondary);
        }
        try {
            setItemInInventoryByTile(LocationMain, TileMain, (JSONObject) itemSecondary);
            setItemInInventoryByTile(LocationSecondary, TileSecondary, (JSONObject) itemMain);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    public void mouseDragged(MouseEvent e) {
        int coords[] = { e.getX(), e.getY() };
        mousePos = coords;
        int[] t = getTileOnCoord(coords);
        String loc = null;
        if (t[1] == 0) {
            loc = "equiped";
        }
        if (t[1] == 1) {
            loc = "inventory";
        }
        if (t[1] == 2) {
            loc = "give";
        }
        if (t[1] == 3) {
            loc = "secondary";
        }
        if (!isDragging) {
            isDragging = true;
            SelectedLocation = loc;
            SelectedTile = t[0];
        }
    }

    public void mouseReleased(MouseEvent e) {
        int coords[] = { e.getX(), e.getY() };
        int[] t = getTileOnCoord(coords);
        String loc = null;
        if (t[1] == 0) {
            loc = "equiped";
        }
        if (t[1] == 1) {
            loc = "inventory";
        }
        if (t[1] == 2) {
            loc = "give";
        }
        if (t[1] == 3) {
            loc = "secondary";
        }
        if (isDragging) {
            isDragging = false;
            DropLocation = loc;
            DropTile = t[0];
            if (DropTile == -1)
                return;
            swapItems(SelectedLocation, SelectedTile, DropLocation, DropTile);
        }
    }

    public void mousePositionUpdate(MouseEvent e) {
        int coords[] = { e.getX(), e.getY() };
        mousePos = coords;

    }

    public void setItemInInventoryByTile(String location, int tile, JSONObject Item) {
        try {
            Player plr = game.getMainPlayer();
            JSONObject plrInventory = plr.getPlayerInventory();
            JSONArray plrInventoryObject = (JSONArray) plrInventory.get(location);
            JSONObject item = (JSONObject) getItemInInventoryByTile(location, tile).clone();

            item.put("collection", Item.get("collection"));
            item.put("type", Item.get("type"));
            item.put("name", Item.get("name"));

            plrInventoryObject.set(tile, item);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

    }

    public Image getSpriteItemInInventoryByTile(String location, int tile) {
        Player plr = game.getMainPlayer();
        if (location == "equiped" || location == "inventory") {
            try {
                JSONObject plrInventory = plr.getPlayerInventory();
                JSONArray plrInventoryObject = (JSONArray) plrInventory.get(location);
                Object item = plrInventoryObject.get(tile);
                JSONObject itemJson = (JSONObject) item;

                Object collection = itemJson.get("collection");
                Object type = itemJson.get("type");
                Object name = itemJson.get("name");

                Image sprite = items.getItemSprite(collection, type, name);
                if (sprite == null) {
                    return null;
                }
                return sprite;
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    public JSONArray resetMainPlayerInventory(int length, Items items) {
        JSONArray n = new JSONArray();
        for (int i = 0; i < length; i++) {
            n.add(items.getBlankInventoryItem(i));
        }
        return n;
    }

    public JSONObject getItemInInventoryByTile(String location, int tile) {
        JSONObject obj = items.getBlankInventoryItem(tile);
        Player plr = game.getMainPlayer();
        if (location == "equiped" || location == "inventory") {
            try {
                JSONObject plrInventory = plr.getPlayerInventory();
                JSONArray plrInventoryObject = (JSONArray) plrInventory.get(location);
                Object item = plrInventoryObject.get(tile);
                JSONObject itemJson = (JSONObject) item;

                return itemJson;
            } catch (Exception e) {
                return obj;
            }
        }
        return obj;
    }

    public void moveItemWithCursor(Graphics2D g, String location, int tile) {
        Player plr = game.getMainPlayer();
        Image sprite = getSpriteItemInInventoryByTile(location, tile);
        if (sprite == null) {
            return;
        }
        int x = mousePos[0] - SizePerSlot / 2;
        int y = mousePos[1] - SizePerSlot / 2;
        g.drawImage(sprite, x + 13, y + 13, gui);
    }

    public void drawInventory(Graphics2D g, UI gui, Game game) {
        this.g = g;
        this.items = game.items;
        if (!gui.inventory) {
            return;
        }
        g.setColor(new Color(0.0f, 0.0f, 0.0f, 0.2f));
        g.fillRect(0, 0, gui.getWidth(), gui.getHeight());

        // Main slots
        int maxAmountOfSlotsPerRow = (int) (Itemw / SizePerSlot);

        int topLeftX = gui.getWidth() / 2 - Itemw / 2;
        int topLeftY = gui.getHeight() / 2 - Itemh / 2;

        int row = 0;
        int column = 0;
        for (int c = 0; c < AmountOfSlots; c++) {

            drawTile(g, column * SizePerSlot + topLeftX, row * SizePerSlot + topLeftY);
            if ((column + 1) % maxAmountOfSlotsPerRow == 0) {
                row++;
                column = 0;
            } else {
                column++;
            }
        }
        // Main Items

        row = 0;
        column = 0;

        Player plr = game.getMainPlayer();
        JSONObject plrInventory = plr.getPlayerInventory();

        JSONArray plrMainObject = (JSONArray) plrInventory.get("inventory");
        for (Object item : plrMainObject) {
            JSONObject itemJson = (JSONObject) item;

            Object collection = itemJson.get("collection");
            Object type = itemJson.get("type");
            Object name = itemJson.get("name");
            Object index = itemJson.get("index");
            row = Math.toIntExact((long) index / maxAmountOfSlotsPerRow);
            column = Math.toIntExact((long) index - row * maxAmountOfSlotsPerRow);
            if (collection == "none") {
                continue;
            }

            Image sprite = items.getItemSprite(collection, type, name);

            int x = column * SizePerSlot + topLeftX;
            int y = row * SizePerSlot + topLeftY;
            g.drawImage(sprite, x + 13, y + 13, gui);
            g.setColor(Color.decode("#CCA28C"));
            g.drawRect(x, y, SizePerSlot, SizePerSlot);

        }
        // Main slots selected
        row = 0;
        column = 0;
        for (int c = 0; c < AmountOfSlots; c++) {
            if (c == SelectedTile && SelectedLocation == "inventory") {
                g.setColor(Color.decode("#261F27")); // Selected
                g.drawRect(column * SizePerSlot + topLeftX, row * SizePerSlot + topLeftY, SizePerSlot - 3,
                        SizePerSlot - 3);
            }
            if (c == DropTile && DropLocation == "inventory") {
                g.setColor(Color.decode("#261F27"));
                g.drawRect(column * SizePerSlot + topLeftX, row * SizePerSlot + topLeftY, SizePerSlot - 3,
                        SizePerSlot - 3);
            }
            if ((column + 1) % maxAmountOfSlotsPerRow == 0) {
                row++;
                column = 0;
            } else {
                column++;
            }
        }

        int shiftRight = (w - 8 * SizePerSlot) / 2;

        // HotBar Slots
        topLeftX = gui.getWidth() / 2 - w / 2;
        topLeftY = gui.getHeight() / 2 - h / 2;
        for (int c = 0; c < 8; c++) {
            int x = topLeftX + c * SizePerSlot + shiftRight;
            int y = topLeftY + (h - SizePerSlot);

            drawTile(g, x, y);
            Font f = new Font("hooge 05_55", Font.BOLD, 15);
            g.setColor(Color.decode("#CCA28C"));
            g.setFont(f);
            g.drawString(String.valueOf(c + 1), x + 10, y + SizePerSlot - 10);
        }

        // Draw Give button

        // Draw Items equiped

        topLeftX = gui.getWidth() / 2 - w / 2;
        topLeftY = gui.getHeight() / 2 - h / 2;

        plrInventory = plr.getPlayerInventory();
        JSONArray plrEquipedObject = (JSONArray) plrInventory.get("equiped");
        for (Object item : plrEquipedObject) {
            JSONObject itemJson = (JSONObject) item;

            Object collection = itemJson.get("collection");
            Object type = itemJson.get("type");
            Object name = itemJson.get("name");
            Object index = itemJson.get("index");

            if (collection == "none") {
                continue;
            }

            Image sprite = items.getItemSprite(collection, type, name);
            int x = topLeftX + (Math.toIntExact((Long) index) * SizePerSlot) + shiftRight;
            int y = topLeftY + (h - SizePerSlot);
            g.drawImage(sprite, x + 13, y + 13, gui);
            g.setColor(Color.decode("#CCA28C"));
            g.drawRect(x, y, SizePerSlot, SizePerSlot);
        }
        // Hotbar selected

        for (int c = 0; c < 8; c++) {
            int x = topLeftX + c * SizePerSlot + shiftRight;
            int y = topLeftY + (h - SizePerSlot);

            if (c == SelectedTile && SelectedLocation == "equiped") {
                g.setColor(Color.decode("#261F27")); // Selected
                g.drawRect(x, y, SizePerSlot - 3,
                        SizePerSlot - 3);
            }
            if (c == DropTile && DropLocation == "equiped") {
                g.setColor(Color.decode("#261F27"));
                g.drawRect(x, y, SizePerSlot - 3,
                        SizePerSlot - 3);
            }
        }

        if (isDragging) {
            moveItemWithCursor(g, SelectedLocation, SelectedTile);
        }
        if (!isDragging && (SelectedTile != -1 || DropTile != -1)) {
            SelectedTile = -1;
            DropTile = -1;
            SelectedLocation = "";
            DropLocation = "";
        }
        // g.drawRect(topLeftX, topLeftY, w, h);
    }
}
