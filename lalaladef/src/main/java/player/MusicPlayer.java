package player;

import javazoom.jl.player.Player;

import javax.sound.sampled.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class MusicPlayer {
    private Player player;
    private Thread playerThread;

    /**
     * Reproduce un MP3 dentro de resources.
     * @param resourcePath ruta relativa a src/main/resources, ej: "/MUSICA/navgal2.mp3"
     */
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
}