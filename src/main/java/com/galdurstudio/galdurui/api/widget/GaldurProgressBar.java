package com.galdurstudio.galdurui.api.widget;

import com.galdurstudio.galdurui.api.theme.GaldurTheme;
import com.galdurstudio.galdurui.internal.render.GaldurRender;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;
import net.minecraft.Util;
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
 *   <li><strong>Remplissage</strong> : de gauche à droite, en dégradé de {@code accent}
 *       (redstone) à {@code ember} (braise) sur toute la largeur : la tête de la barre se
 *       réchauffe à mesure qu'elle avance. Fond {@code background}, cadre {@code text_muted}, et
 *       la colonne de tête est éclaircie pour marquer le front.</li>
 *   <li><strong>Animation</strong> : la barre glisse vers sa nouvelle valeur au lieu de sauter
 *       (elle revient en arrière d'un coup, pour qu'une remise à zéro soit nette), et un éclat
 *       lumineux parcourt la partie remplie tant qu'elle n'est pas pleine.</li>
 *   <li><strong>Texte</strong> : centré, en {@code text} avec ombre ; s'il est trop long, il est
 *       coupé avec « … ». Sans texte ({@code null}), la barre est seule.</li>
 *   <li><strong>Désactivée</strong> ({@code active = false}) : remplissage uni et texte en
 *       {@code text_muted}, sans animation.</li>
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
    /** Temps caractéristique du glissement vers la nouvelle valeur, en millisecondes. */
    private static final double SMOOTHING_MS = 90;
    /** Largeur de l'éclat qui parcourt la barre, en pixels. */
    private static final int SHINE_WIDTH = 18;
    /** Durée d'un passage de l'éclat, de gauche à droite, en millisecondes. */
    private static final long SHINE_PERIOD_MS = 1600;
    /** Intensité maximale de l'éclat, de 0 (invisible) à 1 (blanc). */
    private static final double SHINE_STRENGTH = 0.45;
    /** Éclaircissement de la colonne de tête, de 0 à 1. */
    private static final double HEAD_STRENGTH = 0.35;

    private final DoubleSupplier progress;
    private final Supplier<Component> label;

    /** Avancement affiché, qui rattrape l'avancement réel ; {@code NaN} avant la première image. */
    private double shown = Double.NaN;
    /** Heure de l'image précédente, en millisecondes ({@code Util.getMillis()}). */
    private long lastFrame;

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

    /**
     * Fait avancer l'avancement affiché vers l'avancement réel et le renvoie.
     *
     * <p>Lissage exponentiel indépendant du nombre d'images par seconde. Une baisse est appliquée
     * tout de suite ; une barre désactivée n'est pas animée.</p>
     */
    private double updateShown() {
        double target = clampedProgress();
        long now = Util.getMillis();
        if (Double.isNaN(shown) || target < shown || !active) {
            shown = target;
        } else {
            double elapsed = Math.max(0, now - lastFrame);
            shown += (target - shown) * (1 - Math.exp(-elapsed / SMOOTHING_MS));
            if (target - shown < 0.001) {
                shown = target;
            }
        }
        lastFrame = now;
        return shown;
    }

    /** {@return la couleur à mi-chemin {@code t} (0 à 1) entre {@code from} et {@code to}, canal par canal} */
    private static int lerpColor(int from, int to, double t) {
        int result = 0;
        for (int shift = 0; shift <= 24; shift += 8) {
            int a = (from >>> shift) & 0xFF;
            int b = (to >>> shift) & 0xFF;
            result |= ((int) Math.round(a + (b - a) * t) & 0xFF) << shift;
        }
        return result;
    }

    /**
     * {@return l'intensité de l'éclat dans la colonne {@code column} de la partie remplie, de 0 à 1}
     *
     * <p>L'éclat part juste avant le bord gauche et sort après la tête, puis recommence ; son
     * intensité décroît linéairement depuis son centre.</p>
     */
    private static double shineAt(int column, int filled, long now) {
        double travel = filled + SHINE_WIDTH;
        double center = (now % SHINE_PERIOD_MS) / (double) SHINE_PERIOD_MS * travel - SHINE_WIDTH / 2.0;
        double distance = Math.abs(column + 0.5 - center);
        return Math.max(0, 1 - distance / (SHINE_WIDTH / 2.0));
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int innerX = getX() + BORDER;
        int innerY = getY() + BORDER;
        int innerWidth = getWidth() - 2 * BORDER;
        int innerHeight = getHeight() - 2 * BORDER;
        double value = updateShown();
        int filled = (int) Math.round(innerWidth * value);

        GaldurRender.drawFill(guiGraphics, innerX, innerY, innerWidth, innerHeight, GaldurTheme.background());
        if (filled > 0 && !active) {
            GaldurRender.drawFill(guiGraphics, innerX, innerY, filled, innerHeight, GaldurTheme.textMuted());
        } else if (filled > 0) {
            int from = GaldurTheme.accent();
            int to = GaldurTheme.ember();
            boolean shining = clampedProgress() < 1;
            long now = Util.getMillis();
            int white = 0xFFFFFFFF;
            for (int column = 0; column < filled; column++) {
                int color = lerpColor(from, to, innerWidth <= 1 ? 0 : column / (double) (innerWidth - 1));
                double light = shining ? shineAt(column, filled, now) * SHINE_STRENGTH : 0;
                if (column == filled - 1 && filled < innerWidth) {
                    light = Math.max(light, HEAD_STRENGTH);
                }
                if (light > 0) {
                    color = lerpColor(color, white, light);
                }
                GaldurRender.drawFill(guiGraphics, innerX + column, innerY, 1, innerHeight, color);
            }
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
