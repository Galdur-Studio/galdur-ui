package com.galdurstudio.galdurui.api.anim;

import java.util.function.DoubleUnaryOperator;

/**
 * Nombre qui passe d'une valeur à une autre en un temps donné, en suivant une courbe
 * ({@link GaldurEasing}).
 *
 * <p>On donne la cible quand on veut, par exemple à chaque image ; l'animation ne repart que si
 * la cible change, et elle part de la valeur affichée à ce moment : changer d'avis en plein
 * milieu ne fait pas sauter l'affichage.</p>
 *
 * <pre>{@code
 * private final AnimatedValue survol = new AnimatedValue(0, GaldurAnimations.HOVER_MS, GaldurEasing.OUT_QUAD);
 *
 * protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
 *     survol.setTarget(isHovered() ? 1 : 0);
 *     int couleur = melanger(repos, survole, survol.get());
 *     ...
 * }
 * }</pre>
 *
 * <p>Si le joueur a coupé les animations ({@link GaldurAnimations#enabled()}), {@link #get()}
 * renvoie directement la cible. À utiliser sur le thread du client.</p>
 */
public final class AnimatedValue {
    private final long durationMs;
    private final DoubleUnaryOperator easing;
    private double from;
    private double to;
    private long start;

    /**
     * @param initial    valeur de départ, affichée sans animation
     * @param durationMs durée d'une transition, en millisecondes
     * @param easing     courbe de la transition, par exemple {@link GaldurEasing#OUT_CUBIC}
     */
    public AnimatedValue(double initial, long durationMs, DoubleUnaryOperator easing) {
        this.durationMs = durationMs;
        this.easing = easing;
        this.from = initial;
        this.to = initial;
    }

    /**
     * Change la cible. Sans effet si c'est déjà la cible ; sinon une transition part de la
     * valeur affichée en ce moment.
     *
     * @param target la nouvelle valeur à atteindre
     */
    public void setTarget(double target) {
        if (target == to) {
            return;
        }
        from = get();
        to = target;
        start = GaldurAnimations.now();
    }

    /**
     * Place la valeur directement sur {@code value}, sans transition.
     *
     * @param value la nouvelle valeur
     */
    public void snap(double value) {
        from = value;
        to = value;
    }

    /** {@return la valeur à afficher maintenant} */
    public double get() {
        double t = GaldurAnimations.progress(start, durationMs);
        if (t >= 1) {
            return to;
        }
        return from + (to - from) * easing.applyAsDouble(t);
    }

    /** {@return la cible actuelle} */
    public double getTarget() {
        return to;
    }

    /** {@return {@code true} tant que la valeur affichée n'a pas atteint la cible} */
    public boolean isRunning() {
        return get() != to;
    }
}
