package zombro.zombroServer;

import java.util.ArrayList;
import java.util.List;

import zombro.Player;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.*;
import java.nio.charset.StandardCharsets;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessagePacker;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.core.buffer.MessageBufferOutput;

public class ServerWorld extends Thread {
    public String serverName = "Server";
    public boolean stop = false;

    public int ServerID;

    int lastId = 0;
    int lastPlayerId = 0;

    public ArrayList<ArrayList<ArrayList<Integer>>> assets;

    public JSONObject Blocks = new JSONObject();
    public JSONArray blockedPositions = new JSONArray();

    public int mapSize;
    public int amountOfTreeGroups;
    public int amountOfBigRocks;
    public int amountOfSmallRocks;
    public int amountOfGrass;

    public ArrayList<ServerPlayer> players = new ArrayList<ServerPlayer>();

    public ServerWorld(int ServerID) {
        this.ServerID = ServerID;
    }

    public void run() {
        int port = 3031;
        try {
            while (!stop) {
                if (players != null) {
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    MessagePacker packer = MessagePack.newDefaultPacker(outputStream);

                    packer.packString("serverworldupdate");
                    packer.packInt(ServerID);
                    packer.packArrayHeader(players.size());
                    for (ServerPlayer player : players) {
                        packer.packInt(player.id); // Player ID
                        packer.packInt(player.position[0]); // Player x
                        packer.packInt(player.position[1]); // Player y
                        packer.packInt(player.getCoins());
                        packer.packInt(player.getHealth());
                        packer.packInt(player.getShield());
                        packer.packInt(player.poseSt);
                        packer.packInt(player.walkingstate);
                        packer.packBoolean(player.walking);
                        packer.packBoolean(player.isAllowedToBuild);
                        packer.packBoolean(player.isAllowedToHarvest);
                        packer.packBoolean(player.isAllowedToMine);
                        packer.packString(player.username);
                    }
                    packer.close();
                    byte[] bytePacket = outputStream.toByteArray();

                    for (ServerPlayer player : players) {

                        DatagramPacket responsePacket = new DatagramPacket(
                                bytePacket,
                                bytePacket.length,
                                player.getAddress(),
                                player.getPort());
                        player.socket.send(responsePacket);
                    }
                }
                Thread.sleep(10);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int grabId() {
        lastId++;
        return lastId;
    }

    public int getNextPlayerId() {
        lastPlayerId++;
        return lastPlayerId;
    }
}
