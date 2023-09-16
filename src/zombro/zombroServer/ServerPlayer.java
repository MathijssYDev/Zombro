package zombro.zombroServer;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.net.DatagramSocket;
import java.net.InetAddress;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.awt.Image;
import zombro.UI.UI;

public class ServerPlayer {
    InetAddress ip;
    int port;
    DatagramSocket socket;

    int[] position = { 0, 0 };

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

    String username;
    int id;
    int team;

    public ServerPlayer(String username, int id, InetAddress ip, int port, DatagramSocket socket) {
        this.username = username;
        this.id = id;
        this.team = -1;
        this.ip = ip;
        this.port = port;
        this.socket = socket;
    }

    public void setCoins(int c) {
        this.Coins = c;
    }

    public int getCoins() {
        return this.Coins;
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

    public InetAddress getAddress() {
        return ip;
    }

    public int getPort() {
        return port;
    }
}