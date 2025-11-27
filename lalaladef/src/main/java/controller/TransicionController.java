package controller;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;
import javafx.application.Platform;
import player.MusicPlayer;

import java.net.URL;
import java.util.Random;

public class TransicionController {

    @FXML private AnchorPane rootPane;
    @FXML private ImageView imagenTransicion;

    private final Random random = new Random();
    private Timeline nieveTimeline;
    private Image copoImagen;

    private static final Duration DURACION_IMAGEN = Duration.seconds(9);

    // ============================================================
    // INICIO DE LA ESCENA
    // ============================================================
    @FXML
    public void initialize() {
        cargarImagenCopo();

        // Esperamos un frame para asegurarnos de que la escena está cargada
        Platform.runLater(() -> {
            iniciarFadeBlancoInicial();  // <-- aquí va tu método nuevo
        });
    }

    // ============================================================
    // FADE-INICIAL (blanco → imagen)
    // ============================================================
    private void iniciarFadeBlancoInicial() {

        // 1) PRIMERO colocamos la primera imagen
        setImagen("/escenarios/TRANSICION_REGALOS1.png");

        // 2) Creamos la capa blanca ENCIMA
        AnchorPane blanco = new AnchorPane();
        blanco.setStyle("-fx-background-color: white;");
        blanco.setOpacity(1);

        blanco.prefWidthProperty().bind(rootPane.widthProperty());
        blanco.prefHeightProperty().bind(rootPane.heightProperty());
        rootPane.getChildren().add(blanco);
        blanco.toFront(); // muy importante

        // 3) HACEMOS EL FADE
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(1.4), blanco);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        fadeOut.setOnFinished(e -> {
            rootPane.getChildren().remove(blanco);

            // Ahora sí activo nieve y transición
            agregarNieve();
            iniciarTransicionImagenes();
        });

        fadeOut.play();
    }

    // ============================================================
    // SECUENCIA DE IMÁGENES
    // ============================================================
    private void iniciarTransicionImagenes() {

        setImagen("/escenarios/TRANSICION_REGALOS1.png");

        PauseTransition pausa = new PauseTransition(DURACION_IMAGEN);

        pausa.setOnFinished(ev -> {
            crossfadeImagen("/escenarios/TRANSICION_REGALOS2.png");
        });

        pausa.play();
    }

    private void setImagen(String ruta) {
        try {
            URL url = getClass().getResource(ruta);
            if (url == null) return;
            imagenTransicion.setImage(new Image(url.toExternalForm()));
        } catch (Exception ignored) {}
    }

    // ============================================================
    // CROSSFADE SUAVE ENTRE IMÁGENES
    // ============================================================
    private void crossfadeImagen(String nuevaRuta) {

        ImageView nueva = new ImageView();
        nueva.setPreserveRatio(false);
        nueva.fitWidthProperty().bind(imagenTransicion.fitWidthProperty());
        nueva.fitHeightProperty().bind(imagenTransicion.fitHeightProperty());

        try {
            URL url = getClass().getResource(nuevaRuta);
            if (url != null) nueva.setImage(new Image(url.toExternalForm()));
        } catch (Exception ignored) {}

        nueva.setOpacity(0);
        rootPane.getChildren().add(nueva);
        nueva.toFront();

        FadeTransition fadeIn = new FadeTransition(Duration.seconds(2.2), nueva);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        fadeIn.setOnFinished(e -> {

            imagenTransicion.setImage(nueva.getImage());
            rootPane.getChildren().remove(nueva);

            PauseTransition espera = new PauseTransition(Duration.seconds(1.5));
            espera.setOnFinished(ev -> cargarLagoNevado());
            espera.play();
        });


        fadeIn.play();
    }

    // ============================================================
    // NIEVE
    // ============================================================
    private void cargarImagenCopo() {
        try {
            URL url = getClass().getResource("/img/copo.png");
            if (url != null) copoImagen = new Image(url.toExternalForm());
        } catch (Exception ignored) {}
    }

    private void agregarNieve() {
        if (copoImagen == null) return;

        nieveTimeline = new Timeline(
                new KeyFrame(Duration.millis(250), e -> crearCopo())
        );
        nieveTimeline.setCycleCount(Animation.INDEFINITE);
        nieveTimeline.play();
    }

    private void crearCopo() {

        ImageView copo = new ImageView(copoImagen);

        double esc = 0.5 + random.nextDouble() * 0.5;
        copo.setFitWidth(40 * esc);
        copo.setPreserveRatio(true);

        copo.setLayoutX(random.nextDouble() * rootPane.getWidth());
        copo.setLayoutY(-50);

        rootPane.getChildren().add(copo);

        double finY = rootPane.getHeight() + 80;

        TranslateTransition tt = new TranslateTransition(
                Duration.seconds(6 + random.nextDouble() * 4),
                copo
        );
        tt.setToY(finY);
        tt.setOnFinished(e -> rootPane.getChildren().remove(copo));
        tt.play();
    }

    // ============================================================
    // TRANSICION A ESCENA 2
    // ============================================================
    private void cargarLagoNevado() {

        AnchorPane blanco = new AnchorPane();
        blanco.setStyle("-fx-background-color: white;");
        blanco.setOpacity(0);

        blanco.prefWidthProperty().bind(rootPane.widthProperty());
        blanco.prefHeightProperty().bind(rootPane.heightProperty());
        rootPane.getChildren().add(blanco);

        FadeTransition ft = new FadeTransition(Duration.seconds(1.0), blanco);
        ft.setFromValue(0);
        ft.setToValue(1);

        ft.setOnFinished(e -> {

            // 🔥 Cortamos la música anterior
            MusicPlayer.getGlobalPlayer().stop();

            // Cargar escena del lago
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/lagoView.fxml"));
                Parent root = loader.load();
                rootPane.getScene().setRoot(root);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        ft.play();
    }

}
