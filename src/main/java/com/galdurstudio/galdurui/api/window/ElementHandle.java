package com.galdurstudio.galdurui.api.window;

import java.util.Objects;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

/**
 * Poignée vers un élément d'une {@link GaldurWindow} : permet de le désactiver, de le cacher ou de
 * lui donner une infobulle, avant comme après l'ouverture de la fenêtre.
 *
 * <p>On l'obtient en retour de chaque {@code GaldurWindow.add...(...)}. Comme pour
 * {@link CheckboxHandle}, l'état vit ici et non dans le widget affiché : Minecraft recrée les
 * widgets à chaque redimensionnement, et ils relisent cet état.</p>
 *
 * <pre>{@code
 * ElementHandle valider = window.addButton(Component.literal("Valider"), this::valider);
 * valider.setEnabled(false);
 * valider.setTooltip(Component.literal("Remplis d'abord le formulaire"));
 *
 * ElementHandle detail = window.addText(Component.literal("Détail..."));
 * detail.setVisible(false);   // l'élément disparaît et libère sa place
 * }</pre>
 *
 * <ul>
 *   <li><strong>Désactivé</strong> : affiché en couleur atténuée, ne réagit ni à la souris ni au
 *       clavier. L'infobulle reste visible au survol, pour expliquer pourquoi.</li>
 *   <li><strong>Caché</strong> : n'est pas affiché et <strong>libère sa place</strong> ; la fenêtre
 *       se remet en page sans lui.</li>
 *   <li><strong>Infobulle</strong> : texte affiché au survol de l'élément.</li>
 * </ul>
 *
 * <p>Un changement fait alors que la fenêtre est affichée est visible à l'image suivante. À appeler
 * sur le thread du client.</p>
 */
public class ElementHandle {
    private boolean enabled = true;
    private boolean visible = true;
    @Nullable
    private Component tooltip;

    /** Prévient la fenêtre qu'un état a changé ; posé par {@link GaldurWindow}. */
    private Runnable onChange = () -> {};

    /** Non publique : seule {@link GaldurWindow} crée des poignées. */
    ElementHandle() {
    }

    /** Branche la fenêtre qui doit se mettre à jour quand l'état change. */
    void setChangeListener(Runnable onChange) {
        this.onChange = onChange;
    }

    /**
     * Active ou désactive l'élément. Sans effet si l'élément est déjà dans cet état.
     *
     * @param enabled {@code false} pour le désactiver
     */
    public void setEnabled(boolean enabled) {
        if (enabled != this.enabled) {
            this.enabled = enabled;
            onChange.run();
        }
    }

    /** {@return {@code true} si l'élément est actif (par défaut)} */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Affiche ou cache l'élément. Caché, il libère sa place dans la fenêtre. Sans effet si
     * l'élément est déjà dans cet état.
     *
     * @param visible {@code false} pour le cacher
     */
    public void setVisible(boolean visible) {
        if (visible != this.visible) {
            this.visible = visible;
            onChange.run();
        }
    }

    /** {@return {@code true} si l'élément est affiché (par défaut)} */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Définit le texte affiché au survol de l'élément.
     *
     * @param tooltip le texte de l'infobulle, ou {@code null} pour la retirer
     */
    public void setTooltip(@Nullable Component tooltip) {
        if (!Objects.equals(tooltip, this.tooltip)) {
            this.tooltip = tooltip;
            onChange.run();
        }
    }

    /** {@return le texte de l'infobulle, ou {@code null} s'il n'y en a pas} */
    @Nullable
    public Component getTooltip() {
        return tooltip;
    }
}
