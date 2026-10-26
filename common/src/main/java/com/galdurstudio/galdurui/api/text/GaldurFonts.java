package com.galdurstudio.galdurui.api.text;

import com.galdurstudio.galdurui.GaldurUI;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

/**
 * Brand fonts, applied to a text through its style.
 *
 * <p>{@link #TITLE} is the title font: Fredoka SemiBold, round and bold like the Galdur logo,
 * under the SIL Open Font License 1.1 ({@code assets/galdurui/font/fredoka_ofl.txt}). It is
 * described by {@code assets/galdurui/font/title.json}, so a resource pack can replace it.
 * Characters missing from it (emoji, non-Latin scripts...) come from the Minecraft font.</p>
 *
 * <pre>{@code
 * Component title = GaldurFonts.title(Component.literal("Storage"));
 * }</pre>
 *
 * <p>{@code GaldurWindow} titles ({@code addTitle}) and {@code GaldurContainerScreen} titles
 * already use it.</p>
 */
public final class GaldurFonts {
    /** Title font, {@code galdurui:title}. */
    public static final ResourceLocation TITLE = ResourceLocation.fromNamespaceAndPath(GaldurUI.MODID, "title");

    private GaldurFonts() {
    }

    /**
     * {@return a copy of {@code text} in the title font}; the given text is not modified, and a
     * part of it that sets its own font keeps it
     */
    public static MutableComponent title(Component text) {
        return text.copy().withStyle(style -> style.withFont(TITLE));
    }
}
