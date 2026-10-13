package com.galdurstudio.galdurui.internal.render;

import com.galdurstudio.galdurui.GaldurUI;
import net.minecraft.resources.ResourceLocation;

/**
 * Emplacements des sprites fournis par Galdur UI.
 *
 * <p>Ces identifiants sont résolus par l'atlas d'interface de Minecraft : {@code galdurui:panel}
 * pointe sur {@code assets/galdurui/textures/gui/sprites/panel.png}, dont le
 * {@code .png.mcmeta} déclare le découpage 9-slice.</p>
 *
 * <p>Interne volontairement : le jeu de sprites livré avec la bibliothèque est susceptible de
 * changer. Les widgets publics acceptent un {@link ResourceLocation} quelconque pour qu'un mod
 * puisse fournir les siens.</p>
 */
public final class GaldurSprites {
    /** Fond de panneau, aux couleurs du fond principal. */
    public static final ResourceLocation PANEL = sprite("panel");
    /** Bouton au repos. */
    public static final ResourceLocation BUTTON = sprite("button");
    /** Bouton survolé ou ciblé au clavier. */
    public static final ResourceLocation BUTTON_HOVER = sprite("button_hover");
    /** Bouton pendant l'appui. */
    public static final ResourceLocation BUTTON_PRESSED = sprite("button_pressed");
    /** Bouton désactivé. */
    public static final ResourceLocation BUTTON_DISABLED = sprite("button_disabled");
    /** Creux : champ de recherche, cadre de liste, case à cocher, rail de barre de progression. */
    public static final ResourceLocation FIELD = sprite("field");
    /** Case d'objet (mêmes pixels que {@link #FIELD} à l'origine, redessinable à part). */
    public static final ResourceLocation SLOT = sprite("slot");
    /** Onglet inactif : coins arrondis en haut, ouvert en bas. */
    public static final ResourceLocation TAB = sprite("tab");
    /** Onglet actif. */
    public static final ResourceLocation TAB_SELECTED = sprite("tab_selected");
    /** Rail de la barre de défilement d'une liste (bordure 2). */
    public static final ResourceLocation SCROLLBAR_TRACK = sprite("scrollbar_track");
    /** Curseur de la barre de défilement d'une liste (bordure 2). */
    public static final ResourceLocation SCROLLBAR_THUMB = sprite("scrollbar_thumb");

    private static ResourceLocation sprite(String name) {
        return ResourceLocation.fromNamespaceAndPath(GaldurUI.MODID, name);
    }

    private GaldurSprites() {
    }
}
