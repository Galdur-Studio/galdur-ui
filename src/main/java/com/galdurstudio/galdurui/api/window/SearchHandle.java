package com.galdurstudio.galdurui.api.window;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Poignée vers un champ de recherche d'une {@link GaldurWindow} : son texte, et l'action appelée
 * à chaque frappe.
 *
 * <p>On l'obtient avec {@code GaldurWindow.addSearch(...)}. Comme pour {@link CheckboxHandle},
 * l'état vit ici et non dans le widget affiché : le texte survit aux redimensionnements, et
 * {@link #setText(String)} le change même fenêtre ouverte, sans reconstruire l'écran.</p>
 *
 * <pre>{@code
 * SearchHandle recherche = window.addSearch(Component.literal("Rechercher..."));
 * ListHandle resultats = window.addList(6);
 * recherche.onChange(texte -> resultats.setLines(filtrer(tout, texte)));
 * }</pre>
 *
 * <p>Comme toute {@link ElementHandle}, le champ se désactive, se cache ou reçoit une infobulle.
 * À appeler sur le thread du client.</p>
 */
public class SearchHandle extends ElementHandle {
    private String text = "";
    private Consumer<String> action;

    /** Non publique : seule {@link GaldurWindow} crée des champs. */
    SearchHandle() {
    }

    /** {@return le texte saisi, jamais {@code null} ; vide au départ} */
    public String getText() {
        return text;
    }

    /**
     * Change le texte du champ. Si la valeur change réellement, l'action donnée à
     * {@link #onChange(Consumer)} est appelée, que le changement vienne du joueur ou de ce code.
     *
     * @param text le nouveau texte ; {@code null} vaut un texte vide
     */
    public void setText(String text) {
        String value = Objects.requireNonNullElse(text, "");
        if (value.equals(this.text)) {
            return;
        }
        this.text = value;
        if (action != null) {
            action.accept(value);
        }
    }

    /**
     * Définit l'action appelée quand le texte change, à chaque frappe du joueur. Une seule action
     * est conservée : un nouvel appel remplace la précédente.
     *
     * @param action reçoit le nouveau texte
     */
    public void onChange(Consumer<String> action) {
        this.action = action;
    }
}
