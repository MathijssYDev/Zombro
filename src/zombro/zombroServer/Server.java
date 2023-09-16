package zombro.zombroServer;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessagePacker;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.core.buffer.MessageBufferOutput;

public class Server {
    int lastServerWorldID = 0;
    ArrayList<ServerWorld> Servers = new ArrayList<ServerWorld>();
    Path logFile;

    public static void main(String[] args) {
        Server server = new Server();
    }

    public Server() {
        openlog();
        UDPServer udpserver = new UDPServer(this);
    }

    public void addServerWorld(int serverworldid) {
        ServerWorld svrwrld = new ServerWorld(serverworldid);
        System.out.println(serverworldid);
        svrwrld.start();
        Servers.add(serverworldid - 1, svrwrld);
    }

    public void openlog() {
        try {
            logFile = Files.createTempFile(null, null);
            Files.write(logFile, "[Server] Initialing Server\n".getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.APPEND);
            System.out.println("Made log file: " + logFile.toUri().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void log(String source, String sourceSub, String type, String message) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        try {
            String msg = "[" + source + "] [" + sourceSub + "] [" + dtf.format(now) + "] ["
                    + String.valueOf(System.nanoTime()) + "] [" + type + "] [" + message.replaceAll("\n", "")
                    + "]";
            Files.write(logFile, msg.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);

            System.out.println(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getNewServerWorldID() {
        lastServerWorldID++;
        return lastServerWorldID;
    }

}
