package zombro.UI.menu;

import zombro.Game;
import zombro.UI.UI;
import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.Color;
import java.awt.BorderLayout;

public class Menu {
    Game game;
    JPanel home;

    public Menu(Game game) {
        this.game = game;
    }

    public void pack(UI gui) {
        home = new homepage(game);
        gui.getContentPane().add(home, new BorderLayout().WEST);
        home.setSize(1920, 1080);
        home.setBackground(Color.red);
    }

}
