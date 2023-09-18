package zombro.UI;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.json.simple.JSONObject;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import zombro.draw;
import zombro.items.Items;

import java.awt.image.BufferStrategy;
import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.InputEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import zombro.Game;

public class UI extends JFrame implements KeyListener, MouseListener, MouseMotionListener {
    Assets assets;
    public int camera[] = { 0, 0 };
    public float cameraDetailed[] = { 0, 0 };

    public boolean movement[] = { false, false, false, false };

    public draw drawingThread;
    public BufferStrategy bufferStrategy;
    public Game game;

    public boolean mouseLeft = false;
    public boolean mouseRight = false;
    public boolean mouseMid = false;

    public inventory inv;

    public boolean inventory = false;

    public UI(Game game, Assets assets, Items items) {
        try {
            InputStream logo = getClass()
                    .getResourceAsStream("/zombro/resources/Logo.png");
            Image img = ImageIO.read(logo);
            this.game = game;
            this.assets = assets;
            this.inv = new inventory(this, game);
            JSONObject newInv = game.items.getDefaultPlayerInventory();
            newInv.put("inventory", inv.resetMainPlayerInventory(32, game.items));
            game.getMainPlayer().setPlayerInventory(newInv); // Add default items to main player

            // game.menu.pack(this);
            // pack();
            setIconImage(img);
            setVisible(true);
            createBufferStrategy(2);
            bufferStrategy = getBufferStrategy();
            setTitle("Zombro");
            setSize(1920, 1080);
            setExtendedState(JFrame.MAXIMIZED_BOTH);
            setResizable(true);
            setBackground(Color.decode("#445435"));
            addMouseListener(this);
            addKeyListener(this);
            addMouseMotionListener(this);
            addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    System.out.println("CLOSING...");
                    stopRenderingGame();
                }
            });
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            new Throwable().printStackTrace(new PrintWriter(sw));
            game.log("Game", "Net", "ERROR",
                    "Encounterd a while trying to initialize UI | StackTrace: "
                            + sw.toString() + " > " + e.getMessage());
        }
    }

    private boolean devModeToggled = false;
    private boolean gridModeToggled = false;
    private boolean inventoryToggled = false;
    private boolean buildModeToggled = false;

    public void startRenderingGame() {
        this.drawingThread = new draw(this, assets);
        this.drawingThread.start();
    }

    public void stopRenderingGame() {
        game.close = true;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyChar() == 'w' && !movement[0] && !game.menuIsOpen) {
            movement[0] = true;
        }
        if (e.getKeyChar() == 'a' && !movement[1] && !game.menuIsOpen) {
            movement[1] = true;
        }
        if (e.getKeyChar() == 's' && !movement[3] && !game.menuIsOpen) {
            movement[3] = true;
        }
        if (e.getKeyChar() == 'd' && !movement[2] && !game.menuIsOpen) {
            movement[2] = true;
        }
        if (e.getKeyCode() == e.VK_F11 && !game.devMode && !devModeToggled && !game.menuIsOpen) {
            game.devMode = true;
        }
        if (e.getKeyCode() == e.VK_F11 && game.devMode && devModeToggled && !game.menuIsOpen) {
            game.devMode = false;
            devModeToggled = false;
        }
        if (e.getKeyCode() == e.VK_F9 && !game.gridMode && !gridModeToggled && !game.menuIsOpen) {
            game.gridMode = true;
        }
        if (e.getKeyCode() == e.VK_F9 && game.gridMode && gridModeToggled && !game.menuIsOpen) {
            game.gridMode = false;
            gridModeToggled = false;
        }
        if (e.getKeyCode() == e.VK_E && !inventory && !inventoryToggled && !game.menuIsOpen) {
            inventory = true;
        }
        if (e.getKeyCode() == e.VK_E && inventory && inventoryToggled && !game.menuIsOpen) {
            inventoryToggled = false;
            inventory = false;
        }
        if (e.getKeyCode() == e.VK_B && !game.buildmode && !buildModeToggled && !game.menuIsOpen) {
            if (!game.getMainPlayer().isAllowedToBuild()) {
                return;
            }
            game.buildmode = true;
        }
        if (e.getKeyCode() == e.VK_B && game.buildmode && buildModeToggled && !game.menuIsOpen) {
            if (!game.getMainPlayer().isAllowedToBuild()) {
                return;
            }
            buildModeToggled = false;
            game.buildmode = false;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyChar() == 'w' && movement[0] && !game.menuIsOpen) {
            movement[0] = false;
        }
        if (e.getKeyChar() == 'a' && movement[1] && !game.menuIsOpen) {
            movement[1] = false;
        }
        if (e.getKeyChar() == 's' && movement[3] && !game.menuIsOpen) {
            movement[3] = false;
        }
        if (e.getKeyChar() == 'd' && movement[2] && !game.menuIsOpen) {
            movement[2] = false;
        }
        if (e.getKeyCode() == e.VK_F11 && game.devMode && devModeToggled && !game.menuIsOpen) {
            game.devMode = false;
        }
        if (e.getKeyCode() == e.VK_F11 && game.devMode && !devModeToggled && !game.menuIsOpen) {
            devModeToggled = true;
        }
        if (e.getKeyCode() == e.VK_F9 && game.gridMode && gridModeToggled && !game.menuIsOpen) {
            game.gridMode = false;
        }
        if (e.getKeyCode() == e.VK_F9 && game.gridMode && !gridModeToggled && !game.menuIsOpen) {
            gridModeToggled = true;
        }
        if (e.getKeyCode() == e.VK_E && inventory && inventoryToggled && !game.menuIsOpen) {
            inventory = false;
        }
        if (e.getKeyCode() == e.VK_E && inventory && !inventoryToggled && !game.menuIsOpen) {
            inventoryToggled = true;
        }
        if (e.getKeyCode() == e.VK_B && game.buildmode && buildModeToggled && !game.menuIsOpen) {
            if (!game.getMainPlayer().isAllowedToBuild()) {
                return;
            }
            game.buildmode = false;
        }
        if (e.getKeyCode() == e.VK_B && game.buildmode && !buildModeToggled && !game.menuIsOpen) {
            if (!game.getMainPlayer().isAllowedToBuild()) {
                return;
            }
            buildModeToggled = true;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        if ((e.getModifiers() & InputEvent.BUTTON1_MASK) != 0 && !game.menuIsOpen) {
            mouseLeft = true;
            if (inventory) {
                inv.mouseDragged(e);
            }
            if (game.buildmode && !inventory && !game.menuIsOpen) {
                game.build.mouseClicked(e);
            }
        } // Left

        if ((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0 && !game.menuIsOpen) {
            mouseRight = true;
        } // Right}

        if ((e.getModifiers() & InputEvent.BUTTON2_MASK) != 0 && !game.menuIsOpen) {
            mouseMid = true;
        } // Middle
    }

    boolean inventoryDragging = false;

    @Override
    public void mouseReleased(MouseEvent e) {
        if ((e.getModifiers() & InputEvent.BUTTON1_MASK) != 0 && !game.menuIsOpen) {
            if (inventory) {
                inv.mouseReleased(e);
            }
            mouseLeft = false;
        } // Left

        if ((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0 && !game.menuIsOpen) {
            mouseRight = false;
        } // Right}

        if ((e.getModifiers() & InputEvent.BUTTON2_MASK) != 0 && !game.menuIsOpen) {
            mouseMid = false;
        } // Middle
    }

    public void mouseDragged(MouseEvent e) {
        if (inventory && !game.menuIsOpen) {
            inv.mouseDragged(e);
        }
    }

    public void mouseMoved(MouseEvent e) {
        if (inventory && !game.menuIsOpen) {
            inv.mousePositionUpdate(e);
        }
        if (game.buildmode && !inventory && !game.menuIsOpen) {
            game.build.mousePositionUpdate(e);
        }
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseClicked(MouseEvent e) {

    }
}
