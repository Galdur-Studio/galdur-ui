package com.galdurstudio.galdurui.internal.window;

import com.galdurstudio.galdurui.api.widget.GaldurButton;
import com.galdurstudio.galdurui.api.window.CheckboxHandle;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.network.chat.Component;

/**
 * Élément de fenêtre qui affiche une case à cocher.
 *
 * <p>L'état n'est pas stocké ici (le record est immuable et les widgets sont recréés à chaque
 * {@code init()}) mais dans le {@link CheckboxHandle}, que le widget relit et met à jour.</p>
 *
 * <p>Provisoire : le widget est pour l'instant un bouton dont le préfixe {@code [x]} ou {@code [ ]}
 * est calculé à la création.</p>
 *
 * @param label    le texte affiché à côté de la case
 * @param handle   la poignée qui porte l'état, partagée avec le mod
 * @param centered {@code true} pour centrer la case dans la fenêtre, {@code false} pour l'aligner à gauche
 */
public record CheckboxElement(Component label, CheckboxHandle handle, boolean centered) implements WindowElement {
    @Override
    public LayoutElement createWidget() {
        String prefix = handle.isChecked() ? "[x] " : "[ ] ";
        return GaldurButton.of(Component.literal(prefix).append(label), button -> handle.setChecked(!handle.isChecked()));
    }

    @Override
    public void adjustSettings(LayoutSettings settings) {
        if (centered) {
            settings.alignHorizontallyCenter();
        }
    }
}
