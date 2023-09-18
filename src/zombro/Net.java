package zombro;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessagePacker;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.core.buffer.MessageBufferOutput;

import zombro.zombroServer.ServerPlayer;
import zombro.zombroServer.ServerWorld;

public class Net extends Thread {
    DatagramSocket socket;
    InetAddress receiverAddress;

    Game game;

    public Net(Game game) {
        this.game = game;
    }

    public void run() {
        try {
            byte[] receiveBuffer = new byte[60000];

            while (true) {
                socket.setSoTimeout(20000);
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                socket.receive(receivePacket);

                String IP_SERVER = (String) receivePacket.getAddress().getHostAddress();
                MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(receivePacket.getData());
                String header = unpacker.unpackString();
                if (header.equals("serverworldupdate")) {
                    ServerWorldUpdate(IP_SERVER, unpacker, receivePacket, socket);
                }
                sleep(10);
            }
        } catch (java.net.SocketTimeoutException e) {
            System.out.println("Timeout");
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            game.log("Game", "NET", "ERROR",
                    "Encounterd a error while in the Server loop | StackTrace: "
                            + sw.toString() + " > " + e.getMessage());
        }
    }

    String ip = "";
    int port = 27333;

    public boolean ServerWorldUpdate(String IP_CLIENT, MessageUnpacker message,
            DatagramPacket receivePacket,
            DatagramSocket socket) {
        try {
            int serverworldid = message.unpackInt();
            int playersAmount = message.unpackArrayHeader();

            for (int pi = 0; pi < playersAmount; pi++) {
                int playerid = message.unpackInt(); // Player ID
                int mainplayerid = game.getMainPlayer().id;
                int playerx = message.unpackInt(); // Player x
                int playery = message.unpackInt(); // Player y
                int playercoins = message.unpackInt();
                int playerhealth = message.unpackInt();
                int playershield = message.unpackInt();
                int playerposest = message.unpackInt();
                int playerwalkingstate = message.unpackInt();
                boolean playerwalking = message.unpackBoolean();
                boolean playerisallowedtobuild = message.unpackBoolean();
                boolean playerisallowedtoharvest = message.unpackBoolean();
                boolean playerisallowedtomine = message.unpackBoolean();
                String playerusername = message.unpackString();

                Player targetPlayer = game.players.get(game.players.size() - 1);

                boolean found = false;
                for (Player p : game.players) {
                    if (p.id == playerid) {
                        targetPlayer = p;
                        found = true;
                    }
                }
                if (!found) {
                    game.players.add(new Player(false, game, playerusername, playerid, 0));
                    targetPlayer = game.players.get(game.players.size() - 1);
                }

                if (playerid == mainplayerid) {
                    Player mainplr = game.getMainPlayer();
                    mainplr.setHealth((short) playerhealth);
                    mainplr.setCoins(playercoins);
                    mainplr.setShield((short) playershield);
                }

                try {
                    targetPlayer.position[0] = playerx;
                    targetPlayer.position[1] = playery;
                } catch (Exception e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                MessagePacker packer = MessagePack.newDefaultPacker(outputStream);
                packer.packString("updateclientplayer");
                packer.close();
                byte[] bytePacket = outputStream.toByteArray();

                DatagramPacket responsePacket = new DatagramPacket(
                        bytePacket,
                        bytePacket.length,
                        receivePacket.getAddress(),
                        receivePacket.getPort());

                socket.send(responsePacket);
                // game.assets.MapAssets = GetAssetsOfServerWorld(serverworldid);
            }
            return true;
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            game.log("Game", "Net", "ERROR",
                    "Encounterd a error while handeling a ServerWorldUpdate | StackTrace: "
                            + sw.toString() + " > " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public ArrayList<ArrayList<ArrayList<Integer>>> GetAssetsOfServerWorld(int serverworldid) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MessagePacker packer = MessagePack.newDefaultPacker(outputStream);
            packer.packString("getassetsofserverworld");
            packer.packInt(serverworldid);
            packer.close();
            byte[] bytePacket = outputStream.toByteArray();

            DatagramPacket responsePacket = new DatagramPacket(
                    bytePacket,
                    bytePacket.length,
                    receiverAddress, port);

            socket.send(responsePacket);
            byte[] receiveBuffer = new byte[60000];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            socket.receive(receivePacket);
            MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(receivePacket.getData());
            String header = unpacker.unpackString();
            if (header.equals("assetsofserverworld")) {
                ArrayList<ArrayList<ArrayList<Integer>>> assets = new ArrayList<ArrayList<ArrayList<Integer>>>();

                int header1 = Integer.parseInt(unpacker.unpackString());
                for (int i = 0; i < header1; i++) {
                    int header2 = Integer.parseInt(unpacker.unpackString());
                    assets.add(new ArrayList<ArrayList<Integer>>());
                    for (int i_ = 0; i_ < header2; i_++) {
                        int header3 = Integer.parseInt(unpacker.unpackString());
                        assets.get(i).add(new ArrayList<Integer>());
                        for (int i__ = 0; i__ < header3; i__++) {
                            assets.get(i).get(i_).add(Integer.parseInt(unpacker.unpackString()));
                        }
                    }
                }
                return assets;
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            game.log("Game", "Net", "TIMEOUT",
                    "(NO RESPONSE) A timeout (2500ms) has occurred while trying to getassetsofserverworld on: "
                            + receiverAddress);
            return null;
        }
    }

    public boolean addPlayerToServerWorld(int serverworldid, String username) {
        try {
            socket.setSoTimeout(2500);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            MessagePacker packer = MessagePack.newDefaultPacker(outputStream);
            packer.packString("addplayertoserverworld");
            packer.packInt(serverworldid);
            packer.packString(username);
            packer.close();
            byte[] bytePacket = outputStream.toByteArray();
            DatagramPacket packet = new DatagramPacket(bytePacket, bytePacket.length, receiverAddress, port);

            socket.send(packet);
            game.log("Game", "Net", "SEND",
                    "addplayertoserverworld, server: " + receiverAddress + ", message: "
                            + new String(outputStream.toByteArray(), StandardCharsets.UTF_8));

            byte[] receiveBuffer = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            socket.receive(receivePacket);
            MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(receivePacket.getData());
            String header = unpacker.unpackString();
            if (header.equals("playerid")) {
                int playerid = unpacker.unpackInt();
                game.getMainPlayer().id = serverworldid;
                game.log("Game", "Net", "RECIEVED",
                        "playerid: " + String.valueOf(playerid) + ", server: " + receiverAddress);
            }
            return true;
        } catch (Exception e) {
            game.log("Game", "Net", "TIMEOUT",
                    "(NO RESPONSE) A timeout (2500ms) has occurred while trying to addplayertoserverworld on: "
                            + receiverAddress);
            return false;
        }
    }

    public Object createServerWorld() {
        try {
            socket.setSoTimeout(2500);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            MessagePacker packer = MessagePack.newDefaultPacker(outputStream);
            packer.packString("createserverworld");

            // Paramaters to send
            packer.packInt(game.mapSizeBlock);
            packer.packInt(game.assets.amountOfTreeGroups);
            packer.packInt(game.assets.amountOfBigRocks);
            packer.packInt(game.assets.amountOfSmallRocks);
            packer.packInt(game.assets.amountOfGrass);

            packer.close();
            byte[] bytePacket = outputStream.toByteArray();
            DatagramPacket packet = new DatagramPacket(bytePacket, bytePacket.length, receiverAddress, port);

            socket.send(packet);
            game.log("Game", "Net", "SEND",
                    "createserverworld, server: " + receiverAddress + ", message: "
                            + new String(outputStream.toByteArray(), StandardCharsets.UTF_8));

            byte[] receiveBuffer = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            socket.receive(receivePacket);
            MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(receivePacket.getData());
            String header = unpacker.unpackString();
            int serverworldid = -1;
            if (header.equals("serverworldid")) {
                serverworldid = unpacker.unpackInt();
                game.log("Game", "Net", "RECIEVED",
                        "serverworldid: " + String.valueOf(serverworldid) + ", server: " + receiverAddress);
                return serverworldid;
            }
            return false;
        } catch (Exception e) {
            game.log("Game", "Net", "TIMEOUT",
                    "(NO RESPONSE) A itmeout (2500ms) has occurred while trying to createServerWorld on: "
                            + receiverAddress);
            return false;
        }
    }

    public Object[][] getAllServer() {
        try {
            socket.setSoTimeout(2500);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            MessagePacker packer = MessagePack.newDefaultPacker(outputStream);
            packer.packString("getallservers");
            packer.close();
            byte[] bytePacket = outputStream.toByteArray();
            DatagramPacket packet = new DatagramPacket(bytePacket, bytePacket.length, receiverAddress, port);
            socket.send(packet);
            game.log("Game", "Net", "SEND",
                    "getallservers, server: " + receiverAddress + ", message: "
                            + new String(outputStream.toByteArray(), StandardCharsets.UTF_8));

            byte[] receiveBuffer = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            socket.receive(receivePacket);

            MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(receivePacket.getData());
            String header = unpacker.unpackString();
            if (header.equals("allservers")) {
                int amountOfServers = unpacker.unpackArrayHeader();
                Object[][] servers = new Object[amountOfServers][3];

                for (int pi = 0; pi < amountOfServers; pi++) {
                    servers[pi][0] = unpacker.unpackInt();
                    servers[pi][1] = unpacker.unpackString();
                    servers[pi][2] = unpacker.unpackInt();
                }

                game.log("Game", "Net", "RECIEVED",
                        "allservers, server: " + receiverAddress);
                return servers;
            }
            return null;
        } catch (Exception e) {
            game.log("Game", "Net", "TIMEOUT",
                    "(NO RESPONSE) A itmeout (2500ms) has occurred while trying to createServerWorld on: "
                            + receiverAddress);
            return null;
        }
    }

    public boolean HandShake(String ip) throws Exception {
        this.ip = ip;
        socket = new DatagramSocket();
        receiverAddress = InetAddress.getByName(ip);
        try {
            socket.setSoTimeout(2500);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            MessagePacker packer = MessagePack.newDefaultPacker(outputStream);
            packer.packString("handshake");
            packer.close();
            byte[] bytePacket = outputStream.toByteArray();
            DatagramPacket packet = new DatagramPacket(bytePacket, bytePacket.length, receiverAddress, port);
            socket.send(packet);
            game.log("Game", "Net", "SEND",
                    "Send handshake to server: " + receiverAddress + ", message: "
                            + new String(outputStream.toByteArray(), StandardCharsets.UTF_8));

            byte[] receiveBuffer = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            socket.receive(receivePacket);

            MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(receivePacket.getData());
            String header = unpacker.unpackString();
            switch (header) {
                case "handshakeaccepted":
                    game.log("Game", "Net", "RECIEVED",
                            "Handshake got accepted by server: " + receiverAddress);
                    return true;
                case "handshakedeclined":
                    if (header == "handshakedeclined") {
                        try {
                            String reason = unpacker.unpackString();
                            game.log("Game", "Net", "RECIEVED",
                                    "Handshake got declined: " + receiverAddress + ", reason: " + reason);
                        } catch (Exception e) {
                            StringWriter sw = new StringWriter();
                            e.printStackTrace(new PrintWriter(sw));
                            game.log("Game", "Net", "ERROR",
                                    "Encounterd a error while trying handle a Message from the server:"
                                            + new String(receivePacket.getData(), StandardCharsets.UTF_8)
                                            + " | StackTrace: "
                                            + sw.toString() + " > " + e.getMessage());

                        }
                        return false;
                    }

            }
        } catch (Exception e) {
            game.log("Game", "Net", "TIMEOUT",
                    "(NO RESPONSE) A timeout (2500ms) has occurred while sending a handshake to: " + receiverAddress
                            + ":" + port);
            return false;
        }
        return false;
    }

    public void close() {
        if (socket != null) {
            socket.close();
        }
    }
}