package com.galdurstudio.galdurui.api.anim;

import com.galdurstudio.galdurui.internal.config.GaldurConfig;
import net.minecraft.Util;

/**
 * Réglage global et durées des animations de Galdur UI.
 *
 * <p>Le joueur peut couper toutes les animations dans la configuration du mod (Mods → Galdur UI
 * → Configurer, ou {@code config/galdurui-client.toml}) : les changements deviennent alors
 * instantanés. Une animation écrite par un mod doit vérifier {@link #enabled()} ;
 * {@link AnimatedValue} le fait déjà.</p>
 */
public final class GaldurAnimations {
    /** Durée d'un fondu de survol (bouton, onglet), en millisecondes. */
    public static final long HOVER_MS = 90;
    /** Durée d'une case qui se coche ou se décoche, en millisecondes. */
    public static final long TOGGLE_MS = 110;
    /** Durée de l'ouverture d'un écran, en millisecondes. */
    public static final long OPEN_MS = 180;
    /** Durée du glissement du contenu au changement d'onglet, en millisecondes. */
    public static final long PAGE_MS = 160;
    /** Temps caractéristique du défilement fluide, en millisecondes. */
    public static final double SCROLL_SMOOTHING_MS = 45;

    private GaldurAnimations() {
    }

    /** {@return {@code true} si le joueur n'a pas coupé les animations} */
    public static boolean enabled() {
        return GaldurConfig.animations();
    }

    /** {@return l'horloge des animations, en millisecondes} */
    public static long now() {
        return Util.getMillis();
    }

    /**
     * {@return l'avancement d'une animation commencée à {@code start}, de {@code 0} à {@code 1}}
     *
     * <p>Vaut {@code 1} (terminée) si les animations sont coupées ou si {@code durationMs} est
     * nul ou négatif.</p>
     *
     * @param start      début de l'animation, lu sur {@link #now()}
     * @param durationMs durée, en millisecondes
     */
    public static double progress(long start, long durationMs) {
        if (!enabled() || durationMs <= 0) {
            return 1;
        }
        return Math.max(0, Math.min(1, (now() - start) / (double) durationMs));
    }

    /**
     * {@return {@code current} rapproché de {@code target}, avec un lissage exponentiel qui ne
     * dépend pas du nombre d'images par seconde}
     *
     * <p>Utile pour suivre une cible qui change sans cesse (défilement, barre de progression).
     * Renvoie directement {@code target} si les animations sont coupées, ou quand l'écart
     * restant est inférieur à {@code epsilon}.</p>
     *
     * @param current     la valeur affichée
     * @param target      la valeur à atteindre
     * @param elapsedMs   temps écoulé depuis l'image précédente
     * @param timeConstMs temps caractéristique : l'écart est divisé par e (~2,7) en ce temps
     * @param epsilon     écart sous lequel on s'arrête sur la cible
     */
    public static double approach(double current, double target, double elapsedMs, double timeConstMs, double epsilon) {
        if (!enabled() || Double.isNaN(current)) {
            return target;
        }
        double next = current + (target - current) * (1 - Math.exp(-Math.max(0, elapsedMs) / timeConstMs));
        return Math.abs(target - next) < epsilon ? target : next;
    }
}
