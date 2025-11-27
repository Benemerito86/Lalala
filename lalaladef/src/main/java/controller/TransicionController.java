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

        Platform.runLater(() -> {
            iniciarFadeBlancoInicial();
        });
    }

    // ============================================================
    // FADE-INICIAL (blanco → imagen)
    // ============================================================
    private void iniciarFadeBlancoInicial() {

        setImagen("/escenarios/TRANSICION_REGALOS1.png");

        AnchorPane blanco = new AnchorPane();
        blanco.setStyle("-fx-background-color: white;");
        blanco.setOpacity(1);

        blanco.prefWidthProperty().bind(rootPane.widthProperty());
        blanco.prefHeightProperty().bind(rootPane.heightProperty());
        rootPane.getChildren().add(blanco);
        blanco.toFront();

        FadeTransition fadeOut = new FadeTransition(Duration.seconds(1.4), blanco);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        fadeOut.setOnFinished(e -> {
            rootPane.getChildren().remove(blanco);
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
        pausa.setOnFinished(ev -> crossfadeImagen("/escenarios/TRANSICION_REGALOS2.png"));
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
    // CROSSFADE SUAVE — corregido (sin micro-flash)
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

            // 🔥 Evita el FLASH
            imagenTransicion.setOpacity(0);      // Ocultar la anterior
            imagenTransicion.setImage(nueva.getImage());  // Cambiar imagen
            rootPane.getChildren().remove(nueva);         // Quitar capa superior
            imagenTransicion.setOpacity(1);      // Mostrar imagen ya cambiada

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
    // TRANSICION A ESCENA 2 (lago)
    // ============================================================
    private void cargarLagoNevado() {

        // 1) Overlay blanco completamente OPACO desde el inicio (cubre cualquier frame)
        AnchorPane blanco = new AnchorPane();
        blanco.setStyle("-fx-background-color: white;");
        blanco.setOpacity(1);  // <-- ANTES era 0 (causaba el flash)

        blanco.prefWidthProperty().bind(rootPane.widthProperty());
        blanco.prefHeightProperty().bind(rootPane.heightProperty());
        rootPane.getChildren().add(blanco);
        blanco.toFront();

        // 2) Micro-delay para asegurar que el blanco ya ha sido renderizado
        PauseTransition delay = new PauseTransition(Duration.seconds(0.02));
        delay.setOnFinished(d -> {

            // 3) Ahora sí, cargamos la nueva escena SIN FLICKER
            MusicPlayer.getGlobalPlayer().stop();

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/lagoView.fxml"));
                Parent root = loader.load();

                rootPane.getScene().setRoot(root);

                // 4) Fade-out del blanco dentro de la nueva escena
                AnchorPane blanco2 = new AnchorPane();
                blanco2.setStyle("-fx-background-color: white;");
                blanco2.setOpacity(1);

                ((AnchorPane) root).getChildren().add(blanco2);

                FadeTransition fadeOut = new FadeTransition(Duration.seconds(1.0), blanco2);
                fadeOut.setFromValue(1);
                fadeOut.setToValue(0);

                fadeOut.setOnFinished(e2 -> ((AnchorPane) root).getChildren().remove(blanco2));
                fadeOut.play();

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        delay.play();
    }


}
