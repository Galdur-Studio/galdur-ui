package com.galdurstudio.galdurui.api.layout;

import com.galdurstudio.galdurui.api.theme.GaldurTheme;
import com.galdurstudio.galdurui.api.widget.GaldurPanel;
import java.util.OptionalInt;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.layouts.AbstractLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.resources.ResourceLocation;

/**
 * Layout qui entoure un enfant d'un panneau de fond aux couleurs de la charte.
 *
 * <p>Sa taille est celle de l'enfant augmentée du padding sur les quatre côtés ; l'enfant est
 * placé à l'intérieur, et le fond 9-slice suit la position et la taille finales du layout.
 * Combiné aux layouts vanilla, il évite tout calcul de coordonnées dans les écrans :</p>
 *
 * <pre>{@code
 * LinearLayout colonne = LinearLayout.vertical().spacing(GaldurTheme.gap());
 * // ... colonne.addChild(...)
 * GaldurPanelLayout panneau = new GaldurPanelLayout(colonne);
 * addRenderableOnly(panneau);                     // en premier : le fond passe derrière
 * panneau.visitWidgets(this::addRenderableWidget);
 * panneau.arrangeElements();
 * FrameLayout.centerInRectangle(panneau, getRectangle());
 * }</pre>
 *
 * <p>Sans {@link #padding(int)} explicite (ou après {@link #useThemePadding()}), le padding est
 * relu dans le thème ({@code spacing.panel_padding}) à chaque {@link #arrangeElements()}.</p>
 */
public class GaldurPanelLayout extends AbstractLayout implements Renderable {
    private final LayoutElement child;
    private final GaldurPanel background = new GaldurPanel(0, 0, 0, 0);

    /** Padding imposé ; vide = suivre le thème. */
    private OptionalInt padding = OptionalInt.empty();

    /**
     * Crée un panneau autour de {@code child}.
     *
     * @param child contenu du panneau, en général un {@code LinearLayout}
     */
    public GaldurPanelLayout(LayoutElement child) {
        super(0, 0, 0, 0);
        this.child = child;
    }

    /**
     * Fixe le padding au lieu de suivre le thème.
     *
     * @param padding marge entre le bord du panneau et l'enfant, en pixels d'interface
     * @return ce layout, pour chaîner les appels
     * @throws IllegalArgumentException si {@code padding} est négatif
     */
    public GaldurPanelLayout padding(int padding) {
        if (padding < 0) {
            throw new IllegalArgumentException("Le padding d'un GaldurPanelLayout doit être positif ou nul, reçu : "
                    + padding + ". Pour suivre le thème, utiliser useThemePadding().");
        }
        this.padding = OptionalInt.of(padding);
        return this;
    }

    /**
     * Revient au padding du thème ({@code spacing.panel_padding}), relu à chaque arrangement.
     *
     * @return ce layout, pour chaîner les appels
     */
    public GaldurPanelLayout useThemePadding() {
        this.padding = OptionalInt.empty();
        return this;
    }

    /**
     * Remplace le sprite de fond, par exemple pour qu'un mod utilise le sien.
     *
     * @param sprite identifiant d'un sprite d'interface, idéalement découpé en 9-slice
     * @return ce layout, pour chaîner les appels
     */
    public GaldurPanelLayout withSprite(ResourceLocation sprite) {
        background.withSprite(sprite);
        return this;
    }

    /** {@return le padding effectif, en pixels d'interface} */
    public int getPadding() {
        return padding.orElseGet(GaldurTheme::panelPadding);
    }

    /** {@return l'élément entouré par le panneau} */
    public LayoutElement getChild() {
        return child;
    }

    @Override
    public void visitChildren(Consumer<LayoutElement> visitor) {
        visitor.accept(child);
    }

    @Override
    public void arrangeElements() {
        // Arrange d'abord l'enfant (s'il est lui-même un layout) pour connaître sa taille.
        super.arrangeElements();
        int pad = getPadding();
        child.setPosition(getX() + pad, getY() + pad);
        width = child.getWidth() + 2 * pad;
        height = child.getHeight() + 2 * pad;
        syncBackground();
    }

    @Override
    public void setX(int x) {
        super.setX(x);
        syncBackground();
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        syncBackground();
    }

    private void syncBackground() {
        background.setBounds(getX(), getY(), width, height);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        background.render(guiGraphics, mouseX, mouseY, partialTick);
    }
}
