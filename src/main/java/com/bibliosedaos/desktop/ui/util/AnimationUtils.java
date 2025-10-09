package com.bibliosedaos.desktop.ui.util;

import javafx.animation.Animation;
import javafx.animation.ScaleTransition;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import javafx.animation.Interpolator;

/**
 * Classe d'utilitats per a animacions en JavaFX.
 * Proporciona efectes visuals per a interaccions d'usuari com clics.
 *
 * Assistència d'IA: fragment(s) de codi generat / proposat / refactoritzat per ChatGPT-5 i DeepSeek.
 * S'ha revisat i adaptat manualment per l'autor. Veure llegeixme.pdf per detalls.
 *
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
public final class AnimationUtils {
    private static final double PRESSED = 0.88;
    private static final double RELEASED = 1.0;
    private static final int DURATION = 200;
    private static final String KEY = "anim.scale";

    /**
     * Constructor privat per a classe d'utilitats.
     */
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
        st.setInterpolator(Interpolator.EASE_BOTH); // suaviza entrada/salida
        n.getProperties().put(KEY, st);
        st.setOnFinished(evt -> {
            if (n.getProperties().get(KEY) == st) n.getProperties().remove(KEY);
        });
        st.playFromStart();
    }
}
