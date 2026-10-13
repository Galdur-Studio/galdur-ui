package com.galdurstudio.galdurui.api.widget;

import com.galdurstudio.galdurui.api.theme.GaldurTheme;
import com.galdurstudio.galdurui.internal.render.GaldurRender;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Bouton carré sans texte, qui affiche une icône : un sprite de l'interface ou un objet.
 *
 * <p>C'est un {@link GaldurButton} : même fond, mêmes états (repos, survol, appui, désactivé),
 * même comportement à la souris et au clavier. Seul le contenu change. Le libellé n'est pas
 * affiché mais reste indispensable : il sert d'<strong>infobulle</strong> au survol et de texte
 * pour la narration.</p>
 *
 * <pre>{@code
 * ligne.addChild(GaldurIconButton.ofItem(new ItemStack(Items.HOPPER),
 *         Component.literal("Trier"), bouton -> trier()));
 * ligne.addChild(GaldurIconButton.of(ResourceLocation.fromNamespaceAndPath("monmod", "icon/sort"),
 *         Component.literal("Trier"), bouton -> trier()));
 * }</pre>
 *
 * <ul>
 *   <li><strong>Sprite</strong> : dessiné en 16 × 16, teinté en {@code text} (ou
 *       {@code text_muted} désactivé). À fournir en niveaux de gris, comme les autres sprites de
 *       Galdur UI : le blanc prend exactement la couleur du thème.</li>
 *   <li><strong>Objet</strong> : dessiné comme dans un inventaire ; voilé quand le bouton est
 *       désactivé.</li>
 * </ul>
 *
 * <p>Côté : {@code spacing.button_height}, pour s'aligner sur les boutons à texte d'une même
 * ligne.</p>
 */
public class GaldurIconButton extends GaldurButton {
    /** Côté de l'icône, en pixels. */
    private static final int ICON_SIZE = GaldurRender.ITEM_SIZE;
    /** Opacité du voile posé sur un objet quand le bouton est désactivé (0 à 255). */
    private static final int DISABLED_VEIL_ALPHA = 0xA0;

    @Nullable
    private final ResourceLocation sprite;
    private final ItemStack item;

    /**
     * Crée un bouton icône, sans infobulle. Préférer {@link #of(ResourceLocation, Component, OnPress)}
     * ou {@link #ofItem(ItemStack, Component, OnPress)}, qui ajoutent le libellé en infobulle.
     *
     * @param size    côté du bouton, en pixels
     * @param sprite  sprite à afficher, ou {@code null} pour afficher {@code item}
     * @param item    objet à afficher quand il n'y a pas de sprite ; il est copié
     * @param label   texte de l'infobulle et de la narration
     * @param onPress action déclenchée à l'activation
     */
    protected GaldurIconButton(int size, @Nullable ResourceLocation sprite, ItemStack item, Component label,
                               OnPress onPress) {
        super(0, 0, size, size, label, onPress);
        this.sprite = sprite;
        this.item = item.copy();
    }

    /** {@return {@code button}, avec son libellé en infobulle} */
    private static GaldurIconButton withLabelTooltip(GaldurIconButton button) {
        button.setTooltip(Tooltip.create(button.getMessage()));
        return button;
    }

    /**
     * {@return un bouton qui affiche un sprite de l'interface, positionné en {@code (0, 0)} : c'est
     * le layout qui le place}
     *
     * @param sprite  identifiant du sprite dans l'atlas d'interface, en niveaux de gris
     * @param label   texte de l'infobulle et de la narration
     * @param onPress action déclenchée à l'activation
     */
    public static GaldurIconButton of(ResourceLocation sprite, Component label, OnPress onPress) {
        return withLabelTooltip(new GaldurIconButton(GaldurTheme.buttonHeight(), sprite, ItemStack.EMPTY, label, onPress));
    }

    /**
     * {@return un bouton qui affiche un objet, positionné en {@code (0, 0)}}
     *
     * @param item    l'objet à afficher ; il est copié, le modifier ensuite ne change pas le bouton
     * @param label   texte de l'infobulle et de la narration
     * @param onPress action déclenchée à l'activation
     */
    public static GaldurIconButton ofItem(ItemStack item, Component label, OnPress onPress) {
        return withLabelTooltip(new GaldurIconButton(GaldurTheme.buttonHeight(), null, item, label, onPress));
    }

    /** {@return le côté du bouton : un bouton icône ne s'élargit pas d'après son libellé} */
    @Override
    public int getPreferredWidth() {
        return getWidth();
    }

    @Override
    protected void renderContent(GuiGraphics guiGraphics) {
        int x = getX() + (getWidth() - ICON_SIZE) / 2;
        int y = getY() + (getHeight() - ICON_SIZE) / 2;
        if (sprite != null) {
            GaldurRender.drawNineSlice(guiGraphics, sprite, x, y, ICON_SIZE, ICON_SIZE,
                    active ? GaldurTheme.text() : GaldurTheme.textMuted());
            return;
        }
        GaldurRender.drawItem(guiGraphics, item, x, y);
        if (!active) {
            GaldurRender.dimItem(guiGraphics, x, y, (DISABLED_VEIL_ALPHA << 24) | (GaldurTheme.surface() & 0xFFFFFF));
        }
    }
}
