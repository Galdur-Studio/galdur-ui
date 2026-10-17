package com.galdurstudio.galdurui.api.text;

import com.galdurstudio.galdurui.GaldurUI;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

/**
 * Polices de la charte Galdur, à appliquer à un texte par son style.
 *
 * <p>{@link #TITLE} est la police des titres : Fredoka SemiBold, ronde et épaisse comme le logo,
 * sous licence SIL Open Font License 1.1 ({@code assets/galdurui/font/fredoka_OFL.txt}). Elle
 * est décrite par {@code assets/galdurui/font/title.json} : un pack de ressources peut donc la
 * remplacer. Les caractères absents de la police (emoji, alphabets non latins...) sont pris dans
 * la police de Minecraft.</p>
 *
 * <pre>{@code
 * GaldurRender.text(graphics, GaldurFonts.title(Component.literal("Stockage")), x, y, couleur, false);
 * }</pre>
 *
 * <p>Les titres de {@code GaldurWindow} ({@code addTitle}) et des écrans de menu
 * ({@code GaldurContainerScreen}) l'utilisent déjà.</p>
 */
public final class GaldurFonts {
    /** Police des titres, {@code galdurui:title}. */
    public static final ResourceLocation TITLE = ResourceLocation.fromNamespaceAndPath(GaldurUI.MODID, "title");

    private GaldurFonts() {
    }

    /**
     * {@return une copie de {@code text} dans la police des titres}
     *
     * <p>Le texte reçu n'est pas modifié. Un morceau du texte qui impose sa propre police la
     * garde.</p>
     *
     * @param text le texte à afficher en titre
     */
    public static MutableComponent title(Component text) {
        return text.copy().withStyle(style -> style.withFont(TITLE));
    }
}
