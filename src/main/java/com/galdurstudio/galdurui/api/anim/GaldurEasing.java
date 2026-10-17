package com.galdurstudio.galdurui.api.anim;

import java.util.function.DoubleUnaryOperator;

/**
 * Courbes d'accélération (« easing ») : elles transforment un avancement linéaire {@code t}, de
 * {@code 0} (début) à {@code 1} (fin), en un avancement qui accélère ou ralentit.
 *
 * <p>Toutes valent {@code 0} en {@code 0} et {@code 1} en {@code 1}. À utiliser avec
 * {@link AnimatedValue}, ou directement :</p>
 *
 * <pre>{@code
 * double t = Math.min(1, ecoule / (double) dureeMs);
 * int y = (int) Math.round(depart + (arrivee - depart) * GaldurEasing.OUT_CUBIC.applyAsDouble(t));
 * }</pre>
 *
 * <p>Pour une interface, {@link #OUT_CUBIC} est le bon choix par défaut : le mouvement part vite
 * et se pose en douceur, il répond donc tout de suite au joueur.</p>
 */
public final class GaldurEasing {
    /** Vitesse constante. */
    public static final DoubleUnaryOperator LINEAR = t -> t;
    /** Démarre doucement, finit vite. */
    public static final DoubleUnaryOperator IN_QUAD = t -> t * t;
    /** Démarre vite, finit doucement ; plus discret que {@link #OUT_CUBIC}. */
    public static final DoubleUnaryOperator OUT_QUAD = t -> 1 - (1 - t) * (1 - t);
    /** Doux au départ et à l'arrivée. */
    public static final DoubleUnaryOperator IN_OUT_QUAD = t -> t < 0.5 ? 2 * t * t : 1 - Math.pow(-2 * t + 2, 2) / 2;
    /** Démarre vite, se pose en douceur : le choix par défaut. */
    public static final DoubleUnaryOperator OUT_CUBIC = t -> 1 - Math.pow(1 - t, 3);
    /** Doux au départ et à l'arrivée, plus marqué que {@link #IN_OUT_QUAD}. */
    public static final DoubleUnaryOperator IN_OUT_CUBIC = t -> t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2;
    /** Dépasse légèrement l'arrivée avant d'y revenir : un petit rebond. */
    public static final DoubleUnaryOperator OUT_BACK = t -> {
        double c1 = 1.70158;
        double c3 = c1 + 1;
        return 1 + c3 * Math.pow(t - 1, 3) + c1 * Math.pow(t - 1, 2);
    };

    private GaldurEasing() {
    }
}
