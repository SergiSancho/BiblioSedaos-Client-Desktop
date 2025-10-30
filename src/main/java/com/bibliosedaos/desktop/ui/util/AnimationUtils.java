package com.bibliosedaos.desktop.ui.util;

import javafx.animation.Animation;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import javafx.animation.Interpolator;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Classe d'utilitats per a animacions en JavaFX.
 * Proporciona efectes visuals per a interaccions d'usuari com clics.
 *
 * Aquesta classe ofereix també mètodes "safe" que encapsulen comprobacions
 * de null i captura d'excepcions per evitar repetir try/catch als controladors.
 *
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
public final class AnimationUtils {
    private static final Logger LOG = Logger.getLogger(AnimationUtils.class.getName());
    private static final double PRESSED = 0.88;
    private static final double RELEASED = 1.0;
    private static final int DURATION = 200;
    private static final String KEY = "anim.scale";

    private AnimationUtils(){}

    /**
     * Aplica un efecte de click amb animació d'escala a un node.
     *
     * @param n Node al qual aplicar l'efecte
     */
    public static void applyClickEffect(Node n){
        if (n == null) return;
        if (Boolean.TRUE.equals(n.getProperties().get("anim.added"))) return;
        n.getProperties().put("anim.added", true);

        EventHandler<MouseEvent> press = e -> play(n, PRESSED);
        EventHandler<MouseEvent> release = e -> play(n, RELEASED);

        n.addEventHandler(MouseEvent.MOUSE_PRESSED, press);
        n.addEventHandler(MouseEvent.MOUSE_RELEASED, release);
        n.addEventHandler(MouseEvent.MOUSE_EXITED, release); // restaura si arrastras fuera
    }

    /**
     * Sobrecàrrega d'utilitat per compatibilitat: si tens aplicacions
     * que criden applyClickEffect sobre Node genèric (ja està bé).
     *
     * @param n node
     */
    public static void applyClickEffectSafeOverload(Node n, boolean unused) {
        applyClickEffect(n);
    }

    /**
     * Executa l'animació d'escala per a un node.
     *
     * @param n Node a animar
     * @param target Escala objectiu
     */
    private static void play(Node n, double target){
        Object prev = n.getProperties().get(KEY);
        if (prev instanceof Animation) ((Animation) prev).stop();

        ScaleTransition st = new ScaleTransition(Duration.millis(DURATION), n);
        st.setToX(target); st.setToY(target);
        st.setInterpolator(Interpolator.EASE_BOTH);
        n.getProperties().put(KEY, st);
        st.setOnFinished(evt -> {
            if (n.getProperties().get(KEY) == st) n.getProperties().remove(KEY);
        });
        st.playFromStart();
    }

    /**
     * Aplica un efecte de clic de forma segura a un node.
     * Si el node és null o l'aplicació de l'efecte llença una excepció,
     * aquesta funció la captura i registra en nivell FINE sense trencar la UI.
     *
     * @param node Node al qual aplicar l'efecte (pot ser null)
     */
    public static void safeApplyClick(Node node) {
        if (node == null) return;
        try {
            applyClickEffect(node);
        } catch (Exception ex) {
            LOG.log(Level.FINE, "No s'ha pogut aplicar l'efecte al node: " + node, ex);
        }
    }

    /**
     * Aplica de forma segura l'efecte de clic a tots els nodes
     * que corresponen al selector especificat dins del parent. L'operació fa
     * el lookup en el fil de la UI (Platform.runLater) per evitar errors de thread.
     *
     * @param parent Parent on fer el lookup (pot ser null)
     * @param cssSelector selector CSS, per exemple ".sidebar-btn"
     */
    public static void safeApplyClickToAll(Parent parent, String cssSelector) {
        if (parent == null || cssSelector == null) return;
        Platform.runLater(() -> {
            try {
                parent.lookupAll(cssSelector).forEach(n -> {
                    if (n instanceof Node) safeApplyClick((Node) n);
                });
            } catch (Exception ex) {
                LOG.log(Level.FINE, "Error al aplicar efectes bulk amb selector: " + cssSelector, ex);
            }
        });
    }

    /**
     * Permet executar una acció sobre un Node de forma segura,
     * evitant que excepcions a l'acció trenquin la UI.
     *
     * @param node node sobre el qual executar l'acció
     * @param action acció que accepta el node
     */
    public static void runSafe(Node node, java.util.function.Consumer<Node> action) {
        if (node == null || action == null) return;
        try {
            action.accept(node);
        } catch (Exception ex) {
            LOG.log(Level.FINE, "Error executant acció segura sobre node", ex);
        }
    }
}
