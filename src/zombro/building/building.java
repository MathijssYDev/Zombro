package zombro.building;

import zombro.Game;
import zombro.UI.UI;

import javax.imageio.ImageIO;

import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Map;
import java.net.URL;
import java.awt.image.BufferedImage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.awt.event.MouseEvent;

public class building {
    public JSONArray MapBuildings = new JSONArray();
    public JSONArray ActiveBuildings = new JSONArray();

    public Image BlocksImages[]; // All the tool images

    public JSONObject Blocks = new JSONObject();
    public JSONArray blockedPositions = new JSONArray();
    public JSONObject BlocksInCategory = new JSONObject();

    Game game;

    int buildState = 1; // 0 = Default
    String buildStateBlock = "woodblock"; // 0 = Default
    String[] tabLabels = { "Weapons", "Buildables", "Facilities" };

    int dbst_Height = 200;

    int dbstTabWitdh = 140;
    int dbstTabHeight = 30;

    int[] mousePos = { 0, 0 };

    public building(Game game) {
        this.game = game;
    }

    public void loadblocks(Game game) {
        System.out.println("Loading Blocks...");
        JSONParser parser = new JSONParser();
        try {
            JSONArray tileSize = new JSONArray();
            tileSize.add(0, 64);
            tileSize.add(1, 85);

            ArrayList<Image> ImagesBuffer = new ArrayList<Image>();
            InputStream url = getClass()
                    .getResourceAsStream("/zombro/building/blocks.json");
            Object obj = parser.parse(new InputStreamReader(url));
            JSONObject jsonObject = (JSONObject) obj;

            for (String i : tabLabels) {
                BlocksInCategory.put(i, new JSONObject());
            }

            // Load Weapons
            for (Object Block : jsonObject.keySet()) {
                JSONObject Block_ = (JSONObject) jsonObject.get(Block);

                Object d = Block_.get("asset");
                Block_.put("spriteIndex", ImagesBuffer.size());
                ImagesBuffer.add(loadBlockSprite((String) d, tileSize));
                jsonObject.put(Block, Block_);

                JSONObject cat = (JSONObject) BlocksInCategory.get((String) Block_.get("category"));
                cat.put(Block, Block_);
            }
            Blocks = jsonObject;

            Image out[] = new Image[ImagesBuffer.size()];
            for (int i = 0; i < ImagesBuffer.size(); i++) {
                out[i] = ImagesBuffer.get(i);
            }
            BlocksImages = out;

            System.out.println("Done Loading Blocks!");
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            game.log("Game", "Building", "ERROR",
                    "Error while trying to load Blocks | StackTrace: "
                            + sw.toString() + " > " + e.getMessage());
        }
    }

    public Image loadBlockSprite(String d, JSONArray size) {
        try {
            InputStream u = getClass()
                    .getResourceAsStream(
                            "/zombro/resources/blocks/" + d);
            BufferedImage img = ImageIO.read(u);
            int newWidth = (int) size.get(0);
            int newHeight = (int) size.get(1);

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
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            game.log("Game", "Building", "ERROR",
                    "Error while trying to load item sprites | StackTrace: "
                            + sw.toString() + " > " + e.getMessage());
            return null;
        }
    }

    public void mouseClicked(MouseEvent e) {
        int coords[] = { e.getX(), e.getY() };
        mousePos = coords;

        buildTabColistionCheck();

        int tile[] = getTileOnCoord(mousePos);
        JSONArray blockedPositionsCopy = (JSONArray) blockedPositions.clone();
        for (Object obj : blockedPositionsCopy) {
            JSONArray jsonObj = (JSONArray) obj;
            if ((int) jsonObj.get(0) / 64 == tile[0] && (int) jsonObj.get(1) / 64 == tile[1]) {
                return;
            }
        }
        int tilePixel[] = { tile[0] * 64, tile[1] * 64 };
        placeBlock(tilePixel, (JSONObject) Blocks.get(buildStateBlock));

    }

    public void placeBlock(int[] coords, JSONObject block) {
        JSONArray i = new JSONArray();
        i.add(coords[0]);
        i.add(coords[1]);
        i.add(block);
        i.add((int) block.get("spriteIndex"));
        MapBuildings.add(i);
        JSONArray x = new JSONArray();
        x.add(coords[0]);
        x.add(coords[1]);
        blockedPositions.add(x);
    }

    public void mousePositionUpdate(MouseEvent e) {
        int coords[] = { e.getX(), e.getY() };
        mousePos = coords;
    }

    public int[] getTileOnCoord(int[] coords) {
        int row = (coords[0] + game.gui.camera[0]) / game.sizeOfBlock;
        int column = (coords[1] + game.gui.camera[1]) / game.sizeOfBlock;
        int[] out = { row, column };
        return out;
    }

    public int getBlockOnTile(int[] tile) {
        for (int i = 0; i < ActiveBuildings.size(); i++) {
            JSONArray v = (JSONArray) ActiveBuildings.get(i);
            if ((int) v.get(0) == tile[0] && (int) v.get(1) == tile[1]) {
                return i;
            }
        }
        return -1;
    }

    public JSONArray getBuildingInCamera(int coords[], int proximity) {
        JSONArray as = new JSONArray();
        int width = game.gui.getWidth();
        int height = game.gui.getHeight();

        int minWidth = coords[0] + -proximity;
        int maxWidth = coords[0] + width + proximity;
        int minHeight = coords[1] + -proximity;
        int maxHeight = coords[1] + height + proximity;

        JSONArray MapBuildingsCopy = (JSONArray) MapBuildings.clone();
        for (Object i : MapBuildingsCopy) {
            JSONArray i_ = (JSONArray) i;
            if ((int) i_.get(0) > minWidth && (int) i_.get(0) < maxWidth && (int) i_.get(1) > minHeight &&
                    (int) i_.get(1) < maxHeight) {
                as.add(i_);
            }
        }
        return as;
    }

    public void buildTabColistionCheck() {

    }

    public JSONArray OrganizeBuildingsOnZIndex(JSONArray in) {
        JSONArray out = new JSONArray(in);
        for (int i = 0; i < in.size(); i++) {
            JSONArray check = (JSONArray) in.get(i);
            JSONArray out_ = (JSONArray) out.get(i);
            JSONArray checkClone = (JSONArray) check.clone();
            int y = (int) check.get(1);
            int y_ = (int) out_.get(1);
            out.remove(check);
            if (y >= y_) {
                out.add(i, checkClone);
            } else if (i != in.size() - 1) {
                out.add(i + 1, checkClone);
            } else {
                out.add(checkClone);
            }
        }
        return out;
    }

    public void drawBuildings(Graphics2D g) {
        ActiveBuildings = OrganizeBuildingsOnZIndex(getBuildingInCamera(game.gui.camera, 100));

        int width = game.gui.getWidth();
        int height = game.gui.getHeight();

        int cameraX = game.gui.camera[0];
        int cameraY = game.gui.camera[1];

        if (game.buildmode) {
            JSONArray blockedPositionsCopy = (JSONArray) blockedPositions.clone();
            for (Object obj : blockedPositionsCopy) {
                JSONArray jsonObj = (JSONArray) obj;
                g.setColor(new Color(255, 0, 0, 75));
                g.fillRect((Integer) jsonObj.get(0) - cameraX,
                        (Integer) jsonObj.get(1) - cameraY, 64, 64);
                g.setColor(new Color(255, 0, 0, 150));
                g.drawRect((Integer) jsonObj.get(0) - cameraX,
                        (Integer) jsonObj.get(1) - cameraY, 64, 64);
            }

            int tile[] = getTileOnCoord(mousePos);

            g.drawImage(BlocksImages[(int) ((JSONObject) Blocks.get(buildStateBlock)).get("spriteIndex")],
                    tile[0] * game.sizeOfBlock - cameraX,
                    tile[1] * game.sizeOfBlock - cameraY, null);
            g.setStroke(new BasicStroke(2));
            g.setColor(new Color(220, 220, 220, 255));
            g.drawRoundRect(tile[0] * game.sizeOfBlock - cameraX, tile[1] * game.sizeOfBlock -
                    cameraY,
                    game.sizeOfBlock,
                    game.sizeOfBlock + 10, 5, 5);

            // ------------------ draw block selection tab ------------------
            Font f = new Font("hooge 05_53", Font.PLAIN, 15);
            g.setFont(f);
            for (int t = 0; t < 3; t++) {
                Color bg = new Color(20, 20, 20, 255);
                Color txt = new Color(255, 255, 255, 255);

                if (buildState == t) {
                    bg = new Color(255, 255, 255, 255);
                    txt = new Color(20, 20, 20, 255);
                }

                g.setColor(bg);
                g.fillRect((dbstTabWitdh + 2) * t, game.gui.getHeight() - dbst_Height - dbstTabHeight, dbstTabWitdh,
                        dbstTabHeight);

                int w = g.getFontMetrics(f).stringWidth(tabLabels[t]);
                g.setColor(txt);
                g.drawString(tabLabels[t], (dbstTabWitdh + 2) * t + (dbstTabWitdh - w) / 2,
                        game.gui.getHeight() - dbst_Height - 4 - 5);
            }

            g.fillRect(0, game.gui.getHeight() - dbst_Height, game.gui.getWidth(), dbst_Height);

            g.setColor(new Color(255, 255, 255, 255));
            g.fillRect(0, game.gui.getHeight() - dbst_Height, game.gui.getWidth(), dbst_Height);

            JSONObject cat = (JSONObject) BlocksInCategory.get(tabLabels[buildState]);

            int index = 0;
            for (Object obj : cat.keySet()) {
                JSONObject jsonObject = (JSONObject) cat.get((String) obj);
                JSONArray size = new JSONArray();
                size.add(64 * 2);
                size.add(85 * 2);

                Image img = loadBlockSprite((String) jsonObject.get("asset"), size);
                g.drawImage(img,
                        40 + (128 + 20) * index, game.gui.getHeight() - dbst_Height / 2 - 85,
                        null);
                index++;
            }
        }
        for (Object i : ActiveBuildings) {
            JSONArray i_ = (JSONArray) i;
            Image img = BlocksImages[(int) i_.get(3)];

            g.drawImage(img, (int) i_.get(0) - game.gui.camera[0], (int) i_.get(1) - game.gui.camera[1],
                    null);
        }
        for (Object i : ActiveBuildings) {
            JSONArray i_ = (JSONArray) i;

            // Collision check

            int r = 32;
            int h = game.sizeOfBlock / 2;

            int x_ = (int) i_.get(0) - game.gui.camera[0] + h; // Asset_X
            int y_ = (int) i_.get(1) - game.gui.camera[1] + h; // Asset_Y

            int x = x_; // Asset_X_Origin
            int y = y_; // Asset_Y_Origin

            // if (Math.abs(x) + 40 <= h && Math.abs(y) + 40 <= h) {
            int a = Math.abs((game.gui.getWidth() / 2) - x);
            int b = Math.abs((game.gui.getHeight() / 2) - y);

            double d = (double) Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
            if (d <= 60 + r) {
                double ang = Math.toDegrees(Math.acos(a / d));
                double ang_;
                int direction = 0;

                int xMod = 1;
                int yMod = 1;
                if ((game.gui.getWidth() / 2) >= x && (game.gui.getHeight() / 2) >= y) {
                    direction = 3;
                    xMod = 1;
                    yMod = 1;
                }
                if ((game.gui.getWidth() / 2) <= x && (game.gui.getHeight() / 2) >= y) {
                    direction = 2;
                    xMod = -1;
                    yMod = 1;
                }
                if ((game.gui.getWidth() / 2) >= x && (game.gui.getHeight() / 2) <= y) {
                    direction = 0;
                    xMod = 1;
                    yMod = -1;
                }
                if ((game.gui.getWidth() / 2) <= x && (game.gui.getHeight() / 2) <= y) {
                    direction = 1;
                    xMod = -1;
                    yMod = -1;
                }

                if ((direction + 1) % 2 == 1) {
                    ang_ = direction * 90 + ang;
                } else {
                    ang_ = ((direction + 1) * 90) - ang;
                }

                int xMod_ = 1;
                int yMod_ = 1;
                if (ang_ > 45 && ang_ < 135) {
                    xMod_ = 0;
                    yMod_ = -1;

                } else if (ang_ >= 135 && ang_ < 225) {
                    xMod_ = -1;
                    yMod_ = 0;
                } else if (ang_ >= 225 && ang_ < 315) {
                    xMod_ = 0;
                    yMod_ = 1;
                } else if (ang_ >= 315 || ang_ < 45) {
                    xMod_ = 1;
                    yMod_ = 0;
                }

                int px = (32 + r) - a;
                int py = (32 + r) - b;
                if (px > 0) {
                    game.gui.cameraDetailed[0] += ((32 + r) - a) * xMod_;
                }
                if (py > 0) {
                    game.gui.cameraDetailed[1] += ((32 + r) - b) * yMod_;
                }

                game.gui.camera[0] = Math.round(game.gui.cameraDetailed[0]);
                game.gui.camera[1] = Math.round(game.gui.cameraDetailed[1]);

            }

            if (game.devMode) {
                g.setStroke(new BasicStroke(1));
                g.setColor(Color.green);
                g.drawRect(x - (game.sizeOfBlock / 2), y - (game.sizeOfBlock / 2), 64, 64);
                g.setColor(Color.red);
                g.drawOval(x - 13 - (game.sizeOfBlock / 2), y - 13 - (game.sizeOfBlock / 2), 45 * 2, 45 * 2);
                g.setColor(Color.blue);
                g.setStroke(new BasicStroke(1));
                g.drawLine((game.gui.getWidth() / 2), (game.gui.getHeight() / 2), x, y);
                g.setColor(Color.red);
                g.drawOval((game.gui.getWidth() / 2) - r, (game.gui.getHeight() / 2) - r, r * 2,
                        r * 2);
                g.setColor(Color.orange);
                g.fillOval(x, y, 10, 10);
            }
        }
    }
}