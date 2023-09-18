package zombro;

import zombro.UI.UI;

import java.util.ArrayList;
import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;

import zombro.UI.Assets;
import zombro.UI.Stats;

public class draw extends Thread {
    int cameraMoveSteps = 100; // pixels

    private int frameCount = 0;
    boolean done = false;

    boolean Moved = true;

    UI gui;
    Assets assets;
    double targetFPS = 60.0;
    double nsPerFrame = 1_000_000_000.0 / targetFPS;
    int lastFPS = (int) targetFPS;

    public int FPS;

    Stats status;

    double dt = 0;

    public draw(UI gui, Assets assets) {
        this.assets = assets;
        this.gui = gui;

    }

    public void run() {
        status = new Stats(gui.game);
        try {
            long startTime = System.nanoTime();
            long lastTime = startTime;
            while (!gui.game.close) {
                long now = System.nanoTime();
                long elapsed = now - lastTime;
                if (elapsed >= nsPerFrame) {
                    dt = (double) (System.nanoTime() - startTime) / 1_000_000_000;
                    startTime = System.nanoTime();
                    lastTime = now;
                    if (gui.game.running) { // Check if player is running
                        cameraMoveSteps = 350;
                    } else {
                        cameraMoveSteps = 150;
                    }
                    ArrayList<ArrayList<ArrayList<Integer>>> ProximityAssets = assets.getAssetsInCamera(gui.camera, gui,
                            500);
                    // ArrayList<ArrayList<ArrayList<Integer>>> NewAssets = new ArrayList<>();
                    // for (ArrayList<ArrayList<Integer>> i : ProximityAssets) {
                    // NewAssets.add(i);
                    // }
                    // renderFrame(NewAssets, FPS);
                    renderFrame(ProximityAssets, FPS);
                    frameCount++;
                    if (frameCount % targetFPS == 0) {
                        lastFPS = Math.round((float) 1_000_000_000.0 / elapsed);
                        FPS = lastFPS;
                    }
                }
                sleep(0);
            }
            this.Terminate();
        } catch (InterruptedException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            gui.game.log("Game", "Draw", "ERROR",
                    "Error while trying to loop | StackTrace: "
                            + sw.toString() + " > " + e.getMessage());
        }
    }

    private void renderFrame(ArrayList<ArrayList<ArrayList<Integer>>> ProximityAssets, int FPS_) {
        try {
            Graphics2D g = (Graphics2D) gui.bufferStrategy.getDrawGraphics();
            g.clearRect(0, 0, gui.getWidth(), gui.getHeight());
            g.setColor(Color.decode("#658846"));
            g.fillRect(0 - gui.camera[0], 0 - gui.camera[1], gui.game.mapSize, gui.game.mapSize); // Draw game Field
            g.setColor(Color.black);
            g.setStroke(new BasicStroke(2));
            g.drawRect(0 - gui.camera[0], 0 - gui.camera[1], gui.game.mapSize, gui.game.mapSize); // Draw game border

            g.setColor(Color.black);

            // Draw Grid
            int width = gui.getWidth();
            int height = gui.getHeight();

            int minWidth = gui.camera[0] + -100;
            int maxWidth = gui.camera[0] + width + 100;
            int minHeight = gui.camera[1] + -100;
            int maxHeight = gui.camera[1] + height + 100;
            if (gui.game.gridMode || gui.game.buildmode) {
                g.setColor(Color.decode("#57733e"));
                for (int k = 0; k < gui.game.mapSizeBlock; k++) {
                    for (int l = 0; l < gui.game.mapSizeBlock; l++) {
                        int x = k * gui.game.sizeOfBlock;
                        int y = l * gui.game.sizeOfBlock;
                        if (x > minWidth && x < maxWidth && y > minHeight && y < maxHeight) {
                            g.drawRect(x - gui.camera[0], y
                                    - gui.camera[1],
                                    gui.game.sizeOfBlock,
                                    gui.game.sizeOfBlock);
                        }
                    }
                }
            }
            moveCamera(ProximityAssets, g);
            g.setColor(Color.black);

            // Draw Assets
            try {
                assets.drawAssets(g, gui, ProximityAssets);
                gui.game.build.drawBuildings(g);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // Draw Players
            for (Player p : gui.game.players) {
                p.drawPlayer(g, this, gui);
            }
            // g.drawString("FPS: " + String.valueOf(1 / dt), 15, 60);
            // Draw UI

            status.drawUI(g, gui);
            gui.inv.drawInventory(g, gui, gui.game);
            g.dispose();
            gui.bufferStrategy.show();
        } catch (IllegalStateException e) {
            gui.createBufferStrategy(2);
            gui.bufferStrategy = gui.getBufferStrategy();
            System.out.println("Error while trying to render a frame, (this may be caused by moving the window): " + e);
        }
    }

    public void Terminate() {
        done = true;
    }

    int lastUpdate = 0;

    public void getCloseObject() {

    }

    public void moveCamera(ArrayList<ArrayList<ArrayList<Integer>>> ProximityAssets, Graphics2D g) {
        try {
            if (gui.inventory) {
                return;
            }
            if (gui.movement[0] || gui.movement[1] || gui.movement[2] || gui.movement[3]) {
                if (gui.movement[0]) {
                    if (!(gui.camera[1] < (0 - gui.getHeight() / 2))) {
                        gui.cameraDetailed[1] -= cameraMoveSteps * dt;
                    }
                }
                if (gui.movement[1]) {
                    if (!(gui.camera[0] < 0 - (gui.getWidth() / 2))) {
                        gui.cameraDetailed[0] -= cameraMoveSteps * dt;
                    }
                }
                if (gui.movement[2]) {
                    if (!(gui.camera[0] > gui.game.mapSize - (gui.getWidth() / 2))) {
                        gui.cameraDetailed[0] += cameraMoveSteps * dt;
                    }
                }
                if (gui.movement[3]) {
                    if (!(gui.camera[1] > gui.game.mapSize - (gui.getHeight() / 2))) {
                        gui.cameraDetailed[1] += cameraMoveSteps * dt;
                    }
                }
                gui.camera[0] = Math.round(gui.cameraDetailed[0]);
                gui.camera[1] = Math.round(gui.cameraDetailed[1]);
            }

            int r_player = 32;

            ArrayList<ArrayList<ArrayList<Integer>>> o = new ArrayList<>();
            for (int l = 0; l < ProximityAssets.size(); l++) { // Go through layers
                if (l == 0)
                    continue; // Layer 0 has no interaction...
                o.add(new ArrayList<ArrayList<Integer>>());
                for (ArrayList<Integer> i : ProximityAssets.get(l)) {
                    int Asset_W = assets.assetImages[i.get(0)][i.get(1)].getWidth(gui);
                    int Asset_H = assets.assetImages[i.get(0)][i.get(1)].getHeight(gui);
                    int Asset_X = i.get(2) - gui.camera[0];
                    int Asset_Y = i.get(3) - gui.camera[1];
                    int Asset_X_Origin = Asset_X + Asset_W / 2;
                    int Asset_Y_Origin = Asset_Y + Asset_H / 2;
                    int Asset_X_Origin_real = i.get(2) + Asset_W / 2;
                    int Asset_Y_Origin_real = i.get(3) + Asset_H / 2;
                    int Asset_R = i.get(8);

                    int a = Math.abs((gui.getWidth() / 2) - Asset_X_Origin);
                    int b = Math.abs((gui.getHeight() / 2) - Asset_Y_Origin);
                    double c = Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
                    if (gui.game.devMode) {
                        g.setStroke(new BasicStroke(1));
                        g.setColor(Color.green);
                        g.drawRect(Asset_X, Asset_Y, Asset_W, Asset_H);
                        g.setColor(Color.red);
                        g.drawOval(Asset_X_Origin - Asset_R, Asset_Y_Origin - Asset_R, Asset_R * 2, Asset_R * 2);
                        g.setColor(Color.blue);
                        g.setStroke(new BasicStroke(1));
                        g.drawLine((gui.getWidth() / 2), (gui.getHeight() / 2), Asset_X_Origin, Asset_Y_Origin);
                        g.setColor(Color.red);
                        g.drawOval((gui.getWidth() / 2) - r_player, (gui.getHeight() / 2) - r_player, r_player * 2,
                                r_player * 2);
                    }
                    if (c <= Asset_R + r_player + 50) {
                        o.get(l - 1).add(i);
                    }
                    if (c <= Asset_R + r_player + 50) {
                        double d = Math.toDegrees(Math.acos(a / c));
                        double d_;
                        int x = 1;
                        int y = 1;
                        int direction = 0; // 0 = top left, 1 = top right, 2 = bottom left, 3 is bottom right
                        if ((gui.getWidth() / 2) >= Asset_X_Origin && (gui.getHeight() / 2) >= Asset_Y_Origin) {
                            direction = 3;
                            x = 1;
                            y = 1;
                        }
                        if ((gui.getWidth() / 2) <= Asset_X_Origin && (gui.getHeight() / 2) >= Asset_Y_Origin) {
                            direction = 2;
                            x = -1;
                            y = 1;
                        }
                        if ((gui.getWidth() / 2) >= Asset_X_Origin && (gui.getHeight() / 2) <= Asset_Y_Origin) {
                            direction = 0;
                            x = 1;
                            y = -1;
                        }
                        if ((gui.getWidth() / 2) <= Asset_X_Origin && (gui.getHeight() / 2) <= Asset_Y_Origin) {
                            direction = 1;
                            x = -1;
                            y = -1;
                        }
                        if ((direction + 1) % 2 == 1) {
                            d_ = direction * 90 + d;
                        } else {
                            d_ = ((direction + 1) * 90) - d;
                        }
                        double hitDistance = c - Asset_R - r_player;
                        if (hitDistance <= 0) {
                            double nc = r_player + Asset_R - c;

                            double ap = -Math.sin(Math.toRadians(d_)) * Asset_R;
                            double bp = Math.cos(Math.toRadians(d_)) * Asset_R;

                            double deltaX = Math.cos(Math.toRadians(d_)) * nc;
                            double deltaY = Math.sqrt(Math.pow(nc, 2) - Math.pow(deltaX, 2)) * y;

                            if (gui.game.devMode) {
                                g.fillOval((int) Asset_X_Origin + (int) deltaX - 5,
                                        (int) Asset_Y_Origin + (int) deltaY - 5,
                                        10,
                                        10);
                                g.fillOval((int) bp + Asset_X_Origin - 5, (int) ap + Asset_Y_Origin - 5, 10,
                                        10);
                                g.fillOval(Asset_X_Origin - 10, Asset_Y_Origin - 10, 20, 20);
                            }
                            gui.cameraDetailed[0] += deltaX;
                            gui.cameraDetailed[1] += deltaY;
                            gui.camera[0] = Math.round(gui.cameraDetailed[0]);
                            gui.camera[1] = Math.round(gui.cameraDetailed[1]);
                        }
                        if (gui.game.devMode) {
                            g.setColor(Color.red);
                            if (hitDistance < 0) {
                                g.setStroke(new BasicStroke(5));
                            } else {
                                g.setColor(Color.orange);
                                g.setStroke(new BasicStroke(3));
                            }
                            g.drawLine((gui.getWidth() / 2), (gui.getHeight() / 2), Asset_X_Origin, Asset_Y_Origin);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
