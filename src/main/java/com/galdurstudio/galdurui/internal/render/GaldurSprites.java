package com.galdurstudio.galdurui.internal.render;

import com.galdurstudio.galdurui.GaldurUI;
import net.minecraft.resources.ResourceLocation;

/**
 * Locations of the sprites shipped with Galdur UI.
 *
 * <p>These ids are resolved by Minecraft's GUI atlas: {@code galdurui:panel} points to
 * {@code assets/galdurui/textures/gui/sprites/panel.png}, whose {@code .png.mcmeta} declares the
 * 9-slice scaling.</p>
 *
 * <p>Internal on purpose: the sprite set shipped with the library may change. Public widgets take
 * any {@link ResourceLocation} so a mod can provide its own.</p>
 */
public final class GaldurSprites {
    /** Panel background, in the main background colour. */
    public static final ResourceLocation PANEL = sprite("panel");
    /** Button at rest. */
    public static final ResourceLocation BUTTON = sprite("button");
    /** Button hovered or keyboard-focused. */
    public static final ResourceLocation BUTTON_HOVER = sprite("button_hover");
    /** Button while pressed. */
    public static final ResourceLocation BUTTON_PRESSED = sprite("button_pressed");
    /** Disabled button. */
    public static final ResourceLocation BUTTON_DISABLED = sprite("button_disabled");
    /** Sunken area: search field, list frame, checkbox, progress bar track. */
    public static final ResourceLocation FIELD = sprite("field");
    /** Item cell (same pixels as {@link #FIELD} at first, can be redrawn separately). */
    public static final ResourceLocation SLOT = sprite("slot");
    /** Inactive tab: rounded top corners, open at the bottom. */
    public static final ResourceLocation TAB = sprite("tab");
    /** Active tab. */
    public static final ResourceLocation TAB_SELECTED = sprite("tab_selected");
    /** List scrollbar track (border 2). */
    public static final ResourceLocation SCROLLBAR_TRACK = sprite("scrollbar_track");
    /** List scrollbar thumb (border 2). */
    public static final ResourceLocation SCROLLBAR_THUMB = sprite("scrollbar_thumb");

    private static ResourceLocation sprite(String name) {
        return ResourceLocation.fromNamespaceAndPath(GaldurUI.MODID, name);
    }

    private GaldurSprites() {
    }
}
