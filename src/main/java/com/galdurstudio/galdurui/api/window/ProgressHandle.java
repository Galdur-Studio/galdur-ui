package com.galdurstudio.galdurui.api.window;

import org.jetbrains.annotations.Nullable;
import net.minecraft.network.chat.Component;

/**
 * Poignée vers une barre de progression d'une {@link GaldurWindow} : son avancement et le texte
 * affiché dessus.
 *
 * <p>On l'obtient avec {@code addProgress(...)}. Le widget affiché relit l'avancement et le texte
 * à chaque image : on peut les changer aussi souvent qu'on veut (à chaque tick, par exemple),
 * même fenêtre ouverte, sans reconstruire l'écran.</p>
 *
 * <pre>{@code
 * ProgressHandle craft = window.addProgress(Component.literal("Lingots de fer : 0 / 64"));
 * craft.setProgress(16, 64);
 * craft.setLabel(Component.literal("Lingots de fer : 16 / 64"));
 * }</pre>
 *
 * <p>La largeur de la barre est calculée à la construction de l'écran d'après le texte. Un texte
 * plus long donné ensuite est coupé avec « … », jusqu'à la prochaine reconstruction
 * (redimensionnement, ou ajout d'un élément à la fenêtre). Comme toute {@link ElementHandle}, la
 * barre se désactive (couleurs atténuées), se cache ou reçoit une infobulle.</p>
 *
 * <p>À appeler sur le thread du client.</p>
 */
public class ProgressHandle extends ElementHandle {
    private double progress;
    @Nullable
    private Component label;

    /**
     * Non publique : seules {@link GaldurWindow} et les onglets créent des barres.
     *
     * @param label le texte de départ, ou {@code null} pour aucun
     */
    ProgressHandle(@Nullable Component label) {
        this.label = label;
    }

    /** {@return l'avancement, de {@code 0} (vide) à {@code 1} (pleine)} */
    public double getProgress() {
        return progress;
    }

    /**
     * Change l'avancement.
     *
     * @param progress de {@code 0} (vide) à {@code 1} (pleine) ; une valeur hors de cet intervalle
     *                 y est ramenée, et {@code NaN} vaut {@code 0}
     */
    public void setProgress(double progress) {
        this.progress = Double.isNaN(progress) ? 0 : Math.max(0, Math.min(1, progress));
    }

    /**
     * Change l'avancement à partir d'un nombre fait et d'un total, par exemple 16 objets
     * fabriqués sur 64.
     *
     * @param done  ce qui est fait
     * @param total le total ; s'il vaut {@code 0} ou moins, la barre est vide
     */
    public void setProgress(long done, long total) {
        setProgress(total <= 0 ? 0 : (double) done / total);
    }

    /** {@return le texte affiché sur la barre, ou {@code null} s'il n'y en a pas} */
    @Nullable
    public Component getLabel() {
        return label;
    }

    /**
     * Change le texte affiché sur la barre.
     *
     * @param label le nouveau texte, ou {@code null} pour n'en afficher aucun
     */
    public void setLabel(@Nullable Component label) {
        this.label = label;
    }
}
