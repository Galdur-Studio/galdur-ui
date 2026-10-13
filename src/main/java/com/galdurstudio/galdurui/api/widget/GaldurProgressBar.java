package com.galdurstudio.galdurui.api.widget;

import com.galdurstudio.galdurui.api.theme.GaldurTheme;
import com.galdurstudio.galdurui.internal.render.GaldurRender;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

/**
 * Barre de progression aux couleurs de la charte, avec un texte facultatif centré dessus,
 * utilisable dans les layouts vanilla.
 *
 * <p>Comme {@link GaldurScrollList}, le widget ne garde ni l'avancement ni le texte : il les
 * <strong>relit à chaque image</strong> auprès de fournisseurs. On peut donc faire avancer la
 * barre à chaque tick sans recréer le widget.</p>
 *
 * <pre>{@code
 * double[] avancement = {0.25};
 * colonne.addChild(GaldurProgressBar.of(120, () -> avancement[0],
 *         () -> Component.literal("16 / 64")));
 * }</pre>
 *
 * <ul>
 *   <li><strong>Remplissage</strong> : de gauche à droite, en {@code accent} sur un fond
 *       {@code background}, cadre en {@code text_muted}.</li>
 *   <li><strong>Texte</strong> : centré, en {@code text} avec ombre ; s'il est trop long, il est
 *       coupé avec « … ». Sans texte ({@code null}), la barre est seule.</li>
 *   <li><strong>Désactivée</strong> ({@code active = false}) : remplissage et texte en
 *       {@code text_muted}.</li>
 * </ul>
 *
 * <p>La barre n'est pas interactive : elle ne prend ni le focus ni les clics. Hauteur lue dans
 * le thème : {@code spacing.progress_height}.</p>
 */
public class GaldurProgressBar extends AbstractWidget {
    /** Épaisseur du cadre, en pixels. */
    private static final int BORDER = 1;
    /** Marge entre le bord et un texte coupé, en pixels. */
    private static final int TEXT_PADDING = 2;

    private final DoubleSupplier progress;
    private final Supplier<Component> label;

    /**
     * Crée une barre de progression. Préférer {@link #of(int, DoubleSupplier, Supplier)}.
     *
     * @param width    largeur totale, cadre compris, en pixels
     * @param progress fournisseur de l'avancement, de {@code 0} (vide) à {@code 1} (pleine), appelé
     *                 à chaque image ; les valeurs hors de cet intervalle sont ramenées dedans
     * @param label    fournisseur du texte affiché sur la barre, ou de {@code null} pour aucun
     */
    protected GaldurProgressBar(int width, DoubleSupplier progress, Supplier<Component> label) {
        super(0, 0, width, GaldurTheme.progressHeight(), Component.empty());
        this.progress = progress;
        this.label = label;
    }

    /**
     * {@return une barre de progression sans texte, positionnée en {@code (0, 0)} : c'est le layout
     * qui la place}
     *
     * @param width    largeur totale, cadre compris, en pixels
     * @param progress fournisseur de l'avancement, de {@code 0} à {@code 1}, appelé à chaque image
     */
    public static GaldurProgressBar of(int width, DoubleSupplier progress) {
        return new GaldurProgressBar(width, progress, () -> null);
    }

    /**
     * {@return une barre de progression avec un texte centré, positionnée en {@code (0, 0)}}
     *
     * @param width    largeur totale, cadre compris, en pixels
     * @param progress fournisseur de l'avancement, de {@code 0} à {@code 1}, appelé à chaque image
     * @param label    fournisseur du texte, appelé à chaque image ; {@code null} pour aucun
     */
    public static GaldurProgressBar of(int width, DoubleSupplier progress, Supplier<Component> label) {
        return new GaldurProgressBar(width, progress, label);
    }

    /**
     * {@return la largeur qui affiche entièrement {@code label} sur la barre, ou {@code minWidth}
     * si elle est plus grande}
     *
     * @param label    le texte prévu, ou {@code null} pour aucun
     * @param minWidth largeur minimale, en pixels
     */
    public static int preferredWidth(@Nullable Component label, int minWidth) {
        int text = label == null ? 0 : GaldurRender.textWidth(label) + 2 * (TEXT_PADDING + BORDER);
        return Math.max(minWidth, text);
    }

    /** {@return l'avancement lu, ramené entre {@code 0} et {@code 1} ; {@code NaN} vaut {@code 0}} */
    private double clampedProgress() {
        double value = progress.getAsDouble();
        if (Double.isNaN(value)) {
            return 0;
        }
        return Math.max(0, Math.min(1, value));
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int innerX = getX() + BORDER;
        int innerY = getY() + BORDER;
        int innerWidth = getWidth() - 2 * BORDER;
        int innerHeight = getHeight() - 2 * BORDER;
        int filled = (int) Math.round(innerWidth * clampedProgress());

        GaldurRender.drawFill(guiGraphics, innerX, innerY, innerWidth, innerHeight, GaldurTheme.background());
        if (filled > 0) {
            GaldurRender.drawFill(guiGraphics, innerX, innerY, filled, innerHeight,
                    active ? GaldurTheme.accent() : GaldurTheme.textMuted());
        }
        GaldurRender.drawOutline(guiGraphics, getX(), getY(), getWidth(), getHeight(), GaldurTheme.textMuted());

        Component text = label.get();
        if (text == null) {
            return;
        }
        int textColor = active ? GaldurTheme.text() : GaldurTheme.textMuted();
        int textY = innerY + (innerHeight - GaldurRender.lineHeight()) / 2 + 1;
        int maxWidth = innerWidth - 2 * TEXT_PADDING;
        if (GaldurRender.textWidth(text) <= maxWidth) {
            GaldurRender.text(guiGraphics, text, getX() + (getWidth() - GaldurRender.textWidth(text)) / 2,
                    textY, textColor, true);
        } else {
            GaldurRender.text(guiGraphics, GaldurRender.truncateText(text, maxWidth), innerX + TEXT_PADDING,
                    textY, textColor, true);
        }
    }

    /** Une barre de progression ne se clique pas : pas de son, pas de focus à la souris. */
    @Override
    protected boolean clicked(double mouseX, double mouseY) {
        return false;
    }

    /** Une barre de progression est sautée par la navigation au clavier (Tab, flèches). */
    @Override
    @Nullable
    public ComponentPath nextFocusPath(FocusNavigationEvent event) {
        return null;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        Component text = label.get();
        Component percent = Component.literal(Math.round(clampedProgress() * 100) + " %");
        narrationElementOutput.add(NarratedElementType.TITLE, text != null ? text : percent);
        if (text != null) {
            narrationElementOutput.add(NarratedElementType.POSITION, percent);
        }
    }
}
