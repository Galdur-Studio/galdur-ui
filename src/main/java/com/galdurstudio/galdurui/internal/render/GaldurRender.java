package com.galdurstudio.galdurui.internal.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Couche de dessin de Galdur UI.
 *
 * <p>C'est la <strong>seule</strong> classe de la bibliothèque qui appelle {@link GuiGraphics}.
 * Les widgets de {@code api.widget} décrivent ce qu'ils veulent voir (un sprite étiré ici, une
 * ligne de texte là) et laissent cette classe s'occuper de l'API de rendu de la version du jeu.
 * Le jour où ce rendu change — il a changé en 1.21.6 — seul ce fichier sera à réécrire.</p>
 *
 * <p>Implémentation visant Minecraft 1.21.1.</p>
 */
public final class GaldurRender {
    /**
     * Dessine un sprite 9-slice teinté.
     *
     * <p>Chaque pixel du sprite est multiplié par {@code argb} : un pixel blanc prend exactement
     * la couleur demandée, un pixel gris une version plus sombre. Les sprites de Galdur UI sont
     * donc en niveaux de gris, et leur couleur vient du thème à chaque image. L'alpha de
     * {@code argb} rend le sprite translucide.</p>
     *
     * <p>Implémentation 1.21.1 : {@link GuiGraphics#setColor} (qui vide le tampon en attente
     * puis règle la couleur du shader) et blending activé, comme {@code AbstractButton} ; la
     * couleur est remise à blanc après le dessin pour ne pas teinter la suite.</p>
     *
     * @param graphics contexte de rendu fourni par l'écran
     * @param sprite   identifiant du sprite dans l'atlas d'interface
     * @param x        abscisse du coin supérieur gauche
     * @param y        ordonnée du coin supérieur gauche
     * @param width    largeur voulue, en pixels d'interface
     * @param height   hauteur voulue, en pixels d'interface
     * @param argb     teinte, en ARGB empaqueté ({@code 0xFFFFFFFF} = aucune teinte)
     */
    public static void drawNineSlice(GuiGraphics graphics, ResourceLocation sprite, int x, int y, int width, int height, int argb) {
        graphics.setColor(channel(argb, 16), channel(argb, 8), channel(argb, 0), channel(argb, 24));
        RenderSystem.enableBlend();
        graphics.blitSprite(sprite, x, y, width, height);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    /** {@return la composante de {@code argb} décalée de {@code shift} bits, entre 0 et 1} */
    private static float channel(int argb, int shift) {
        return ((argb >>> shift) & 0xFF) / 255.0F;
    }

    /**
     * Dessine une ligne de texte alignée à gauche.
     *
     * <p>Seul appel à {@link GuiGraphics#drawString} de la bibliothèque : les autres méthodes
     * de texte passent par ici.</p>
     *
     * @param graphics contexte de rendu
     * @param text     texte à dessiner
     * @param x        abscisse du premier caractère
     * @param y        ordonnée du haut de la ligne
     * @param argb     couleur du texte, en ARGB empaqueté
     * @param shadow   {@code true} pour dessiner l'ombre portée
     */
    public static void text(GuiGraphics graphics, Component text, int x, int y, int argb, boolean shadow) {
        graphics.drawString(font(), text, x, y, argb, shadow);
    }

    /**
     * Dessine une ligne de texte avec ombre portée, centrée horizontalement.
     *
     * @param graphics contexte de rendu
     * @param text     texte à dessiner
     * @param centerX  abscisse du centre de la ligne
     * @param y        ordonnée du haut de la ligne
     * @param argb     couleur du texte, en ARGB empaqueté
     */
    public static void drawCenteredText(GuiGraphics graphics, Component text, int centerX, int y, int argb) {
        text(graphics, text, centerX - textWidth(text) / 2, y, argb, true);
    }

    /**
     * {@return la largeur de {@code text} en pixels d'interface}
     *
     * <p>Permet aux widgets de se dimensionner sans toucher à {@link Font}.</p>
     *
     * @param text texte à mesurer
     */
    public static int textWidth(Component text) {
        return font().width(text);
    }

    /** {@return la hauteur d'une ligne de texte de la police courante, en pixels d'interface} */
    public static int lineHeight() {
        return font().lineHeight;
    }

    /** {@return la police d'interface courante} */
    private static Font font() {
        return Minecraft.getInstance().font;
    }

    private GaldurRender() {
    }
}
