package zombro.zombroServer;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessagePacker;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.core.buffer.MessageBufferOutput;

import zombro.Player;
import zombro.UI.Assets;

public class UDPServer {
    String blockedIPs[] = {};

    Server server;

    public UDPServer(Server server) {
        this.server = server;
        int port = 27333;
        try {
            DatagramSocket socket = new DatagramSocket(port);
            System.out.println("UDP Server is listening on port " + port);

            byte[] receiveBuffer = new byte[1024];

            while (true) {
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                socket.receive(receivePacket);

                String IP_CLIENT = (String) receivePacket.getAddress().getHostAddress();

                String receivedData = new String(receivePacket.getData(), StandardCharsets.UTF_8);
                if (!receivedData.contains("updateclientplayerorld")) {
                    server.log("Server", "UDPServer", "RECIEVED",
                            "Recieved data from: " + IP_CLIENT
                                    + ", Message: "
                                    + receivedData);
                }
                MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(receivePacket.getData());
                String header = unpacker.unpackString();
                if (header.equals("handshake")) {
                    Handshake(IP_CLIENT, unpacker, receivePacket, socket);
                }
                if (header.equals("createserverworld")) {
                    CreateServerWorld(IP_CLIENT, unpacker, receivePacket, socket);
                }
                if (header.equals("addplayertoserverworld")) {
                    addPlayerToServerWorld(IP_CLIENT, unpacker, receivePacket, socket);
                }
                if (header.equals("getallservers")) {
                    GetAllServers(IP_CLIENT, unpacker, receivePacket, socket);
                }
                if (header.equals("getassetsofserverworld")) {
                    GetAssetsOfServerWorld(IP_CLIENT, unpacker, receivePacket, socket);
                }
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            server.log("Server", "UDPServer", "ERROR",
                    "Encounterd a error while in the Server loop | StackTrace: "
                            + sw.toString() + " > " + e.getMessage());
        }
    }

    public void GetAllServers(String IP_CLIENT, MessageUnpacker message, DatagramPacket receivePacket,
            DatagramSocket socket) {
        try {
            ArrayList<ServerWorld> servers = server.Servers;

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MessagePacker packer = MessagePack.newDefaultPacker(outputStream);
            packer.packString("allservers");
            packer.packArrayHeader(servers.size());
            for (ServerWorld serverworld : servers) {
                packer.packInt(serverworld.ServerID);
                packer.packString(serverworld.serverName);
                packer.packInt(serverworld.players.size());
            }
            packer.close();
            byte[] bytePacket = outputStream.toByteArray();

            DatagramPacket responsePacket = new DatagramPacket(
                    bytePacket,
                    bytePacket.length,
                    receivePacket.getAddress(),
                    receivePacket.getPort());

            socket.send(responsePacket);

            return;
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            server.log("Server", "UDPServer", "ERROR",
                    "Encounterd a error while handeling GetAllServers | StackTrace: "
                            + sw.toString() + " > " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void GetAssetsOfServerWorld(String IP_CLIENT, MessageUnpacker message,
            DatagramPacket receivePacket,
            DatagramSocket socket) {
        try {
            message.unpackInt();
            // ServerWorld serverworld = server.Servers.get(message.unpackInt());
            ServerWorld serverworld = server.Servers.get(0);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MessagePacker packer = MessagePack.newDefaultPacker(outputStream);
            packer.packString("assetsofserverworld");
            packer.packString(String.valueOf(serverworld.assets.size()));
            for (int i = 0; i < serverworld.assets.size(); i++) {
                ArrayList<ArrayList<Integer>> assets = serverworld.assets.get(i);
                packer.packString(String.valueOf(assets.size()));
                for (int i_ = 0; i_ < assets.size(); i_++) {
                    ArrayList<Integer> assets_ = assets.get(i_);
                    packer.packString(String.valueOf(assets_.size()));
                    for (int i__ = 0; i__ < assets_.size(); i__++) {
                        packer.packString(String.valueOf(assets_.get(i__)));
                    }
                }
            }
            packer.close();
            byte[] bytePacket = outputStream.toByteArray();

            DatagramPacket responsePacket = new DatagramPacket(
                    bytePacket,
                    bytePacket.length,
                    receivePacket.getAddress(),
                    receivePacket.getPort());

            socket.send(responsePacket);

            return;
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            server.log("Server", "UDPServer", "ERROR",
                    "Encounterd a error while handeling GetAssetsOfServerWorld | StackTrace: "
                            + sw.toString() + " > " + e.getMessage());
            e.printStackTrace();
        }
        return;
    }

    public void addPlayerToServerWorld(String IP_CLIENT, MessageUnpacker message, DatagramPacket receivePacket,
            DatagramSocket socket) {
        try {
            int serverworldid = message.unpackInt();
            String username = message.unpackString();
            ServerWorld serverworld;
            try {
                serverworld = server.Servers.get(serverworldid);
            } catch (Exception e) {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                MessagePacker packer = MessagePack.newDefaultPacker(outputStream);
                packer.packString("serverworlderror");
                packer.packString("Couldn't find specified serverworld!");
                packer.close();
                byte[] bytePacket = outputStream.toByteArray();

                DatagramPacket responsePacket = new DatagramPacket(
                        bytePacket,
                        bytePacket.length,
                        receivePacket.getAddress(),
                        receivePacket.getPort());

                socket.send(responsePacket);

                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                server.log("Server", "UDPServer", "ERROR",
                        "Encounterd a error while trying to get a world | StackTrace: "
                                + sw.toString() + " > " + e.getMessage());
                e.printStackTrace();
                return;
            }
            int playerId = serverworld.getNextPlayerId();
            ServerPlayer player = new ServerPlayer(username, playerId, receivePacket.getAddress(),
                    receivePacket.getPort(), socket);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MessagePacker packer = MessagePack.newDefaultPacker(outputStream);
            packer.packString("playerid");
            packer.packInt(playerId);
            packer.close();
            byte[] bytePacket = outputStream.toByteArray();

            DatagramPacket responsePacket = new DatagramPacket(
                    bytePacket,
                    bytePacket.length,
                    receivePacket.getAddress(),
                    receivePacket.getPort());

            socket.send(responsePacket);
            serverworld.players.add(playerId - 1, player);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            server.log("Server", "UDPServer", "ERROR",
                    "Encounterd a error while handeling addPlayerToServerWorld | StackTrace: "
                            + sw.toString() + " > " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void CreateServerWorld(String IP_CLIENT, MessageUnpacker message, DatagramPacket receivePacket,
            DatagramSocket socket) {
        try {
            int mapSizeBlock = message.unpackInt();
            int amountOfTreeGroups = message.unpackInt();
            int amountOfBigRocks = message.unpackInt();
            int amountOfSmallRocks = message.unpackInt();
            int amountOfGrass = message.unpackInt();

            Assets assetsNew = new Assets();
            assetsNew.mapSize = mapSizeBlock * 64;
            assetsNew.amountOfTreeGroups = amountOfTreeGroups;
            assetsNew.amountOfBigRocks = amountOfBigRocks;
            assetsNew.amountOfSmallRocks = amountOfSmallRocks;
            assetsNew.amountOfGrass = amountOfGrass;

            int serverWorldID = server.getNewServerWorldID();

            server.log("Server", "UDPServer", "RECIEVED", "CreateServerWorld from: " + IP_CLIENT
                    + ", Data: mapSizeBlock:" + mapSizeBlock + ", amountOfTreeGroups:" + amountOfTreeGroups
                    + ", amountOfBigRocks:" + amountOfBigRocks + ", amountOfSmallRocks:" + amountOfSmallRocks
                    + ", amountOfGrass:" + amountOfGrass + ", serverID:" + serverWorldID);

            server.addServerWorld(serverWorldID);
            ServerWorld serverworld = server.Servers.get(server.Servers.size() - 1);
            serverworld.assets = assetsNew.randomAssets(serverworld);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MessagePacker packer = MessagePack.newDefaultPacker(outputStream);
            packer.packString("serverworldid");
            packer.packInt(serverWorldID);
            packer.close();
            byte[] bytePacket = outputStream.toByteArray();

            DatagramPacket responsePacket = new DatagramPacket(
                    bytePacket,
                    bytePacket.length,
                    receivePacket.getAddress(),
                    receivePacket.getPort());

            socket.send(responsePacket);

        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            server.log("Server", "UDPServer", "ERROR",
                    "Encounterd a error while handeling CreateServerWorld | StackTrace: "
                            + sw.toString() + " > " + e.getMessage());
            e.printStackTrace();
        }

    }

    public void Handshake(String IP_CLIENT, MessageUnpacker message, DatagramPacket receivePacket,
            DatagramSocket socket) {
        try {
            if (Arrays.toString(blockedIPs).contains(IP_CLIENT)) {
                server.log("Server", "UDPServer", "ACTION",
                        "Refused to handshake with: " + IP_CLIENT
                                + " , Reason: CLIENT_IP was found in blocked IP's");
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                MessagePacker packer = MessagePack.newDefaultPacker(outputStream);
                packer.packString("handshakedeclined");
                packer.packString("This IP address has been blocked by this server!");
                packer.close();
                byte[] bytePacket = outputStream.toByteArray();

                DatagramPacket responsePacket = new DatagramPacket(
                        bytePacket,
                        bytePacket.length,
                        receivePacket.getAddress(),
                        receivePacket.getPort());

                socket.send(responsePacket);
                return;
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MessagePacker packer = MessagePack.newDefaultPacker(outputStream);
            packer.packString("handshakeaccepted");
            packer.close();
            byte[] bytePacket = outputStream.toByteArray();

            DatagramPacket responsePacket = new DatagramPacket(
                    bytePacket,
                    bytePacket.length,
                    receivePacket.getAddress(),
                    receivePacket.getPort());

            socket.send(responsePacket);

            server.log("Server", "UDPServer", "SEND",
                    "Accepted handshake from: " + IP_CLIENT
                            + ", Send message: "
                            + new String(outputStream.toByteArray(), StandardCharsets.UTF_8));
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            server.log("Server", "UDPServer", "ERROR",
                    "Encounterd a error while handeling handshake | StackTrace: "
                            + sw.toString() + " > " + e.getMessage());
        }
    }
}