package com.galdurstudio.galdurui.api.widget;

import com.galdurstudio.galdurui.api.theme.GaldurTheme;
import com.galdurstudio.galdurui.internal.render.GaldurRender;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.function.IntSupplier;

/**
 * Ligne de texte aux couleurs de la charte, utilisable dans les layouts vanilla.
 *
 * <p>Le widget fait exactement la taille de son texte : largeur du texte × hauteur d'une
 * ligne. Il ne reçoit ni clic ni focus et ne joue aucun son ; il reste lu par le narrateur.</p>
 *
 * <pre>{@code
 * LinearLayout colonne = LinearLayout.vertical();
 * colonne.addChild(GaldurLabel.of(Component.literal("Titre")));
 * GaldurLabel compteur = colonne.addChild(GaldurLabel.muted(Component.literal("Clics : 0")));
 * compteur.setText(Component.literal("Clics : 1"));  // met aussi la largeur à jour
 * }</pre>
 *
 * <p>La couleur est relue dans le thème à chaque image : elle suit immédiatement un
 * rechargement des ressources (F3+T n'est pris en compte par Minecraft que si aucun écran
 * n'est ouvert).</p>
 */
public class GaldurLabel extends AbstractWidget {
    /**
     * Couleur ARGB, relue à chaque image pour suivre le thème.
     */
    private final IntSupplier color;
    private final int maxWidth;

    /**
     * Crée un libellé.
     *
     * @param text  texte affiché
     * @param color fournisseur de la couleur ARGB, appelé à chaque image
     */
    protected GaldurLabel(Component text, IntSupplier color) {
        this(text, color, 0);
    }

    protected GaldurLabel(Component text, IntSupplier color, int maxWidth) {
        super(0, 0, fitWidth(text, maxWidth), GaldurRender.lineHeight(), text);
        this.color = color;
        this.maxWidth = maxWidth;
        // Comme StringWidget : un widget inactif n'est ni cliquable ni atteignable au clavier.
        this.active = false;
    }

    public static GaldurLabel of(Component text, int maxWidth) {
        return new GaldurLabel(text, GaldurTheme::text, maxWidth);
    }

    /**
     * {@return un libellé dans la couleur de texte courante ({@code text})}
     *
     * @param text texte affiché
     */
    public static GaldurLabel of(Component text) {
        return GaldurLabel.of(text, 0);
    }


    /**
     * {@return un libellé dans la couleur de texte secondaire ({@code text_muted})}
     *
     * @param text texte affiché
     */
    public static GaldurLabel muted(Component text, int maxWidth) {
        return new GaldurLabel(text, GaldurTheme::textMuted, maxWidth);
    }

    public static GaldurLabel muted(Component text) {
        return GaldurLabel.muted(text, 0);
    }

    private static int fitWidth(Component text, int maxWidth) {
        if (maxWidth <= 0) {
            return GaldurRender.textWidth(text);
        }
        return Math.min(maxWidth, GaldurRender.textWidth(text));
    }

    /**
     * {@return le texte affiché}
     */
    public Component getText() {
        return getMessage();
    }

    /**
     * Remplace le texte et ajuste la largeur du widget en conséquence.
     *
     * <p>La position n'est pas recalculée : si la largeur change, relancer
     * {@code arrangeElements()} sur le layout parent pour réaligner.</p>
     *
     * @param text nouveau texte
     */
    public void setText(Component text) {
        setMessage(text);
        setWidth(fitWidth(text, maxWidth));
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        GaldurRender.text(guiGraphics, GaldurRender.truncateText(getMessage(), maxWidth), getX(), getY(), color.getAsInt(), true);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    @Override
    public void playDownSound(SoundManager handler) {
        // Un libellé n'est pas un bouton : aucun son.
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, getMessage());
    }
}
