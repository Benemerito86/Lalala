package controller;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.Random;

public class GameController {

    // ====== NODOS DEL FXML ======
    @FXML
    private AnchorPane rootPane;   // contenedor raíz (pantalla completa)

    @FXML
    private ImageView fondo;       // imagen principal

    @FXML
    private AnchorPane overlay;    // panel de mensaje grande

    @FXML
    private Label overlayTexto;    // texto del mensaje

    // ====== ZONA CLICABLE DE LA MARIQUITA (COORDENADAS DE LA IMAGEN ORIGINAL) ======
    private final double xIni = 3520;
    private final double yIni = 1225;
    private final double ancho = 430;
    private final double alto = 620;

    // ====== AJUSTES DEL ZOOM INICIAL ======
    private double zoomInicial = 1.8;
    private double translateInicial = 480; // ya lo tenías afinado así

    // ====== NAVIDAD / NIEVE ======
    private boolean navidenoActivado = false;
    private Timeline nieveTimeline;
    private final Random random = new Random();

    // Imagen del copo (cargada una vez)
    private Image copoImagen;

    @FXML
    public void initialize() {

        // Fondo normal
        URL url = getClass().getResource("/img/PLANETA_SNIEVE.png");
        if (url == null) {
            System.out.println("❌ No se encontró la imagen del fondo.");
            return;
        }
        Image img = new Image(url.toExternalForm());
        fondo.setImage(img);

        // 📌 Fullscreen sin bordes en blanco
        fondo.fitWidthProperty().bind(rootPane.widthProperty());
        fondo.fitHeightProperty().bind(rootPane.heightProperty());
        fondo.setPreserveRatio(false); // que rellene todo, aunque estire un poco

        // Zoom inicial
        fondo.setScaleX(zoomInicial);
        fondo.setScaleY(zoomInicial);

        // Empieza mostrando más a la izquierda (tú afinaste este valor)
        fondo.setTranslateX(translateInicial);

        overlay.setVisible(false);

        // Cargar la imagen del copo una sola vez
        cargarImagenCopo();

        // Lanzar animación de zoom al arrancar
        Platform.runLater(this::animarZoom);
    }

    /**
     * Carga la imagen del copo desde /img/copo.png
     */
    private void cargarImagenCopo() {
        try {
            URL copoUrl = getClass().getResource("/img/copo.png");
            if (copoUrl == null) {
                System.out.println("⚠ No se encontró /img/copo.png. La nieve no se mostrará.");
                return;
            }
            copoImagen = new Image(copoUrl.toExternalForm());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Animación del zoom desde la izquierda hacia la vista normal.
     */
    private void animarZoom() {

        // Si ya estamos en modo navideño, NO hacemos zoom
        if (navidenoActivado) {
            return;
        }

        Duration dur = Duration.seconds(8);

        ScaleTransition zoom = new ScaleTransition(dur, fondo);
        zoom.setFromX(zoomInicial);
        zoom.setFromY(zoomInicial);
        zoom.setToX(1.0);
        zoom.setToY(1.0);

        TranslateTransition mover = new TranslateTransition(dur, fondo);
        mover.setFromX(translateInicial);
        mover.setToX(0);

        ParallelTransition anim = new ParallelTransition(zoom, mover);

        anim.setOnFinished(e -> {
            fondo.setScaleX(1.0);
            fondo.setScaleY(1.0);
            fondo.setTranslateX(0);
        });

        anim.play();
    }

    /**
     * CLICK EN LA IMAGEN PRINCIPAL → comprobar si han clicado la mariquita.
     */
    @FXML
    private void clickImagen(MouseEvent e) {

        double x = e.getX();
        double y = e.getY();

        // Pasamos las coordenadas del click a coordenadas de la imagen original
        double escalaX = fondo.getImage().getWidth() / fondo.getBoundsInParent().getWidth();
        double escalaY = fondo.getImage().getHeight() / fondo.getBoundsInParent().getHeight();

        double realX = x * escalaX;
        double realY = y * escalaY;

        if (realX >= xIni && realX <= xIni + ancho &&
                realY >= yIni && realY <= yIni + alto) {

            reproducirSonido("/Audio/aplausos.mp3");
            mostrarAcierto();

        } else {
            reproducirSonido("/Audio/fallo.mp3");
            mostrarFallo();
        }
    }

    /**
     * REPRODUCIR SONIDO
     */
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

    /**
     * MENSAJE DE ACIERTO BONITO PARA NIÑOS + cambio a fondo navideño.
     */
    private void mostrarAcierto() {
        overlayTexto.setText("🎉 ¡MUY BIEN! 🎉\n¡Encontraste a la mariquita!");
        overlayTexto.setStyle(
                "-fx-text-fill: #ffe066; " +
                        "-fx-font-size: 48px; " +
                        "-fx-font-weight: bold;"
        );

        mostrarOverlay(() -> {
            // Al terminar de mostrar el overlay, cambiamos a fondo navideño
            cambiarAFondoNavideno();
        });
    }

    /**
     * MENSAJE DE FALLO
     */
    private void mostrarFallo() {
        overlayTexto.setText("😮 ¡Casi!\n¡Sigue buscando!");
        overlayTexto.setStyle(
                "-fx-text-fill: #ff6b6b; " +
                        "-fx-font-size: 48px; " +
                        "-fx-font-weight: bold;"
        );

        mostrarOverlay(null);
    }

    /**
     * Mostrar overlay con fade-in. Si hay acción al final, se ejecuta.
     */
    private void mostrarOverlay(Runnable alFinal) {

        overlay.setOpacity(0);
        overlay.setVisible(true);

        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), overlay);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.setOnFinished(ev -> {
            if (alFinal != null) {
                alFinal.run();
            }
        });
        fadeIn.play();
    }

    /**
     * Cierra el overlay con fade-out (asociado al clic del botón "Cerrar").
     */
    @FXML
    private void cerrarOverlay() {
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.5), overlay);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> overlay.setVisible(false));
        fadeOut.play();
    }

    /**
     * Cambia la imagen de fondo a la versión navideña y añade nieve cartoon.
     */
    private void cambiarAFondoNavideno() {

        if (navidenoActivado) return;  // ya está

        navidenoActivado = true;

        URL url = getClass().getResource("/img/PLANETA_NIEVE_NAVIDAD.png");
        if (url == null) {
            System.out.println("⚠ No se encontró la imagen navideña, se mantiene la original.");
            // Aunque no haya imagen navideña, arrancamos nieve igual si hay copoImagen
            agregarNieveCartoon();
            return;
        }

        Image nueva = new Image(url.toExternalForm());

        // Cross-fade suave entre la imagen normal y la navideña
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(1), fondo);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        fadeOut.setOnFinished(ev -> {
            fondo.setImage(nueva);
            fondo.setScaleX(1.0);
            fondo.setScaleY(1.0);
            fondo.setTranslateX(0);

            FadeTransition fadeIn = new FadeTransition(Duration.seconds(1), fondo);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.setOnFinished(ev2 -> {
                // Cuando ya está la imagen navideña, arrancamos la nieve
                agregarNieveCartoon();
            });
            fadeIn.play();
        });

        fadeOut.play();
    }

    /**
     * Añade copos de nieve cartoon que caen continuamente por la pantalla (usando copo.png).
     */
    private void agregarNieveCartoon() {

        if (nieveTimeline != null) {
            // Ya está en marcha
            return;
        }

        // Si no hay imagen del copo, no hacemos nada
        if (copoImagen == null) {
            System.out.println("⚠ No hay imagen de copo cargada. Revisa /img/copo.png");
            return;
        }

        nieveTimeline = new Timeline(
                new KeyFrame(Duration.millis(250), e -> crearCopo())
        );
        nieveTimeline.setCycleCount(Animation.INDEFINITE);
        nieveTimeline.play();
    }

    /**
     * Crea un copo (ImageView con copo.png) que cae desde arriba hacia abajo.
     */
    private void crearCopo() {

        if (rootPane.getWidth() <= 0 || rootPane.getHeight() <= 0) {
            return;
        }

        // Nuevo ImageView reutilizando la misma Image copoImagen
        ImageView copo = new ImageView(copoImagen);

        // Tamaño aleatorio para que haya copos grandes y pequeños
        double escala = 0.4 + random.nextDouble() * 0.6; // entre 0.4 y 1.0
        copo.setPreserveRatio(true);
        copo.setFitWidth(40 * escala);  // puedes ajustar el tamaño base
        // Posición inicial arriba
        double inicioX = random.nextDouble() * rootPane.getWidth();
        copo.setLayoutX(inicioX);
        copo.setLayoutY(-50);

        // Lo añadimos por encima del fondo, pero debajo del overlay
        rootPane.getChildren().add(copo);

        double finY = rootPane.getHeight() + 80;
        double dur = 5 + random.nextDouble() * 4; // entre 5 y 9 segundos por caída

        TranslateTransition caer = new TranslateTransition(Duration.seconds(dur), copo);
        caer.setFromY(0);
        caer.setToY(finY);

        caer.setOnFinished(ev -> rootPane.getChildren().remove(copo));
        caer.play();
    }

    /**
     * Volver al menú principal
     */
    @FXML
    private void volverMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/menuView.fxml"));
            Parent menuRoot = loader.load();
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.getScene().setRoot(menuRoot);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
