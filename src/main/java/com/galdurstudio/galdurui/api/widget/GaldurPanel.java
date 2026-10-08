package com.galdurstudio.galdurui.api.widget;

import com.galdurstudio.galdurui.internal.render.GaldurRender;
import com.galdurstudio.galdurui.internal.render.GaldurSprites;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.resources.ResourceLocation;

/**
 * Panneau de fond aux couleurs de la charte, dessiné avec un sprite 9-slice.
 *
 * <p>Élément purement décoratif : il ne reçoit ni clic ni focus, et sert de support aux
 * widgets posés dessus. Comme il implémente {@link Renderable}, on l'ajoute à un écran avec
 * {@code addRenderableOnly(...)} — en premier, pour qu'il passe derrière le reste.</p>
 *
 * <pre>{@code
 * GaldurPanel fond = new GaldurPanel(x, y, 220, 140);
 * addRenderableOnly(fond);
 * }</pre>
 *
 * <p>Les coins du sprite ne sont pas étirés : prévoir au moins 8 px de côté, sous peine de
 * les voir se chevaucher.</p>
 */
public class GaldurPanel implements Renderable {
    private int x;
    private int y;
    private int width;
    private int height;
    private ResourceLocation sprite = GaldurSprites.PANEL;

    /** Indique si le panneau doit être dessiné. */
    public boolean visible = true;

    /**
     * Crée un panneau.
     *
     * @param x      abscisse du coin supérieur gauche
     * @param y      ordonnée du coin supérieur gauche
     * @param width  largeur, en pixels d'interface
     * @param height hauteur, en pixels d'interface
     */
    public GaldurPanel(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Remplace le sprite de fond, par exemple pour qu'un mod utilise le sien.
     *
     * @param sprite identifiant d'un sprite d'interface, idéalement découpé en 9-slice
     * @return ce panneau, pour chaîner les appels
     */
    public GaldurPanel withSprite(ResourceLocation sprite) {
        this.sprite = sprite;
        return this;
    }

    /**
     * Déplace et redimensionne le panneau.
     *
     * <p>Utile depuis {@code Screen#init()}, qui est rappelé à chaque changement de taille de
     * fenêtre ou d'échelle d'interface.</p>
     *
     * @param x      abscisse du coin supérieur gauche
     * @param y      ordonnée du coin supérieur gauche
     * @param width  largeur, en pixels d'interface
     * @param height hauteur, en pixels d'interface
     */
    public void setBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /** {@return l'abscisse du coin supérieur gauche} */
    public int getX() {
        return x;
    }

    /** {@return l'ordonnée du coin supérieur gauche} */
    public int getY() {
        return y;
    }

    /** {@return la largeur du panneau, en pixels d'interface} */
    public int getWidth() {
        return width;
    }

    /** {@return la hauteur du panneau, en pixels d'interface} */
    public int getHeight() {
        return height;
    }

    /** {@return l'abscisse du centre du panneau} */
    public int getCenterX() {
        return x + width / 2;
    }

    /** {@return l'abscisse du bord droit, exclue} */
    public int getRight() {
        return x + width;
    }

    /** {@return l'ordonnée du bord inférieur, exclue} */
    public int getBottom() {
        return y + height;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (visible) {
            GaldurRender.drawNineSlice(guiGraphics, sprite, x, y, width, height);
        }
    }
}
