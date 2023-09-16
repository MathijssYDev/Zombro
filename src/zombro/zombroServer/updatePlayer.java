package zombro.zombroServer;

public class updatePlayer extends Thread {
    ServerPlayer player;

    public updatePlayer(ServerPlayer player) {
        this.player = player;
    }

    public void run() {
        try {
            while (true) {
                System.out.println(player.username);
                Thread.sleep(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
