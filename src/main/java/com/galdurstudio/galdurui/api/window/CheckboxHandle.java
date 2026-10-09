package com.galdurstudio.galdurui.api.window;

import java.util.function.Consumer;

/**
 * Poignée vers une case à cocher d'une {@link GaldurWindow} : permet de lire son état, de le
 * changer et d'être prévenu quand il change.
 *
 * <p>On l'obtient avec {@code GaldurWindow.addCheckbox(...)}. L'état vit ici et non dans le widget
 * affiché : Minecraft recrée les widgets à chaque redimensionnement, la case reste donc cochée
 * (ou non) et le widget relit cet état.</p>
 *
 * <pre>{@code
 * CheckboxHandle son = window.addCheckbox(Component.literal("Activer le son"), false);
 * son.onChange(active -> System.out.println("Son : " + active));
 * son.setChecked(true);   // affiche "Son : true"
 * }</pre>
 */
public class CheckboxHandle {
    private boolean checked;
    private Consumer<Boolean> action;

    /**
     * Crée une case dans l'état donné. Non publique : seule {@link GaldurWindow} en crée.
     *
     * @param defaultState {@code true} si la case est cochée au départ
     */
    CheckboxHandle(boolean defaultState) {
        this.checked = defaultState;
    }

    /**
     * Coche ou décoche la case.
     *
     * <p>Si la valeur change réellement, l'action donnée à {@link #onChange(Consumer)} est appelée
     * avec la nouvelle valeur, que le changement vienne du joueur ou de ce code. Si la case est
     * déjà dans cet état, il ne se passe rien.</p>
     *
     * @param checked {@code true} pour cocher, {@code false} pour décocher
     */
    public void setChecked(boolean checked) {
        if (checked == this.checked) {
            return;
        }
        this.checked = checked;
        if (action != null) {
            action.accept(checked);
        }
    }

    /** {@return {@code true} si la case est cochée} */
    public boolean isChecked() {
        return checked;
    }

    /**
     * Définit l'action appelée quand l'état de la case change.
     *
     * <p>Une seule action est conservée : un nouvel appel remplace la précédente. Elle reçoit le
     * nouvel état ({@code true} si la case vient d'être cochée).</p>
     *
     * @param action appelée à chaque changement d'état
     */
    public void onChange(Consumer<Boolean> action) {
        this.action = action;
    }
}
