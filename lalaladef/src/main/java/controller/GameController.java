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

    MusicPlayer player = new MusicPlayer();

    // =======================================
    // NODOS FXML
    // =======================================
    @FXML private AnchorPane rootPane;
    @FXML private ImageView fondo;
    @FXML private ImageView lalaSprite;
    @FXML private AnchorPane overlay;
    @FXML private Label overlayTexto;
    @FXML private Button btnVolver;

    // =======================================
    // FONDO REAL — 3840×2160
    // =======================================
    private final double BASE_W = 3840.0;
    private final double BASE_H = 2160.0;

    private double escalaX = 1.0;
    private double escalaY = 1.0;

    // =======================================
    // POSICIONES BASE LALA (AJUSTADAS SUAVE)
    // =======================================
    private final double[] lalaBaseX = {
            1148,  // Fase 0 — LALA_DEPIE
            1620,  // Fase 1 — PARACAIDAS
            2564   // Fase 2 — SENTADA
    };

    private final double[] lalaBaseY = {
            720,   // Fase 0
            42,    // Fase 1
            1240   // Fase 2
    };

    // Tamaños BASE
    private final double[] lalaBaseWidth = {
            377, // de pie
            550, // paracaídas
            670  // sentada
    };

    private final double[] lalaBaseHeight = {
            613,
            673,
            877
    };

    private final String[] lalaSprites = {
            "/lala/LALA_DEPIE.png",
            "/lala/LALA_PARACAIDAS.png",
            "/lala/LALA_SENTADA.png"
    };

    private int faseActual = 0;

    // =======================================
    // ZOOM INICIAL
    // =======================================
    private double zoomInicial = 1.8;
    private double translateInicialX = -480;
    private double translateInicialY = 0;

    // NAVIDAD
    private boolean navidenoActivado = false;
    private Timeline nieveTimeline;
    private final Random random = new Random();
    private Image copoImagen;

    // =======================================
    // INIT
    // =======================================
    @FXML
    public void initialize() {

        player.play("/audio/navGal.mp3");

        // Cargar fondo 3840×2160
        URL url = getClass().getResource("/escenarios/CITY_ONE.png");
        if (url == null) {
            System.out.println("❌ No se encontró CITY_ONE.png");
            return;
        }

        fondo.setImage(new Image(url.toExternalForm()));
        fondo.setPreserveRatio(false);

        // Fondo adaptado al tamaño del panel
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

        Platform.runLater(this::animarZoomInicial);
    }

    // =======================================
    // ESCALADO POR RESOLUCIÓN
    // =======================================
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

    // =======================================
    // ZOOM INICIAL
    // =======================================
    private void animarZoomInicial() {

        Duration dur = Duration.seconds(12);

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

    // =======================================
    // MOSTRAR LALA de forma exacta
    // =======================================
    private void mostrarLalaFase() {

        actualizarEscala();

        try {
            URL url = getClass().getResource(lalaSprites[faseActual]);
            if (url == null) return;

            lalaSprite.setImage(new Image(url.toExternalForm()));

            // Escala extra solo para la Lala sentada (fase 2)
            double extraScale = (faseActual == 2) ? 1.08 : 1.0;

            // Tamaño proporcional REAL
            lalaSprite.setFitWidth(lalaBaseWidth[faseActual] * escalaX * extraScale);
            lalaSprite.setFitHeight(lalaBaseHeight[faseActual] * escalaY * extraScale);

            // Posición real
            lalaSprite.setLayoutX(lalaBaseX[faseActual] * escalaX);
            lalaSprite.setLayoutY(lalaBaseY[faseActual] * escalaY);

            // Animación POP
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

    // =======================================
    // CLICK
    // =======================================
    @FXML
    private void clickImagen(MouseEvent e) {

        if (lalaSprite != null && lalaSprite.getBoundsInParent().contains(e.getX(), e.getY())) {
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
            new MediaPlayer(new Media(url.toURI().toString())).play();
        } catch (Exception ignored) {}
    }

    // =======================================
    // OVERLAY CENTRADO
    // =======================================
    private void mostrarOverlay(boolean esAcierto) {

        overlay.setVisible(true);
        overlay.setOpacity(0);

        // El overlay ocupa toda la pantalla
        overlay.prefWidthProperty().bind(rootPane.widthProperty());
        overlay.prefHeightProperty().bind(rootPane.heightProperty());

        // Centramos el texto SIEMPRE
        Platform.runLater(() -> {
            AnchorPane.setTopAnchor(overlayTexto,
                    (rootPane.getHeight() - overlayTexto.getPrefHeight()) / 2 - 40);
            AnchorPane.setLeftAnchor(overlayTexto,
                    (rootPane.getWidth() - overlayTexto.getPrefWidth()) / 2);
        });

        // Fade IN
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.4), overlay);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        fadeIn.setOnFinished(e -> {

            // Espera de segundos
            PauseTransition espera = new PauseTransition(Duration.seconds(1.6));

            espera.setOnFinished(ev -> {

                // Fade OUT
                FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.5), overlay);
                fadeOut.setToValue(0);

                fadeOut.setOnFinished(ev2 -> overlay.setVisible(false));

                fadeOut.play();

                // Si fue acierto → avanzar fase después del fade
                if (esAcierto) {
                    avanzarFase();
                }
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
    private void cerrarOverlay() {
        // vacío, pero evita que el FXML peten
    }


    // =======================================
    // CAMBIO DE FASE
    // =======================================
    private void avanzarFase() {

        if (faseActual == 0) {
            faseActual = 1;

            // SUBIR A LA LALA DEL PARACAÍDAS
            animarMovimiento(-120, 200, 1.20);
            return;
        }

        if (faseActual == 1) {
            faseActual = 2;

            // BAJAR A LA LALA SENTADA
            animarMovimiento(-340, -120, 1.22);
            return;
        }

        cambiarAFondoNavideno();
    }

    // =======================================
    // MOVIMIENTO + ZOOM (CON CLAMPS)
    // =======================================
    private void animarMovimiento(double movX, double movY, double zoomFinal) {

        actualizarEscala();

        Duration dur = Duration.seconds(3);

        // Fade-out de Lala antes de mover la cámara
        if (lalaSprite.isVisible()) {
            FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.3), lalaSprite);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(ev -> lalaSprite.setVisible(false));
            fadeOut.play();
        }

        double pantallaW = rootPane.getWidth();
        double pantallaH = rootPane.getHeight();

        // tamaño del fondo después del zoom final
        double fondoW = pantallaW * zoomFinal;
        double fondoH = pantallaH * zoomFinal;

        double minX = pantallaW - fondoW;  // límite izquierda
        double maxX = 0;                   // límite derecha

        double minY = pantallaH - fondoH;  // límite arriba
        double maxY = 0;                   // límite abajo

        // Margen de seguridad para NO llegar al borde exacto
        // (en pixels de pantalla, sin escalar)
        double margenX = 50;
        double margenY = 80;

        minX += margenX;
        maxX -= margenX;
        minY += margenY;
        maxY -= margenY;

        // movimiento real con escala + clamp
        // (movX/movY se pensaron en coords "base", por eso mantenemos el *escalaX / *escalaY)
        double destinoX = clamp(movX * escalaX, minX, maxX);
        double destinoY = clamp(movY * escalaY, minY, maxY);

        // ANIMACIONES DE ZOOM Y TRASLACIÓN
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

    // =======================================
    // NAVIDAD
    // =======================================
    private void cambiarAFondoNavideno() {
        if (navidenoActivado) return;
        navidenoActivado = true;

        try {
            URL urlFondo = getClass().getResource("/escenarios/CITY_ONE_N.png");
            URL urlLala = getClass().getResource("/lala/LALA_SENTADA_.png");

            if (urlFondo == null || urlLala == null) return;

            Image nuevoFondo = new Image(urlFondo.toExternalForm());
            Image nuevaLala = new Image(urlLala.toExternalForm());

            // === FADE OUT conjunto ===
            FadeTransition fadeOutFondo = new FadeTransition(Duration.seconds(0.8), fondo);
            fadeOutFondo.setToValue(0);

            FadeTransition fadeOutLala = new FadeTransition(Duration.seconds(0.8), lalaSprite);
            fadeOutLala.setToValue(0);

            ParallelTransition fadeOutGroup =
                    new ParallelTransition(fadeOutFondo, fadeOutLala);

            fadeOutGroup.setOnFinished(ev -> {

                // CAMBIO real de imágenes cuando están invisibles
                fondo.setImage(nuevoFondo);
                lalaSprite.setImage(nuevaLala);

                // Reaplicar opacidad antes del fade in
                fondo.setOpacity(0);
                lalaSprite.setOpacity(0);

                // === FADE IN conjunto ===
                FadeTransition fadeInFondo = new FadeTransition(Duration.seconds(1.0), fondo);
                fadeInFondo.setToValue(1);

                FadeTransition fadeInLala = new FadeTransition(Duration.seconds(1.0), lalaSprite);
                fadeInLala.setToValue(1);

                ParallelTransition fadeInGroup =
                        new ParallelTransition(fadeInFondo, fadeInLala);

                fadeInGroup.setOnFinished(ev2 -> {
                    // Y al terminar, añadimos NIEVE
                    agregarNieve();
                });

                fadeInGroup.play();
            });

            fadeOutGroup.play();

        } catch (Exception ignored) {}
    }

    private void cargarImagenCopo() {
        try {
            URL url = getClass().getResource("/img/copo.png");
            if (url != null)
                copoImagen = new Image(url.toExternalForm());
        } catch (Exception ignored) {}
    }

    private void agregarNieve() {
        if (copoImagen == null) return;

        nieveTimeline = new Timeline(new KeyFrame(Duration.millis(300), e -> crearCopo()));
        nieveTimeline.setCycleCount(Animation.INDEFINITE);
        nieveTimeline.play();

        player.play("/audio/WallyEstrellas.mp3");
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

        TranslateTransition tt = new TranslateTransition(Duration.seconds(5 + random.nextDouble() * 4), copo);
        tt.setToY(finY);
        tt.setOnFinished(e -> rootPane.getChildren().remove(copo));
        tt.play();
    }

    // =======================================
    // VOLVER
    // =======================================
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

    // LIMITADOR DE MOVIMIENTO PARA NO MOSTRAR BORDES
    private double clamp(double valor, double min, double max) {
        return Math.max(min, Math.min(max, valor));
    }

}


