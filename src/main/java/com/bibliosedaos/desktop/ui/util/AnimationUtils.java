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
     * @param node Node al qual aplicar l'efecte
     */
    public static void applyClickEffect(Node node){
        if (node == null) return;
        if (Boolean.TRUE.equals(node.getProperties().get("anim.added"))) return;
        node.getProperties().put("anim.added", true);

        EventHandler<MouseEvent> press = e -> play(node, PRESSED);
        EventHandler<MouseEvent> release = e -> play(node, RELEASED);

        node.addEventHandler(MouseEvent.MOUSE_PRESSED, press);
        node.addEventHandler(MouseEvent.MOUSE_RELEASED, release);
        node.addEventHandler(MouseEvent.MOUSE_EXITED, release);
    }

    /**
     * Executa l'animació d'escala per a un node.
     *
     * @param node Node a animar
     * @param target Escala objectiu
     */
    private static void play(Node node, double target){
        Object prev = node.getProperties().get(KEY);
        if (prev instanceof Animation animation) {
            animation.stop();
        }

        ScaleTransition st = new ScaleTransition(Duration.millis(DURATION), node);
        st.setToX(target);
        st.setToY(target);
        st.setInterpolator(Interpolator.EASE_BOTH);
        node.getProperties().put(KEY, st);
        st.setOnFinished(evt -> {
            if (node.getProperties().get(KEY) == st) {
                node.getProperties().remove(KEY);
            }
        });
        st.playFromStart();
    }

    /**
     * Aplica un efecte de clic de forma segura a un node.
     * Si el node és null o l'aplicació de l'efecte llença una excepció.
     *
     * @param node Node al qual aplicar l'efecte (pot ser null)
     */
    public static void safeApplyClick(Node node) {
        if (node == null) return;
        try {
            applyClickEffect(node);
        } catch (Exception ex) {
            LOG.log(Level.FINE, () -> "No s'ha pogut aplicar l'efecte al node: " + node);
        }
    }

    /**
     * Aplica de forma segura l'efecte de clic a tots els nodes
     * que corresponen al selector especificat dins del parent.
     *
     * @param parent Parent on fer el lookup (pot ser null)
     * @param cssSelector selector CSS, per exemple ".sidebar-btn"
     */
    public static void safeApplyClickToAll(Parent parent, String cssSelector) {
        if (parent == null || cssSelector == null) return;
        Platform.runLater(() -> {
            try {
                parent.lookupAll(cssSelector).forEach(n -> {
                    if (n != null) safeApplyClick(n);
                });
            } catch (Exception ex) {
                LOG.log(Level.FINE, () -> "Error al aplicar efectes bulk amb selector: " + cssSelector);
            }
        });
    }
}