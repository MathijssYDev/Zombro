package zombro.UI;

import javax.imageio.ImageIO;

import org.json.simple.JSONArray;

import java.awt.*;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;
import java.awt.image.BufferedImage;
import zombro.Game;
import zombro.zombroServer.ServerWorld;

import java.util.Collections;

public class Assets {
    Game game;

    public int treeSpreading = 5; // 1-10
    public int amountOfTreeGroups = 500; // Minimal = 1

    public int amountOfBigRocks = 40; // Minimal = 1

    public int amountOfSmallRocks = 150; // Minimal = 1
    public int amountOfGrass = 300;

    public int mapSize; // Mapsize -> X & Y (Square map)

    public BufferedImage GrassAsset;
    public BufferedImage TallGrassAsset;
    String assetImagesFileNames[][] = {
            { "coal", "emeralds", "gold", "rubies", "sapphires", "silver" },
            { "Rock1_grass_shadow_dark1", "Rock4_grass_shadow_dark1", "Rock5_grass_shadow_dark1",
                    "Rock1_grass_shadow_dark5", "Rock4_grass_shadow_dark5", "Rock5_grass_shadow_dark5" },
            { "Tree2", "Tree3" }
    }; // All the assets file names
    public Image assetImages[][]; // All the assets images

    public ArrayList<ArrayList<ArrayList<Integer>>> MapAssets = new ArrayList<>(); // All the assets in the map
    // {{{(ASSET TYPE),(ASSET INDEX),(X CENTER),(Y
    // CENTER),(WITDH),(HEIGHT)} <-- Asset} <-- Layer (0 is lowest) } <--
    // Containter
    public ArrayList<ArrayList<ArrayList<Integer>>> ActiveAssets = new ArrayList<>();

    public JSONArray blockedPositionsRandom = new JSONArray();

    // -------------- Constructors --------------
    public Assets() {
    }

    public Assets(Game game, int mapSize) { // If host, or not connected to server? Then set Assets to null
        this(game, mapSize, null);
    }

    public Assets(Game game, int mapSize, ArrayList<ArrayList<ArrayList<Integer>>> MapAssets) {
        this.game = game;
        this.mapSize = mapSize;
        this.MapAssets = MapAssets; // If not host or saved assets
        this.loadAssets();
    }
    // -------------- Loading --------------

    public void loadAssets() {
        System.out.println("Loading Assets...");
        try {
            InputStream GrassAssetu = getClass().getResourceAsStream("/zombro/resources/grass.png");
            GrassAsset = ImageIO.read(GrassAssetu);
            InputStream TallGrassAssetu = getClass()
                    .getResourceAsStream("/zombro/resources/tallgrass.png");
            TallGrassAsset = ImageIO.read(TallGrassAssetu);
            String type[] = { "gems", "stones", "trees" };
            ArrayList<ArrayList<Image>> ImageArray = new ArrayList<>();
            for (int i = 0; i < assetImagesFileNames.length; i++) {
                ImageArray.add(i, new ArrayList<Image>());
                for (int x = 0; x < assetImagesFileNames[i].length; x++) {
                    InputStream u = getClass()
                            .getResourceAsStream(
                                    "/zombro/resources/" + type[i] + "/" + assetImagesFileNames[i][x] + ".png");
                    BufferedImage img = ImageIO.read(u);
                    // Set the desired scale factor
                    int newWidth;
                    int newHeight;
                    double scale;
                    if (i == 2) {
                        scale = (64 * 3) / img.getWidth();

                        newWidth = (int) 64 * 3;
                        newHeight = (int) 64 * 3;
                    } else {
                        scale = 2.0;

                        // Calculate the new dimensions for the upscaled image
                        newWidth = (int) (img.getWidth() * scale);
                        newHeight = (int) (img.getHeight() * scale);
                    }

                    // Create a new BufferedImage with the upscaled dimensions
                    BufferedImage upscaledImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);

                    // Perform the upscale operation using nearest-neighbor interpolation
                    for (int y = 0; y < newHeight; y++) {
                        for (int b = 0; b < newWidth; b++) {
                            int originalX;
                            int originalY;
                            originalX = (int) Math.min(b / scale, img.getWidth() - 1);
                            originalY = (int) Math.min(y / scale, img.getHeight() - 1);
                            int rgb = img.getRGB(originalX, originalY);
                            upscaledImage.setRGB(b, y, rgb);
                        }
                    }

                    ImageArray.get(i).add(x, upscaledImage);
                }
            }
            Image out[][] = new Image[ImageArray.size()][];
            for (int i = 0; i < ImageArray.size(); i++) {
                Image out_[] = new Image[ImageArray.get(i).size()];
                for (int x = 0; x < ImageArray.get(i).size(); x++) {
                    out_[x] = ImageArray.get(i).get(x);
                }
                out[i] = out_;
            }
            assetImages = out;
        } catch (Exception e) {
            System.out.println("Error while loading assets: " + e);
            e.printStackTrace();
        }
        System.out.println("Done loading Assets!");
    }

    // -------------- Generation --------------
    public int grabId(Object source) {
        if (game == null) {
            ServerWorld sw = (ServerWorld) source;
            return sw.grabId();
        } else {
            return game.grabId();
        }
    }

    public ArrayList<ArrayList<ArrayList<Integer>>> randomAssets() {
        return this.randomAssets(game);
    }

    public ArrayList<ArrayList<ArrayList<Integer>>> randomAssets(Object source) {
        int mapSizeBlock;

        if (game == null) {
            ServerWorld sw = (ServerWorld) source;
            mapSizeBlock = 100;
        } else {
            mapSizeBlock = game.mapSizeBlock;
        }
        ArrayList<ArrayList<Integer>> usedPositions = new ArrayList<>();

        ArrayList<ArrayList<ArrayList<Integer>>> assets = new ArrayList<>();

        Random rand = new Random();
        assets.add(new ArrayList<ArrayList<Integer>>());
        for (int i = 0; i < amountOfGrass; i++) {
            ArrayList<Integer> asset = new ArrayList<>(); // (ASSET TYPE),(ASSETINDEX),(X CENTER),(Y
            // CENTER)
            asset.add(1); // Type stones
            asset.add(rand.nextInt(2) - 2); // Get length of array (-) the random value
            // to get a
            // small stone asset randomly. (Big
            // stone,Big stone,Big
            // stone, ----- Small stone,Small
            // stone,Small stone)
            asset.add(rand.nextInt(mapSize)); // Add a random X coordinate
            asset.add(rand.nextInt(mapSize)); // Add a random Y coordinate
            asset.add(grabId(source)); // Add a id
            asset.add(grabZIndex()); // Add a z-index
            assets.get(0).add(asset); // Add asset to layer 0
        }
        for (int i = 0; i < amountOfSmallRocks; i++) { // Making layer 0 - Smallrocks with no interaction
            ArrayList<Integer> asset = new ArrayList<>(); // (ASSET TYPE),(ASSETINDEX),(X CENTER),(Y
            // CENTER)
            asset.add(1); // Type stones
            asset.add(assetImagesFileNames[1].length - rand.nextInt(3) - 1); // Getlength of array (-) the random value
            // to get a
            // small stone asset randomly. (Big
            // stone,Big stone,Big
            // stone, ----- Small stone,Small
            // stone,Small stone)
            asset.add(rand.nextInt(mapSize)); // Add a random X coordinate
            asset.add(rand.nextInt(mapSize)); // Add a random Y coordinate
            asset.add(grabId(source)); // Add a id
            asset.add(grabZIndex()); // Add a z-index
            assets.get(0).add(asset); // Add asset to layer 0
        }

        // ----------------------- Trees -----------------------
        ArrayList<Integer> RandomOrganizedZIndex = new ArrayList<>();
        for (int i = 0; i < amountOfTreeGroups; i++) {
            int Y = rand.nextInt(mapSizeBlock);
            RandomOrganizedZIndex.add(i, Y * 64);
        }
        Collections.sort(RandomOrganizedZIndex);
        assets.add(new ArrayList<ArrayList<Integer>>());
        for (int i = 0; i < amountOfTreeGroups; i++) { // Making layer 1 - Trees with interaction
            int X = rand.nextInt(mapSizeBlock);
            ArrayList<Integer> asset = new ArrayList<>(); // (ASSET TYPE),(ASSET INDEX),(X CENTER),(Y
            // CENTER)
            int id = grabId(source);
            asset.add(2); // Type stones
            asset.add(rand.nextInt(2));
            asset.add(X * 64); // Add a random X coordinate
            asset.add((int) RandomOrganizedZIndex.get(i)); // Add a random Y coordinate
            asset.add(id); // Add a id
            asset.add(grabZIndex()); // Add a z-index
            asset.add(3); // Add width
            asset.add(3); // Add height
            asset.add(65); // Add a radius
            asset.add(100); // Add a Health
            assets.get(1).add(asset); // Add asset to layer 1

            ArrayList<Integer> pos = new ArrayList<Integer>(2);
            pos.add(0, X * 64);
            pos.add(1, RandomOrganizedZIndex.get(i));
            for (int h = 0; h < 3; h++) {
                for (int k = 0; k < 3; k++) {
                    JSONArray pos_ = new JSONArray(pos);
                    pos_.set(0, pos.get(0) + (h * 64));
                    pos_.set(1, pos.get(1) + (k * 64));
                    blockedPositionsRandom.add(pos_);
                }
            }

        }

        // ----------------------- Rocks -----------------------

        assets.add(new ArrayList<ArrayList<Integer>>());

        for (int i = 0; i < amountOfBigRocks; i++) { // Making layer 1 - Trees withinteraction
            int X = rand.nextInt(mapSizeBlock);
            int Y = rand.nextInt(mapSizeBlock);
            ArrayList<Integer> pos = new ArrayList<Integer>(2);
            pos.add(1);
            pos.add(1);
            int id = grabId(source);
            do {
                pos.set(0, rand.nextInt(mapSizeBlock));
                pos.set(1, rand.nextInt(mapSizeBlock));
            } while (usedPositions.contains(pos));
            for (int h = -1; h < 2; h++) {
                JSONArray posk_ = new JSONArray(pos);
                ArrayList<Integer> posk = new ArrayList<Integer>(pos);
                posk.set(0, pos.get(0) + h);
                posk_.set(0, pos.get(0) + h);
                for (int k = -1; k < 2; k++) {
                    posk.set(1, pos.get(1) + k);
                    posk_.set(1, pos.get(1) + k);
                    usedPositions.add(posk);
                    // blockedPositionsRandom.add(posk_);
                }
            }
            X = pos.get(0);
            Y = pos.get(1);
            ArrayList<Integer> asset = new ArrayList<>(); // (ASSET TYPE),(ASSETINDEX),(X CENTER),(Y
            // CENTER)
            asset.add(1); // Type stones
            asset.add(rand.nextInt(3));
            asset.add((X) * 64); // Add a random X coordinate
            asset.add((Y) * 64); // Add a random Y coordinate
            asset.add(id); // Add a id
            asset.add(grabZIndex()); // Add a z-index
            asset.add(3); // Add width
            asset.add(3); // Add height
            asset.add(50); // Add a radius
            assets.get(1).add(asset); // Add asset to layer 1
        }
        return assets;
    }

    public int ZINDEX = 0;

    public int grabZIndex() {
        ZINDEX++;
        return ZINDEX;
    }

    // -------------- Drawing related --------------

    // public void loadAssetsSprites() {
    // sprites
    // }

    public ArrayList<ArrayList<ArrayList<Integer>>> getAssetsInCamera(int coords[], UI source, int proximity) {
        ArrayList<ArrayList<ArrayList<Integer>>> as = new ArrayList<>();

        int width = source.getWidth();
        int height = source.getHeight();

        int minWidth = coords[0] + -proximity;
        int maxWidth = coords[0] + width + proximity;
        int minHeight = coords[1] + -proximity;
        int maxHeight = coords[1] + height + proximity;
        for (int l = 0; l < MapAssets.size(); l++) { // Go through layers
            as.add(new ArrayList<ArrayList<Integer>>());
            for (ArrayList<Integer> i : MapAssets.get(l)) {
                if (i.get(2) > minWidth && i.get(2) < maxWidth && i.get(3) > minHeight && i.get(3) < maxHeight) {
                    as.get(l).add(i);
                }
            }
        }
        return as;
    }

    public void drawAssets(Graphics g, UI source, ArrayList<ArrayList<ArrayList<Integer>>> as) throws Exception {
        for (ArrayList<Integer> i : as.get(0)) { // Draw layer 0
            if (i.get(1) == -1) {
                g.drawImage(GrassAsset, i.get(2) - source.camera[0], i.get(3) - source.camera[1],
                        source);
            } else if (i.get(1) == -2) {
                g.drawImage(TallGrassAsset, i.get(2) - source.camera[0], i.get(3) - source.camera[1],
                        source);
            } else {
                g.drawImage(assetImages[i.get(0)][i.get(1)], i.get(2) - source.camera[0], i.get(3) - source.camera[1],
                        source);
            }
        }
        for (ArrayList<Integer> i : as.get(1)) { // Draw layer 1
            g.setColor(Color.blue);
            g.drawImage(assetImages[i.get(0)][i.get(1)],
                    i.get(2) - source.camera[0],
                    i.get(3) - source.camera[1],
                    source);
            if (game.devMode) {
                g.setColor(Color.PINK);
                g.drawString(String.valueOf(i.get(4)), i.get(2) - source.camera[0], i.get(3) - source.camera[1]);
            }
            g.setColor(Color.black);
        }
        for (ArrayList<Integer> i : as.get(2)) { // Draw layer 2
            g.setColor(Color.blue);
            g.drawImage(assetImages[i.get(0)][i.get(1)],
                    i.get(2) - source.camera[0],
                    i.get(3) - source.camera[1],
                    source);

            g.setColor(Color.black);
        }
    }
}
