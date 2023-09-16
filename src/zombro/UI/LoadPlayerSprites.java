package zombro.UI;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import java.awt.Image;
import java.awt.image.BufferedImage;

public class LoadPlayerSprites {
    String assetImagesFileNames[][] = {
            { "forwards_eyes_1", "forwards_eyes_2", "forwards_eyes_3", "forwards_eyes_4", "forwards_idle",
                    "forwards_walking_1", "forwards_walking_2" },
            { "left_eyes_1", "left_eyes_2", "left_idle", "left_walking_1", "left_walking_2" },
            { "right_eyes_1", "right_eyes_2", "right_idle", "right_walking_1", "right_walking_2" },
            { "backwards_idle", "backwards_walking_1", "backwards_walking_2" }
    }; // All the sprites file names
    public Image assetImages[][];

    public void loadAssets() {
        System.out.println("Loading player sprites...");
        try {
            String type[] = { "forwards", "left", "right", "backwards" };
            ArrayList<ArrayList<Image>> ImageArray = new ArrayList<>();
            for (int i = 0; i < assetImagesFileNames.length; i++) {
                ImageArray.add(i, new ArrayList<Image>());
                for (int x = 0; x < assetImagesFileNames[i].length; x++) {
                    InputStream u = getClass()
                            .getResourceAsStream(
                                    "/zombro/resources/player/" + type[i] + "/" + assetImagesFileNames[i][x] +
                                            ".png");
                    // InputStream u = getClass()
                    // .getResourceAsStream(
                    // "/zombro/resources/tools/0001.png");
                    BufferedImage img = ImageIO.read(u);
                    // Set the desired scale factor

                    double scale = 3.0; // Change this value as needed

                    // Calculate the new dimensions for the upscaled image
                    int newWidth = (int) (img.getWidth() * scale);
                    int newHeight = (int) (img.getHeight() * scale);

                    // Create a new BufferedImage with the upscaled dimensions
                    BufferedImage upscaledImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);

                    // Perform the upscale operation using nearest-neighbor interpolation
                    for (int y = 0; y < newHeight; y++) {
                        for (int b = 0; b < newWidth; b++) {
                            int originalX = (int) Math.min(b / scale, img.getWidth() - 1);
                            int originalY = (int) Math.min(y / scale, img.getHeight() - 1);
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
            System.out.println("Done loading player sprites!");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error while loading player sprites: " + e);
        }
    }
}
