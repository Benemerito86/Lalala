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

public class LagoController {

    MusicPlayer player = MusicPlayer.getGlobalPlayer();

    // ============================================================
    // ELEMENTOS DEL FXML
    // ============================================================
    @FXML private AnchorPane rootPane;
    @FXML private ImageView fondo;
    @FXML private ImageView lalaSprite;
    @FXML private AnchorPane overlay;
    @FXML private Label overlayTexto;
    @FXML private Button btnVolver;

    // ============================================================
    // ESCENARIO BASE 3840×2160
    // ============================================================
    private final double BASE_W = 3840.0;
    private final double BASE_H = 2160.0;

    private double escalaX = 1.0;
    private double escalaY = 1.0;

    // ============================================================
    // DATOS DE LALA (tres fases)
    // ============================================================
    private final double[] lalaBaseX = { 1000, 2850, 1264 };
    private final double[] lalaBaseY = { 910, 900, 1500 };

    private final double[] lalaBaseWidth  = { 307, 330, 570 };
    private final double[] lalaBaseHeight = { 543, 453, 777 };

    private final String[] lalaSprites = {
            "/lala/LALA_HORIZONTE.png",
            "/lala/LALA_PIEDRA.png",
            "/lala/LALA_AGUA.png"
    };

    private int faseActual = 0;

    // ============================================================
    // VALORES DEL ZOOM INICIAL
    // ============================================================
    private double zoomInicial = 1.9;
    private double translateInicialX = -380;
    private double translateInicialY = 0;

    // ============================================================
    // NIEVE
    // ============================================================
    private boolean navidenoActivado = false;
    private Timeline nieveTimeline;
    private final Random random = new Random();
    private Image copoImagen;

    // ============================================================
    // INICIALIZACIÓN
    // ============================================================
    @FXML
    public void initialize() {

        // Música del lago
        player.play("/audio/WallyEstrellas2.mp3");

        // Fondo nevado
        URL url = getClass().getResource("/escenarios/LAGO_NEVADO.png");
        if (url == null) {
            System.out.println("❌ No se encontró LAGO_NEVADO.png");
            return;
        }

        fondo.setImage(new Image(url.toExternalForm()));
        fondo.setPreserveRatio(false);

        fondo.fitWidthProperty().bind(rootPane.widthProperty());
        fondo.fitHeightProperty().bind(rootPane.heightProperty());

        fondo.setScaleX(zoomInicial);
        fondo.setScaleY(zoomInicial);
        fondo.setTranslateX(translateInicialX);
        fondo.setTranslateY(translateInicialY);

        overlay.setVisible(false);
        lalaSprite.setVisible(false);

        cargarImagenCopo();

        overlay.lookupAll("Button").forEach(node -> node.setVisible(false));

        Platform.runLater(this::fadeInScene);
        Platform.runLater(this::animarZoomInicial);
    }

    // ============================================================
    // FADE-IN BLANCO
    // ============================================================
    private void fadeInScene() {

        AnchorPane blanco = new AnchorPane();
        blanco.setStyle("-fx-background-color: white;");
        blanco.setOpacity(1);

        blanco.prefWidthProperty().bind(rootPane.widthProperty());
        blanco.prefHeightProperty().bind(rootPane.heightProperty());
        rootPane.getChildren().add(blanco);

        FadeTransition ft = new FadeTransition(Duration.seconds(1.5), blanco);
        ft.setFromValue(1);
        ft.setToValue(0);
        ft.setOnFinished(e -> rootPane.getChildren().remove(blanco));
        ft.play();
    }

    // ============================================================
    // ESCALADO SEGÚN RESOLUCIÓN
    // ============================================================
    private void actualizarEscala() {
        double w = rootPane.getWidth();
        double h = rootPane.getHeight();

        if (w <= 0 || h <= 0) {
            Platform.runLater(this::actualizarEscala);
            return;
        }

        escalaX = w / BASE_W;
        escalaY = h / BASE_H;
    }

    // ============================================================
    // ZOOM INICIAL
    // ============================================================
    private void animarZoomInicial() {

        Duration dur = Duration.seconds(10);

        ScaleTransition zoom = new ScaleTransition(dur, fondo);
        zoom.setFromX(zoomInicial);
        zoom.setToX(1);
        zoom.setFromY(zoomInicial);
        zoom.setToY(1);

        TranslateTransition mover = new TranslateTransition(dur, fondo);
        mover.setFromX(translateInicialX);
        mover.setToX(0);
        mover.setFromY(translateInicialY);
        mover.setToY(0);

        ParallelTransition pt = new ParallelTransition(zoom, mover);

        pt.setOnFinished(e -> {
            fondo.setScaleX(1);
            fondo.setScaleY(1);
            fondo.setTranslateX(0);
            fondo.setTranslateY(0);
            mostrarLalaFase();
        });

        pt.play();
    }

    // ============================================================
    // MOSTRAR SPRITE DE LALA SEGÚN FASE
    // ============================================================
    private void mostrarLalaFase() {

        actualizarEscala();

        try {
            URL url = getClass().getResource(lalaSprites[faseActual]);
            if (url == null) return;

            lalaSprite.setImage(new Image(url.toExternalForm()));

            double extraScale = (faseActual == 2) ? 1.08 : 1.0;

            lalaSprite.setFitWidth(lalaBaseWidth[faseActual]  * escalaX * extraScale);
            lalaSprite.setFitHeight(lalaBaseHeight[faseActual] * escalaY * extraScale);

            lalaSprite.setLayoutX(lalaBaseX[faseActual] * escalaX);
            lalaSprite.setLayoutY(lalaBaseY[faseActual] * escalaY);

            lalaSprite.setOpacity(0);
            lalaSprite.setScaleX(0.7);
            lalaSprite.setScaleY(0.7);
            lalaSprite.setVisible(true);

            FadeTransition fade = new FadeTransition(Duration.seconds(0.4), lalaSprite);
            fade.setFromValue(0);
            fade.setToValue(1);

            ScaleTransition scale = new ScaleTransition(Duration.seconds(0.4), lalaSprite);
            scale.setToX(1);
            scale.setToY(1);

            new ParallelTransition(fade, scale).play();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ============================================================
    // CLICK: ACIERTO / FALLO
    // ============================================================
    @FXML
    private void clickImagen(MouseEvent e) {

        if (lalaSprite != null && lalaSprite.getBoundsInParent().contains(e.getX(), e.getY())) {
            reproducirSonido("/Audio/acierto.mp3");
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
            new MediaPlayer(new Media(url.toURI().toString())).play();
        } catch (Exception ignored) {}
    }

    // ============================================================
    // OVERLAY
    // ============================================================
    private void mostrarOverlay(boolean esAcierto) {

        overlay.setVisible(true);
        overlay.setOpacity(0);

        overlay.prefWidthProperty().bind(rootPane.widthProperty());
        overlay.prefHeightProperty().bind(rootPane.heightProperty());

        Platform.runLater(() -> {
            AnchorPane.setTopAnchor(overlayTexto,
                    (rootPane.getHeight() - overlayTexto.getPrefHeight()) / 2 - 40);
            AnchorPane.setLeftAnchor(overlayTexto,
                    (rootPane.getWidth() - overlayTexto.getPrefWidth()) / 2);
        });

        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.4), overlay);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        fadeIn.setOnFinished(e -> {

            PauseTransition espera = new PauseTransition(Duration.seconds(1.6));

            espera.setOnFinished(ev -> {

                FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.5), overlay);
                fadeOut.setToValue(0);

                fadeOut.setOnFinished(ev2 -> overlay.setVisible(false));

                fadeOut.play();

                if (esAcierto) avanzarFase();
            });

            espera.play();
        });

        fadeIn.play();
    }

    private void mostrarAcierto() {
        overlayTexto.setText("🎉 ¡MUY BIEN! 🎉\n¡Encontraste a Lala!");
        overlayTexto.setStyle("-fx-font-size: 56px; -fx-font-weight: bold; -fx-text-fill: #ffe066;");
        mostrarOverlay(true);
    }

    private void mostrarFallo() {
        overlayTexto.setText("😮 ¡Casi!\n¡Sigue buscando!");
        overlayTexto.setStyle("-fx-font-size: 56px; -fx-font-weight: bold; -fx-text-fill: #ff6b6b;");
        mostrarOverlay(false);
    }

    @FXML
    private void cerrarOverlay() { }

    // ============================================================
    // AVANZAR FASE (misma lógica)
    // ============================================================
    private void avanzarFase() {

        if (faseActual == 0) {
            faseActual = 1;
            animarMovimiento(-340, 120, 1.20);
            return;
        }

        if (faseActual == 1) {
            faseActual = 2;
            animarMovimiento(-120, -210, 1.24);
            return;
        }

        cambiarAFondoNavideno();
    }

    // ============================================================
    // MOVIMIENTO DE CÁMARA
    // ============================================================
    private void animarMovimiento(double movX, double movY, double zoomFinal) {

        actualizarEscala();

        Duration dur = Duration.seconds(3);

        if (lalaSprite.isVisible()) {
            FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.3), lalaSprite);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(ev -> lalaSprite.setVisible(false));
            fadeOut.play();
        }

        double pantallaW = rootPane.getWidth();
        double pantallaH = rootPane.getHeight();

        double fondoW = pantallaW * zoomFinal;
        double fondoH = pantallaH * zoomFinal;

        double minX = pantallaW - fondoW;
        double maxX = 0;

        double minY = pantallaH - fondoH;
        double maxY = 0;

        double margenX = 50;
        double margenY = 80;

        minX += margenX;
        maxX -= margenX;
        minY += margenY;
        maxY -= margenY;

        double destinoX = clamp(movX * escalaX, minX, maxX);
        double destinoY = clamp(movY * escalaY, minY, maxY);

        ScaleTransition st = new ScaleTransition(dur, fondo);
        st.setToX(zoomFinal);
        st.setToY(zoomFinal);

        TranslateTransition tt = new TranslateTransition(dur, fondo);
        tt.setToX(destinoX);
        tt.setToY(destinoY);

        ParallelTransition pt = new ParallelTransition(st, tt);

        pt.setOnFinished(e -> {
            fondo.setScaleX(zoomFinal);
            fondo.setScaleY(zoomFinal);
            fondo.setTranslateX(destinoX);
            fondo.setTranslateY(destinoY);
            mostrarLalaFase();
        });

        pt.play();
    }

    // ============================================================
    // FONDO + LALA + NIEVE NAVIDAD
    // ============================================================
    private void cambiarAFondoNavideno() {

        if (navidenoActivado) return;
        navidenoActivado = true;

        try {
            URL urlFondo = getClass().getResource("/escenarios/CITY_ONE_N.png");
            URL urlLala  = getClass().getResource("/lala/LALA_SENTADA_.png");

            if (urlFondo == null || urlLala == null) return;

            Image nuevoFondo = new Image(urlFondo.toExternalForm());
            Image nuevaLala  = new Image(urlLala.toExternalForm());

            AnchorPane blanco = new AnchorPane();
            blanco.setStyle("-fx-background-color: white;");
            blanco.setOpacity(0);

            blanco.prefWidthProperty().bind(rootPane.widthProperty());
            blanco.prefHeightProperty().bind(rootPane.heightProperty());
            rootPane.getChildren().add(blanco);

            FadeTransition fadeToWhite = new FadeTransition(Duration.seconds(1.2), blanco);
            fadeToWhite.setFromValue(0);
            fadeToWhite.setToValue(1);

            fadeToWhite.setOnFinished(ev -> {

                fondo.setImage(nuevoFondo);
                lalaSprite.setImage(nuevaLala);

                FadeTransition fadeOutWhite = new FadeTransition(Duration.seconds(1.2), blanco);
                fadeOutWhite.setFromValue(1);
                fadeOutWhite.setToValue(0);

                fadeOutWhite.setOnFinished(ev2 -> {

                    rootPane.getChildren().remove(blanco);

                    agregarNieve();
                    player.play("/audio/WallyEstrellas2.mp3");

                    rootPane.setPickOnBounds(true);
                    rootPane.toFront();

                    rootPane.setOnMouseClicked(ev3 -> {
                        rootPane.setOnMouseClicked(null);
                        cargarTransicion();
                    });
                });

                fadeOutWhite.play();
            });

            fadeToWhite.play();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // ============================================================
    // NIEVE
    // ============================================================
    private void cargarImagenCopo() {
        try {
            URL url = getClass().getResource("/img/copo.png");
            if (url != null)
                copoImagen = new Image(url.toExternalForm());
        } catch (Exception ignored) {}
    }

    private void agregarNieve() {
        if (copoImagen == null) return;

        nieveTimeline = new Timeline(
                new KeyFrame(Duration.millis(300), e -> crearCopo())
        );
        nieveTimeline.setCycleCount(Animation.INDEFINITE);
        nieveTimeline.play();
    }

    private void crearCopo() {

        ImageView copo = new ImageView(copoImagen);

        double esc = 0.4 + random.nextDouble() * 0.6;
        copo.setFitWidth(40 * esc);
        copo.setPreserveRatio(true);

        copo.setLayoutX(random.nextDouble() * rootPane.getWidth());
        copo.setLayoutY(-50);

        rootPane.getChildren().add(copo);

        double finY = rootPane.getHeight() + 80;

        TranslateTransition tt = new TranslateTransition(
                Duration.seconds(5 + random.nextDouble() * 4),
                copo
        );
        tt.setToY(finY);
        tt.setOnFinished(e -> rootPane.getChildren().remove(copo));
        tt.play();
    }

    // ============================================================
    // VOLVER AL MENÚ
    // ============================================================
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
    private void onHoverEnter(MouseEvent e) {
        ScaleTransition st = new ScaleTransition(Duration.millis(150), btnVolver);
        st.setToX(1.1);
        st.setToY(1.1);
        st.play();
    }

    @FXML
    private void onHoverExit(MouseEvent e) {
        ScaleTransition st = new ScaleTransition(Duration.millis(150), btnVolver);
        st.setToX(1);
        st.setToY(1);
        st.play();
    }

    // ============================================================
    // UTILIDAD: limitar valores
    // ============================================================
    private double clamp(double valor, double min, double max) {
        return Math.max(min, Math.min(max, valor));
    }

    // ============================================================
    // CARGAR TRANSICIÓN
    // ============================================================
    private void cargarTransicion() {

        AnchorPane blanco = new AnchorPane();
        blanco.setStyle("-fx-background-color: white;");
        blanco.setOpacity(0);

        blanco.prefWidthProperty().bind(rootPane.widthProperty());
        blanco.prefHeightProperty().bind(rootPane.heightProperty());
        rootPane.getChildren().add(blanco);

        FadeTransition fade = new FadeTransition(Duration.seconds(1.0), blanco);
        fade.setFromValue(0);
        fade.setToValue(1);

        fade.setOnFinished(ev -> {

            if (nieveTimeline != null) nieveTimeline.stop();

            Platform.runLater(() -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/transicionView.fxml"));
                    Parent root = loader.load();

                    Stage stage = (Stage) rootPane.getScene().getWindow();
                    stage.getScene().setRoot(root);

                    stage.setFullScreen(false);
                    stage.setResizable(false);
                    stage.setWidth(1920);
                    stage.setHeight(1080);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });

        fade.play();
    }
}
