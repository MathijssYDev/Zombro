package zombro;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.PrintWriter;
import java.io.Serial;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.lang.Exception;
import org.json.simple.JSONObject;

import zombro.UI.UI;
import zombro.items.Items;
import zombro.zombroServer.ServerWorld;
import zombro.UI.Assets;
import zombro.UI.LoadPlayerSprites;
import zombro.UI.menu.Menu;
import zombro.building.building;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

public class Game implements ActionListener { // MouseListener, ActionListener, WindowListener
    int lastId = 0;
    final public int sizeOfBlock = 64;
    public UI gui;

    public int mapSizeBlock = 100;

    public int mapSize = mapSizeBlock * sizeOfBlock;
    LoadPlayerSprites playerSprites = new LoadPlayerSprites();
    Players playersLoop = new Players(this);

    Assets assets;
    public Items items;
    public building build;

    ArrayList<Player> players = new ArrayList<Player>();
    public boolean gridMode = false;
    public boolean buildmode = false;
    public boolean devMode = false;
    public boolean running = false;

    public boolean menuIsOpen = false;
    public Menu menu;
    public boolean close = false;
    Net net;

    Path logFile;

    public Game() {
        openlog();
        // ---------------- Load Items ----------------
        items = new Items(this);
        items.loadTools(this);

        // ---------------- Load Blocks ----------------
        build = new building(this);
        build.loadblocks(this);

        // ---------------- Load Assets ----------------
        assets = new Assets(this, mapSize);
        this.build.blockedPositions = assets.blockedPositionsRandom;

        playersLoop.start();
        System.setProperty("sun.java2d.opengl", "true");

        // ---------------- Initialize Players ----------------

        playerSprites.loadAssets();
        players.add(new Player(true, this, "Zombro", 0, 1));

        // menu = new Menu(this);

        // ---------------- Initialize NET ----------------

        net = new Net(this);
        try {

            if (net.HandShake("localhost")) { // handshake with the server to
                // establish a UDP connection
                Object serverworldid;
                Object[][] servers = net.getAllServer(); // ServerID, ServerName,
                // AmountOfPlayers
                if (servers.length != 0) {
                    serverworldid = (int) servers[0][0];
                    System.out.println("joining pre-existing server world");
                } else {
                    serverworldid = net.createServerWorld(); // create a server world on server
                    System.out.println("creating server world");
                }
                // the server, and get the
                // serverworldID in return from the server
                if (serverworldid.equals(false)) { // If unsuccesful, the
                    // net.createServerWorld function will return
                    // `false`
                    System.out.println("Failed to create Server World");
                } else {
                    net.addPlayerToServerWorld((int) serverworldid - 1, "Zombro"); // If
                    // succesful, add a player to
                    // the world
                    assets.MapAssets = net.GetAssetsOfServerWorld((int) serverworldid - 1);
                    System.out.println(assets.MapAssets);
                    this.gui = new UI(this, assets, items);
                    this.gui.startRenderingGame();
                    net.run(); // start the thread that reads server messages (Player position
                    // updates, block
                    // updates ect)
                    // this.gui = new UI(this, assets, items);
                    // this.gui.startRenderingGame();
                }
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            log("Game", "Net", "ERROR",
                    "Encounterd a error while trying to initialize a handshake with: " + "localhost" + " | StackTrace: "
                            + sw.toString() + " > " + e.getMessage());
        }

        // ---------------- Initialize UI ----------------
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
    }

    public Player getMainPlayer() {
        return players.get(0);
    }

    public int grabId() {
        lastId++;
        return lastId;
    }

    public void openlog() {
        try {
            logFile = Files.createTempFile(null, null);
            Files.write(logFile, "[Game] Initialing game\n".getBytes(StandardCharsets.UTF_8));
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
                    + String.valueOf(System.nanoTime()) + "] [" + type + "] [" + message
                    + "]";
            Files.write(logFile, msg.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);

            System.out.println(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
