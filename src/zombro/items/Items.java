package zombro.items;

import zombro.Game;
import javax.imageio.ImageIO;

import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.net.URL;
import java.awt.image.BufferedImage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Items {
    Game game;

    public Image Images[]; // All the tool images
    public Image InventoryImages[]; // All the tool images

    public JSONObject Weapons = new JSONObject();
    public JSONObject Resources = new JSONObject();

    public Items(Game game) {
        this.game = game;
    }

    public void loadTools(Game game) {
        this.game = game;
        System.out.println("Loading Items...");
        JSONParser parser = new JSONParser();
        try {
            JSONArray tileSize = new JSONArray();
            int tileSize_ = 54;
            tileSize.add(0, tileSize_);
            tileSize.add(1, tileSize_);

            ArrayList<Image> ImagesBuffer = new ArrayList<Image>();
            ArrayList<Image> InventoryImagesBuffer = new ArrayList<Image>();
            InputStream url = getClass()
                    .getResourceAsStream("/zombro/items/items.json");
            Object obj = parser.parse(new InputStreamReader(url));
            JSONObject jsonObject = (JSONObject) obj;

            JSONObject weapons = (JSONObject) jsonObject.get("weapons");
            JSONObject resources = (JSONObject) jsonObject.get("resources");
            // Load Weapons
            for (Object Type : weapons.keySet()) {
                JSONObject Type_ = (JSONObject) weapons.get(Type);

                for (Object Item : Type_.keySet()) {
                    JSONObject ItemObject = (JSONObject) Type_.get(Item);

                    Object d = ItemObject.get("asset");
                    JSONArray size = (JSONArray) ItemObject.get("size");
                    ItemObject.put("spriteIndex", ImagesBuffer.size());
                    // Type_.put(Item, ItemObject);
                    ImagesBuffer.add(loadItemSprite((String) d, size));
                    InventoryImagesBuffer.add(loadItemSprite((String) d, tileSize));
                }
                // weapons.put(Type, Type_);
            }
            Weapons = weapons;

            // Load Resources
            for (Object Type : resources.keySet()) {
                JSONObject Type_ = (JSONObject) resources.get(Type);
                JSONArray size = (JSONArray) Type_.get("size");
                String asset = (String) Type_.get("asset");
                Type_.put("spriteIndex", ImagesBuffer.size());
                // resources.put(Type, Type_);
                ImagesBuffer.add(loadItemSprite((String) asset, size));
                InventoryImagesBuffer.add(loadItemSprite((String) asset, tileSize));
            }
            Resources = resources;

            Image out[] = new Image[ImagesBuffer.size()];
            for (int i = 0; i < ImagesBuffer.size(); i++) {
                out[i] = ImagesBuffer.get(i);
            }
            Images = out;
            out = new Image[InventoryImagesBuffer.size()];
            for (int i = 0; i < InventoryImagesBuffer.size(); i++) {
                out[i] = InventoryImagesBuffer.get(i);
            }
            InventoryImages = out;

            System.out.println("Done Loading Items!");
        } catch (Exception e) {
            System.out.println("Error while trying to load items: " + e);
            e.printStackTrace();
        }
    }

    public JSONObject getBlankInventoryItem(int index) {
        JSONParser getBlankInventoryItemparser = new JSONParser();
        try {
            String jsonString = "{\"name\":\"none\",\"index\":" + index + ",\"collection\":\"none\",\"type\":\"none\"}";
            JSONObject jsonObject = (JSONObject) getBlankInventoryItemparser.parse(jsonString);
            return jsonObject;
        } catch (Exception e) {
            System.out.println("Error while trying to getBlankInventoryItem: " + e);
            e.printStackTrace();
            return null;
        }
    }

    public Image loadItemSprite(String d, JSONArray size) {
        try {
            InputStream u = getClass()
                    .getResourceAsStream(
                            "/zombro/resources/tools/" + d);
            BufferedImage img = ImageIO.read(u);
            int newWidth;
            int newHeight;
            try {
                newWidth = Math.toIntExact((long) size.get(0));
                newHeight = Math.toIntExact((long) size.get(1));
            } catch (Exception e) {
                newWidth = (int) size.get(0);
                newHeight = (int) size.get(1);
            }

            BufferedImage upscaledImage = new BufferedImage((int) newWidth, (int) newHeight,
                    BufferedImage.TYPE_INT_ARGB);

            for (int y = 0; y < newHeight; y++) {
                for (int b = 0; b < newWidth; b++) {
                    int originalX;
                    int originalY;
                    originalX = (int) Math.min(b / ((float) newWidth / (float) img.getWidth()),
                            img.getWidth() - 1);
                    originalY = (int) Math.min(y / ((float) newHeight / (float) img.getHeight()),
                            img.getHeight() - 1);
                    int rgb = img.getRGB(originalX, originalY);
                    upscaledImage.setRGB(b, y, rgb);
                }
            }
            return upscaledImage;
        } catch (Exception e) {
            System.out.println("Error while trying to load item sprites: " + e);
            e.printStackTrace();
            return null;
        }
    }

    public JSONObject getItem(Object collection, Object type, Object name) {
        if (collection.equals("weapons")) {
            JSONObject type_ = (JSONObject) Weapons.get(type);
            return (JSONObject) type_.get(name);
        }
        if (collection.equals("resources")) {
            JSONObject i = (JSONObject) Resources.get(name);
            return i;
        }
        return null;
    }

    public Image getItemSprite(Object collection, Object type, Object name) {
        JSONObject item = getItem(collection, type, name);
        if (item == null) {
            return null;
        }
        Image img = InventoryImages[Math.toIntExact((Integer) ((Object) item.get("spriteIndex")))];
        return img;
    }

    public JSONObject getDefaultPlayerInventory() {
        JSONParser parser = new JSONParser();
        try {
            InputStream url = getClass()
                    .getResourceAsStream("/zombro/items/inventory.json");
            Object obj = parser.parse(new InputStreamReader(url));
            JSONObject jsonObject = (JSONObject) obj;
            return jsonObject;
        } catch (Exception e) {
            System.out.println("Error while trying read Default Player Inventory File: " + e);
            e.printStackTrace();
            return null;
        }
    }
}
