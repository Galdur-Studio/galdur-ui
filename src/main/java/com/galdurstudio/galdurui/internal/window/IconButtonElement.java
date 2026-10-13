package com.galdurstudio.galdurui.internal.window;

import com.galdurstudio.galdurui.api.widget.GaldurIconButton;
import com.galdurstudio.galdurui.api.window.ElementHandle;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Élément de fenêtre qui affiche un bouton icône, avec un sprite ou un objet.
 *
 * @param sprite   le sprite à afficher, ou {@code null} pour afficher {@code item}
 * @param item     l'objet à afficher quand il n'y a pas de sprite (copie faite par l'appelant)
 * @param label    texte de l'infobulle et de la narration
 * @param action   exécutée quand on active le bouton
 * @param centered {@code true} pour centrer le bouton, {@code false} pour l'aligner à gauche
 * @param handle   l'état commun (désactivé, caché, infobulle), partagé avec le mod
 */
public record IconButtonElement(@Nullable ResourceLocation sprite, ItemStack item, Component label,
                                Runnable action, boolean centered, ElementHandle handle) implements WindowElement {
    @Override
    public LayoutElement createWidget(boolean enabled) {
        return sprite != null
                ? GaldurIconButton.of(sprite, label, button -> action.run())
                : GaldurIconButton.ofItem(item, label, button -> action.run());
    }

    @Override
    public void adjustSettings(LayoutSettings settings) {
        if (centered) {
            settings.alignHorizontallyCenter();
        }
    }
}
