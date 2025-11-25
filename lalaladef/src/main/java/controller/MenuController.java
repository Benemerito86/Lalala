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
        player.play("/MUSICA/navgal2.mp3");

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
    }

    @FXML
    private void handlePlay() {
        try {
            player.stop();
            Stage stage = (Stage) playButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/gameView.fxml"));
            Parent gameRoot = loader.load();
            stage.getScene().setRoot(gameRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleExit() {
        System.exit(0);
    }
}