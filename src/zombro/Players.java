package zombro;

public class Players extends Thread {
    Game game;

    public Players(Game game) {
        this.game = game;
        try {
            // while (true) {
            // // updateEveryPlayer();
            // sleep(1);
            // }
        } catch (Exception e) {
            System.out.println("Error while trying to updateEveryPlayer!: " + e);
        }
    }

    public void updateEveryPlayer() {
        for (Player p : game.players) {
            p.updatePlayer();
        }
    }
}