package zombro;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.awt.Image;
import zombro.UI.UI;

public class Player {
    // Items inventory;
    int[] position = { 0, 0 };

    int blinkdelay = 2000; // very ms

    short Health = 100;
    short Shield = 20;
    int Coins = 0;
    boolean isAllowedToBuild;
    boolean isAllowedToHarvest;
    boolean isAllowedToMine;
    boolean walking = false;
    int eyeState = 0;
    int poseSt = 3;
    byte walkingstate = 0;

    long lastWalkinUpdate = 0;
    long lastEyeUpdate = 0;
    long lastBodyUpdate = 0;

    String username;
    int id;
    int team;
    Game game;

    JSONObject playerInventory = null;

    Boolean isMain;

    public Player(Boolean isMain, Game game, String username, int id, int team) {
        this.isMain = isMain;
        this.game = game;
        this.username = username;
        this.id = id;
        this.team = team;
    }

    public void updatePlayer() {
        long millis = System.currentTimeMillis();
        int updateTime = 1500;

        if (isMain) {
            if (game.gui.movement[0] == true) { // Forwards
                poseSt = 0;
                eyeState = 0;
            }
            if (game.gui.movement[3] == true) { // Backwards
                poseSt = 3;
                eyeState = 0;
            }
            if (game.gui.movement[1] == true) { // Left
                poseSt = 1;
                eyeState = 0;
            }
            if (game.gui.movement[2] == true) { // Right
                poseSt = 2;
                eyeState = 0;
            }

            if (millis - lastEyeUpdate >= updateTime) {
                lastEyeUpdate = millis;
                eyeState++;
                if (poseSt == 0 && eyeState > 5) {
                    eyeState = 0;
                }
                if (poseSt == 1 && eyeState > 3) {
                    eyeState = 0;
                }
                if (poseSt == 2 && eyeState > 3) {
                    eyeState = 0;
                }
                if (poseSt == 2) {
                    eyeState = -1;
                }
            }
        }
    }

    public void drawPlayer(Graphics g, draw d, UI source) {
        if (isMain) {
            int playerX = source.getWidth() / 2;
            int playerY = source.getHeight() / 2;
            Player plr = game.getMainPlayer();
            Image img = game.playerSprites.assetImages[poseSt][game.playerSprites.assetImages[poseSt].length
                    - walkingstate - 1];
            Font f = new Font("hooge 05_53", Font.PLAIN, 20);
            int w = g.getFontMetrics(f).stringWidth(username);
            g.setColor(Color.WHITE);
            g.setFont(f);
            g.drawString(username, playerX - (w / 2), playerY - img.getHeight(source) / 2);
            g.setColor(Color.black);
            g.drawImage(img, playerX - img.getWidth(source) / 2, playerY -
                    img.getHeight(source) / 2, source);
            // JSONObject obj = plr.getEquipedItem();
            // g.drawImage(game.items.getItemSprite(obj.get("collection"), obj.get("type"),
            // obj.get("name")),
            // playerX - img.getWidth(source) / 2, playerY -
            // img.getHeight(source) / 2,
            // source);
            // if (eyeState != -1) {
            // g.drawImage(game.playerSprites.assetImages[poseSt][eyeState], playerX,
            // playerY, source);
            // }
        } else {
            int playerX = position[0] - source.camera[0];
            int playerY = position[1] - source.camera[1];
            Font f = new Font("hooge 05_53", Font.PLAIN, 20);
            g.setColor(Color.WHITE);
            g.setFont(f);
            g.drawString(username, playerX, playerY);
            g.setColor(Color.black);
            g.drawImage(
                    game.playerSprites.assetImages[poseSt][game.playerSprites.assetImages[poseSt].length
                            - walkingstate
                            - 1],
                    playerX, playerY, source);
        }
    }

    public void giveItem(JSONObject Item) {

    }

    public void removeItem(JSONObject Item) {

    }

    public void setCoins(int c) {
        this.Coins = c;
    }

    public int getCoins() {
        return this.Coins;
    }

    public void setPlayerInventory(JSONObject Inventory) {
        playerInventory = Inventory;
    }

    public JSONObject getPlayerInventory() {
        return playerInventory;
    }

    public void setEquipedItem(JSONObject Item) {

    }

    public JSONObject getEquipedItem() {
        return null;
    }

    public void setHealth(short h) {
        this.Health = h;
    }

    public short getHealth() {
        return this.Health;
    }

    public void setShield(short h) {
        this.Shield = h;
    }

    public short getShield() {
        return this.Shield;
    }

    public void setPosition(int[] p) {
        this.position = p;
    }

    public int[] getPosition() {
        return this.position;
    }

    public boolean isAllowedToBuild() {
        return this.isAllowedToBuild;
    }

    public void setIsAllowedToBuild(boolean v) {
        this.isAllowedToBuild = v;
    }

    public boolean isAllowedToHarvest() {
        return this.isAllowedToHarvest;
    }

    public void isAllowedToHarvest(boolean v) {
        this.isAllowedToHarvest = v;
    }

    public boolean isAllowedToMine() {
        return this.isAllowedToMine;
    }

    public void isAllowedToMine(boolean v) {
        this.isAllowedToMine = v;
    }

}