package com.galdurstudio.galdurui.api.text;

import com.galdurstudio.galdurui.GaldurUI;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

/**
 * Title font, removed: titles now use the Minecraft font.
 *
 * @deprecated kept so mods built against 1.0.0 still load; {@link #title(Component)} returns a copy
 * of the text unchanged. Will be removed in 2.0.0.
 */
@Deprecated
public final class GaldurFonts {
    /** Former title font id, no longer shipped. */
    public static final ResourceLocation TITLE = GaldurUI.id("title");

    private GaldurFonts() {
    }

    /** {@return a copy of {@code text}, unchanged} */
    public static MutableComponent title(Component text) {
        return text.copy();
    }
}
