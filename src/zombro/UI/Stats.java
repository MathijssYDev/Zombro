package zombro.UI;

import java.awt.*;

import zombro.Game;

public class Stats {
    Game game;

    public Stats(Game game) {
        this.game = game;
    }

    public void drawUI(Graphics g, UI gui) {
        int w = 400;
        int h = 200;
        int modifierH = 0;

        Color c = new Color(0.2f, 0.2f, 0.2f, .5f);
        g.setColor(c);
        if (game.buildmode) {
            modifierH += 250;
        }
        g.fillRoundRect(gui.getWidth() - w - 20, gui.getHeight() - h - 20 - modifierH, w, h, 5, 5);

        c = new Color(0.2f, 0.2f, 0.2f, 1.0f);
        g.setColor(c);
        g.fillRect(gui.getWidth() - w - 20 + 20, gui.getHeight() - h - 20 - modifierH + 20, 350, 30);
        c = new Color(0.1f, 0.8f, 0.0f, 1.0f);
        g.setColor(c);
        g.fillRect(gui.getWidth() - w - 20 + 20, gui.getHeight() - h - 20 - modifierH + 20,
                (int) (game.getMainPlayer().getHealth() * 3.5), 30);

        c = new Color(0.2f, 0.2f, 0.2f, 1.0f);
        g.setColor(c);
        g.fillRect(gui.getWidth() - w - 20 + 20, gui.getHeight() - h - 20 - modifierH + 60, 350, 30);
        c = new Color(0.2f, 0.4f, 1.0f, 1.0f);
        g.setColor(c);
        g.fillRect(gui.getWidth() - w - 20 + 20, gui.getHeight() - h - 20 - modifierH + 60,
                (int) (game.getMainPlayer().getShield() * 3.5), 30);
    }
}
