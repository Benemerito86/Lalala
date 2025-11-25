package controller;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;
import player.MusicPlayer;

import java.net.URL;
import java.util.Random;

public class GameController {
    MusicPlayer player= new MusicPlayer();

    // ====== NODOS DEL FXML ======
    @FXML
    private AnchorPane rootPane;
    @FXML
    private ImageView fondo;
    @FXML
    private AnchorPane overlay;
    @FXML
    private Label overlayTexto;
    @FXML
    private Button btnVolver;

    // ====== ZONA CLICABLE DE LA MARIQUITA ======
    private final double xIni = 3520;
    private final double yIni = 1225;
    private final double ancho = 430;
    private final double alto = 620;

    private double zoomInicial = 1.8;
    private double translateInicial = 480;
    private boolean navidenoActivado = false;
    private Timeline nieveTimeline;
    private final Random random = new Random();
    private Image copoImagen;

    @FXML
    public void initialize() {
        player.play("/MUSICA/Navidad Galáctica.mp3");
        URL url = getClass().getResource("/img/PLANETA_SNIEVE.png");
        if (url == null) {
            System.out.println("❌ No se encontró la imagen del fondo.");
            return;
        }
        Image img = new Image(url.toExternalForm());
        fondo.setImage(img);

        fondo.fitWidthProperty().bind(rootPane.widthProperty());
        fondo.fitHeightProperty().bind(rootPane.heightProperty());
        fondo.setPreserveRatio(false);

        fondo.setScaleX(zoomInicial);
        fondo.setScaleY(zoomInicial);
        fondo.setTranslateX(translateInicial);

        overlay.setVisible(false);
        cargarImagenCopo();
        Platform.runLater(this::animarZoom);
    }

    private void cargarImagenCopo() {
        try {
            URL copoUrl = getClass().getResource("/img/copo.png");
            if (copoUrl == null) {
                System.out.println("⚠ No se encontró /img/copo.png.");
                return;
            }
            copoImagen = new Image(copoUrl.toExternalForm());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void animarZoom() {
        if (navidenoActivado) return;
        Duration dur = Duration.seconds(8);
        ScaleTransition zoom = new ScaleTransition(dur, fondo);
        zoom.setFromX(zoomInicial); zoom.setFromY(zoomInicial);
        zoom.setToX(1.0); zoom.setToY(1.0);
        TranslateTransition mover = new TranslateTransition(dur, fondo);
        mover.setFromX(translateInicial); mover.setToX(0);
        ParallelTransition anim = new ParallelTransition(zoom, mover);
        anim.setOnFinished(e -> {
            fondo.setScaleX(1.0);
            fondo.setScaleY(1.0);
            fondo.setTranslateX(0);
        });
        anim.play();
    }

    // ✅ CORREGIDO: "private" → "public"
    @FXML
    public void clickImagen(MouseEvent e) {
        double escalaX = fondo.getImage().getWidth() / fondo.getBoundsInParent().getWidth();
        double escalaY = fondo.getImage().getHeight() / fondo.getBoundsInParent().getHeight();
        double realX = e.getX() * escalaX;
        double realY = e.getY() * escalaY;

        if (realX >= xIni && realX <= xIni + ancho &&
                realY >= yIni && realY <= yIni + alto) {
            reproducirSonido("/Audio/aplausos.mp3");
            mostrarAcierto();
        } else {
            reproducirSonido("/Audio/fallo.mp3");
            mostrarFallo();
        }
    }

    private void reproducirSonido(String ruta) {
        try {
            URL url = getClass().getResource(ruta);
            if (url == null) return;
            MediaPlayer mp = new MediaPlayer(new Media(url.toURI().toString()));
            mp.play();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void mostrarAcierto() {
        overlayTexto.setText("🎉 ¡MUY BIEN! 🎉\n¡Encontraste a la mariquita!");
        overlayTexto.setStyle("-fx-text-fill: #ffe066; -fx-font-size: 48px; -fx-font-weight: bold;");
        mostrarOverlay(() -> cambiarAFondoNavideno());
    }

    private void mostrarFallo() {
        overlayTexto.setText("😮 ¡Casi!\n¡Sigue buscando!");
        overlayTexto.setStyle("-fx-text-fill: #ff6b6b; -fx-font-size: 48px; -fx-font-weight: bold;");
        mostrarOverlay(null);
    }

    private void mostrarOverlay(Runnable alFinal) {
        overlay.setOpacity(0);
        overlay.setVisible(true);
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), overlay);
        fadeIn.setFromValue(0); fadeIn.setToValue(1);
        fadeIn.setOnFinished(ev -> { if (alFinal != null) alFinal.run(); });
        fadeIn.play();
    }

    @FXML
    private void cerrarOverlay() {
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.5), overlay);
        fadeOut.setFromValue(1); fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> overlay.setVisible(false));
        fadeOut.play();
    }

    private void cambiarAFondoNavideno() {
        if (navidenoActivado) return;
        navidenoActivado = true;

        URL url = getClass().getResource("/img/PLANETA_NIEVE_NAVIDAD.png");
        if (url == null) {
            agregarNieveCartoon();
            return;
        }

        Image nueva = new Image(url.toExternalForm());
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(1), fondo);
        fadeOut.setFromValue(1); fadeOut.setToValue(0);
        fadeOut.setOnFinished(ev -> {
            fondo.setImage(nueva);
            fondo.setScaleX(1.0); fondo.setScaleY(1.0); fondo.setTranslateX(0);
            FadeTransition fadeIn = new FadeTransition(Duration.seconds(1), fondo);
            fadeIn.setFromValue(0); fadeIn.setToValue(1);
            fadeIn.setOnFinished(ev2 -> agregarNieveCartoon());
            fadeIn.play();
        });
        fadeOut.play();
    }

    private void agregarNieveCartoon() {
        if (nieveTimeline != null || copoImagen == null) return;
        nieveTimeline = new Timeline(new KeyFrame(Duration.millis(250), e -> crearCopo()));
        nieveTimeline.setCycleCount(Animation.INDEFINITE);
        nieveTimeline.play();
        player.play("/MUSICA/Wally en las Estrellas.mp3");

    }

    private void crearCopo() {
        if (rootPane.getWidth() <= 0 || rootPane.getHeight() <= 0) return;
        ImageView copo = new ImageView(copoImagen);
        double escala = 0.4 + random.nextDouble() * 0.6;
        copo.setPreserveRatio(true);
        copo.setFitWidth(40 * escala);
        copo.setLayoutX(random.nextDouble() * rootPane.getWidth());
        copo.setLayoutY(-50);
        rootPane.getChildren().add(copo);
        double finY = rootPane.getHeight() + 80;
        double dur = 5 + random.nextDouble() * 4;
        TranslateTransition caer = new TranslateTransition(Duration.seconds(dur), copo);
        caer.setFromY(0); caer.setToY(finY);
        caer.setOnFinished(ev -> rootPane.getChildren().remove(copo));
        caer.play();
    }

    @FXML
    private void volverMenu() {
        try {
            Stage stage = (Stage) rootPane.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/menuView.fxml"));
            Parent menuRoot = loader.load();
            stage.getScene().setRoot(menuRoot);
            player.stop();
            if (!stage.isFullScreen()) stage.setFullScreen(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onHoverEnter(MouseEvent event) {
        ScaleTransition st = new ScaleTransition(Duration.millis(120), btnVolver);
        st.setToX(1.08); st.setToY(1.08); st.play();
    }

    @FXML
    private void onHoverExit(MouseEvent event) {
        ScaleTransition st = new ScaleTransition(Duration.millis(120), btnVolver);
        st.setToX(1.0); st.setToY(1.0); st.play();
    }
}