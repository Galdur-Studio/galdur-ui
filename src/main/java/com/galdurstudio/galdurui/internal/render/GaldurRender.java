package com.galdurstudio.galdurui.internal.render;

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
    /** Hauteur d'une ligne de texte, en pixels d'interface. */
    public static final int TEXT_HEIGHT = 9;

    /**
     * Dessine un sprite 9-slice dans le rectangle donné.
     *
     * <p>Le découpage vient du {@code .png.mcmeta} du sprite : les coins gardent leur taille,
     * les bords et le centre sont étirés. Le rectangle doit donc être au moins aussi grand que
     * deux fois la bordure, sinon les coins se chevauchent.</p>
     *
     * @param graphics contexte de rendu fourni par l'écran
     * @param sprite   identifiant du sprite dans l'atlas d'interface
     * @param x        abscisse du coin supérieur gauche
     * @param y        ordonnée du coin supérieur gauche
     * @param width    largeur voulue, en pixels d'interface
     * @param height   hauteur voulue, en pixels d'interface
     */
    public static void drawNineSlice(GuiGraphics graphics, ResourceLocation sprite, int x, int y, int width, int height) {
        graphics.blitSprite(sprite, x, y, width, height);
    }

    /**
     * Dessine une ligne de texte avec ombre portée, alignée à gauche.
     *
     * @param graphics contexte de rendu
     * @param text     texte à dessiner
     * @param x        abscisse du premier caractère
     * @param y        ordonnée du haut de la ligne
     * @param argb     couleur du texte, en ARGB empaqueté
     */
    public static void drawText(GuiGraphics graphics, Component text, int x, int y, int argb) {
        graphics.drawString(font(), text, x, y, argb);
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
        graphics.drawCenteredString(font(), text, centerX, y, argb);
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

    /** {@return la police d'interface courante} */
    private static Font font() {
        return Minecraft.getInstance().font;
    }

    private GaldurRender() {
    }
}
