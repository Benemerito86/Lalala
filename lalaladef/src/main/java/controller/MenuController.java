package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.beans.binding.Bindings;
import player.MusicPlayer;

import java.io.IOException;

public class MenuController {
    MusicPlayer player = new MusicPlayer();

    @FXML private Button playButton;
    @FXML private Button exitButton;
    @FXML private Pane rootPane;
    @FXML private Pane contentGroup;

    @FXML
    private void initialize() {
        player.play("/audio/navGal2.mp3");

        // Mantengo TODO lo vuestro tal cual:
        contentGroup.translateXProperty().bind(
                Bindings.divide(
                        Bindings.subtract(rootPane.widthProperty(), 1920.0),
                        2
                )
        );
        contentGroup.translateYProperty().bind(
                Bindings.divide(
                        Bindings.subtract(rootPane.heightProperty(), 1080.0),
                        2
                )
        );

        contentGroup.scaleXProperty().bind(
                Bindings.min(
                        Bindings.divide(rootPane.widthProperty(), 1920.0),
                        Bindings.divide(rootPane.heightProperty(), 1080.0)
                )
        );
        contentGroup.scaleYProperty().bind(contentGroup.scaleXProperty());

        // 🔥 AÑADIDO: fijar ventana a 1920×1080 cuando entra al menú
        rootPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                Stage stage = (Stage) rootPane.getScene().getWindow();
                stage.setFullScreen(false);
                stage.setResizable(false);
                stage.setWidth(1920);
                stage.setHeight(1080);
            }
        });
    }

    @FXML
    private void handlePlay() {
        try {
            player.stop();
            Stage stage = (Stage) playButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/gameView.fxml"));
            Parent gameRoot = loader.load();
            stage.getScene().setRoot(gameRoot);

            // 🔥 fijamos tamaño también al entrar al juego
            stage.setFullScreen(false);
            stage.setResizable(false);
            stage.setWidth(1920);
            stage.setHeight(1080);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleExit() {
        System.exit(0);
    }
}
