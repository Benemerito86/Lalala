package player;

import javazoom.jl.player.Player;

import java.io.InputStream;

public class MusicPlayer {
    private Player player;
    private Thread playerThread;

    public void play(String resourcePath) {
        stop();

        playerThread = new Thread(() -> {
            try {
                InputStream is = getClass().getResourceAsStream(resourcePath);
                if (is == null) {
                    System.err.println("❌ No se encontró el recurso: " + resourcePath);
                    return;
                }
                player = new Player(is);
                player.play();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        playerThread.start();
    }

    public void stop() {
        if (player != null) player.close();
        if (playerThread != null && playerThread.isAlive()) playerThread.interrupt();
    }

    // =====================================================
    // NUEVO: REPRODUCTOR GLOBAL
    // =====================================================
    private static MusicPlayer globalPlayer = new MusicPlayer();

    public static MusicPlayer getGlobalPlayer() {
        return globalPlayer;
    }
}
