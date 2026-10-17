package com.galdurstudio.galdurui.api.widget;

import com.galdurstudio.galdurui.api.anim.GaldurAnimations;
import com.galdurstudio.galdurui.internal.narration.GaldurNarration;
import com.galdurstudio.galdurui.api.theme.GaldurTheme;
import com.galdurstudio.galdurui.internal.render.GaldurRender;
import com.galdurstudio.galdurui.internal.render.GaldurSprites;
import java.util.List;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

/**
 * Liste de lignes de texte qui défile, aux couleurs de la charte, utilisable dans les layouts
 * vanilla. Le joueur sélectionne une ligne d'un clic, ou avec les flèches haut et bas.
 *
 * <p>Comme {@link GaldurCheckbox}, le widget ne garde ni les lignes ni la sélection : il les
 * <strong>relit à chaque image</strong> auprès de fournisseurs, et signale les changements
 * demandés par le joueur. Les lignes peuvent donc changer à tout moment sans recréer le widget.
 * Seule la position de défilement vit dans le widget ; {@link #onScroll(DoubleConsumer)} permet de
 * la conserver ailleurs quand le widget est recréé (redimensionnement).</p>
 *
 * <pre>{@code
 * List<Component> minerais = List.of(Component.literal("Fer"), Component.literal("Or"));
 * int[] choix = {-1};
 * colonne.addChild(GaldurScrollList.of(120, 5, () -> minerais,
 *         () -> choix[0], index -> choix[0] = index));
 * }</pre>
 *
 * <ul>
 *   <li><strong>Molette</strong> : défile d'une ligne par cran.</li>
 *   <li><strong>Barre de défilement</strong> : affichée seulement si les lignes ne tiennent pas ;
 *       on peut la saisir et la glisser.</li>
 *   <li><strong>Clavier</strong> : flèches haut et bas pour changer de sélection, la liste
 *       défile pour garder la ligne sélectionnée visible.</li>
 *   <li><strong>Désactivée</strong> ({@code active = false}) : texte atténué, ni clic ni
 *       sélection, ni défilement.</li>
 * </ul>
 *
 * <p>Le texte d'une ligne trop longue est coupé au bord de la liste (voir
 * {@code GaldurRender.drawClipped}). Tailles lues dans le thème : {@code spacing.list_row_height}
 * et {@code spacing.scrollbar_width}.</p>
 */
public class GaldurScrollList extends AbstractWidget {
    /** Épaisseur du cadre, en pixels. */
    private static final int BORDER = 1;
    /** Marge entre le bord gauche d'une ligne et son texte, en pixels. */
    private static final int TEXT_PADDING = 3;
    /** Hauteur minimale du curseur de la barre de défilement, en pixels. */
    private static final int MIN_THUMB_HEIGHT = 8;
    /** Opacité du surlignage de la ligne survolée (0 à 255). */
    private static final int HOVER_ALPHA = 0x40;

    private final int visibleRows;
    private final Supplier<List<Component>> lines;
    private final IntSupplier selected;
    private final IntConsumer onSelect;
    private DoubleConsumer onScroll = amount -> {};

    /** Défilement, en pixels depuis le haut du contenu. */
    private double scroll;
    /**
     * Défilement affiché, qui rattrape {@link #scroll} en douceur (molette, flèches) ;
     * {@code NaN} avant la première image. Les clics se basent dessus : on clique ce qu'on voit.
     */
    private double shownScroll = Double.NaN;
    /** Heure de l'image précédente, pour le défilement fluide. */
    private long lastFrame;
    /** Vrai pendant que le joueur glisse le curseur de la barre de défilement. */
    private boolean draggingThumb;

    /**
     * Crée une liste. Préférer {@link #of(int, int, Supplier, IntSupplier, IntConsumer)}.
     *
     * @param width       largeur totale, cadre et barre de défilement compris, en pixels
     * @param visibleRows nombre de lignes visibles à la fois (au moins 1)
     * @param lines       fournisseur des lignes, appelé à chaque image
     * @param selected    fournisseur de l'index de la ligne sélectionnée, ou {@code -1} pour aucune
     * @param onSelect    appelée avec l'index de la ligne que le joueur sélectionne
     */
    protected GaldurScrollList(int width, int visibleRows, Supplier<List<Component>> lines,
                               IntSupplier selected, IntConsumer onSelect) {
        super(0, 0, width, Math.max(1, visibleRows) * GaldurTheme.listRowHeight() + 2 * BORDER,
                Component.empty());
        this.visibleRows = Math.max(1, visibleRows);
        this.lines = lines;
        this.selected = selected;
        this.onSelect = onSelect;
    }

    /**
     * {@return une liste défilante, positionnée en {@code (0, 0)} : c'est le layout qui la place}
     *
     * @param width       largeur totale, cadre et barre de défilement compris, en pixels
     * @param visibleRows nombre de lignes visibles à la fois (au moins 1) ; fixe la hauteur
     * @param lines       fournisseur des lignes, appelé à chaque image
     * @param selected    fournisseur de l'index de la ligne sélectionnée, ou {@code -1} pour aucune
     * @param onSelect    appelée avec l'index de la ligne que le joueur sélectionne
     */
    public static GaldurScrollList of(int width, int visibleRows, Supplier<List<Component>> lines,
                                      IntSupplier selected, IntConsumer onSelect) {
        return new GaldurScrollList(width, visibleRows, lines, selected, onSelect);
    }

    /**
     * {@return la largeur qui affiche entièrement la plus longue des lignes données, barre de
     * défilement comprise, ou {@code minWidth} si elle est plus grande}
     *
     * @param lines    les lignes à afficher
     * @param minWidth largeur minimale, en pixels
     */
    public static int preferredWidth(List<Component> lines, int minWidth) {
        int widest = 0;
        for (Component line : lines) {
            widest = Math.max(widest, GaldurRender.textWidth(line));
        }
        return Math.max(minWidth, widest + 2 * TEXT_PADDING + 2 * BORDER + GaldurTheme.scrollbarWidth());
    }

    /**
     * Définit l'action appelée quand le défilement change, par le joueur ou par le code. Une
     * seule action est conservée.
     *
     * @param onScroll reçoit le nouveau défilement, en pixels depuis le haut du contenu
     * @return cette liste, pour chaîner les appels
     */
    public GaldurScrollList onScroll(DoubleConsumer onScroll) {
        this.onScroll = onScroll;
        return this;
    }

    /** {@return le défilement, en pixels depuis le haut du contenu} */
    public double getScroll() {
        return scroll;
    }

    /**
     * Fait défiler la liste, ramenée entre le haut et le bas du contenu.
     *
     * @param scroll défilement voulu, en pixels depuis le haut du contenu
     */
    public void setScroll(double scroll) {
        double clamped = Mth.clamp(scroll, 0, maxScroll());
        if (clamped != this.scroll) {
            this.scroll = clamped;
            onScroll.accept(clamped);
        }
    }

    /**
     * Fait défiler juste assez pour que la ligne {@code index} soit entièrement visible.
     *
     * @param index l'index de la ligne ; sans effet s'il est hors de la liste
     */
    public void scrollToLine(int index) {
        if (index < 0 || index >= lines.get().size()) {
            return;
        }
        int rowHeight = GaldurTheme.listRowHeight();
        int top = index * rowHeight;
        int bottom = top + rowHeight;
        if (top < scroll) {
            setScroll(top);
        } else if (bottom > scroll + innerHeight()) {
            setScroll(bottom - innerHeight());
        }
    }

    private int innerX() {
        return getX() + BORDER;
    }

    private int innerY() {
        return getY() + BORDER;
    }

    private int innerHeight() {
        return getHeight() - 2 * BORDER;
    }

    private int contentHeight() {
        return lines.get().size() * GaldurTheme.listRowHeight();
    }

    private int maxScroll() {
        return Math.max(0, contentHeight() - innerHeight());
    }

    private boolean hasScrollbar() {
        return contentHeight() > innerHeight();
    }

    /** {@return la largeur des lignes : tout l'intérieur, moins la barre si elle est affichée} */
    private int rowsWidth() {
        int inner = getWidth() - 2 * BORDER;
        return hasScrollbar() ? inner - GaldurTheme.scrollbarWidth() : inner;
    }

    private int scrollbarX() {
        return innerX() + rowsWidth();
    }

    private int thumbHeight() {
        int track = innerHeight();
        return Mth.clamp(track * track / Math.max(1, contentHeight()), MIN_THUMB_HEIGHT, track);
    }

    private int thumbY() {
        int travel = innerHeight() - thumbHeight();
        int max = maxScroll();
        return innerY() + (max == 0 ? 0 : (int) (view() * travel / max));
    }

    /** {@return l'index de la ligne sous ce point, ou {@code -1} s'il n'y en a pas} */
    private int rowAt(double mouseX, double mouseY) {
        if (mouseX < innerX() || mouseX >= innerX() + rowsWidth()
                || mouseY < innerY() || mouseY >= innerY() + innerHeight()) {
            return -1;
        }
        int index = (int) ((mouseY - innerY() + view()) / GaldurTheme.listRowHeight());
        return index < lines.get().size() ? index : -1;
    }

    private boolean isOverScrollbar(double mouseX, double mouseY) {
        return hasScrollbar() && mouseX >= scrollbarX() && mouseX < scrollbarX() + GaldurTheme.scrollbarWidth()
                && mouseY >= innerY() && mouseY < innerY() + innerHeight();
    }

    /** {@return le défilement affiché, ou le défilement réel avant la première image} */
    private double view() {
        return Double.isNaN(shownScroll) ? scroll : shownScroll;
    }

    /** Rapproche le défilement affiché du défilement réel ; d'un coup quand on tire le curseur. */
    private void updateShownScroll() {
        long now = GaldurAnimations.now();
        shownScroll = draggingThumb
                ? scroll
                : GaldurAnimations.approach(shownScroll, scroll, now - lastFrame,
                        GaldurAnimations.SCROLL_SMOOTHING_MS, 0.25);
        lastFrame = now;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        setScroll(scroll);
        updateShownScroll();
        double view = view();
        List<Component> rows = lines.get();
        int rowHeight = GaldurTheme.listRowHeight();
        int rowsWidth = rowsWidth();
        int selectedIndex = selected.getAsInt();
        int hovered = active && isHovered() ? rowAt(mouseX, mouseY) : -1;
        int textColor = active ? GaldurTheme.text() : GaldurTheme.textMuted();
        boolean keyboardFocused = isFocused() && Minecraft.getInstance().getLastInputType().isKeyboard();

        GaldurRender.drawNineSlice(guiGraphics, GaldurSprites.FIELD, getX(), getY(), getWidth(), getHeight(),
                GaldurTheme.surface());
        GaldurRender.drawClipped(guiGraphics, innerX(), innerY(), rowsWidth, innerHeight(), () -> {
            int first = (int) (view / rowHeight);
            int last = Math.min(rows.size() - 1, (int) ((view + innerHeight()) / rowHeight));
            for (int index = first; index <= last; index++) {
                int rowY = innerY() + index * rowHeight - (int) Math.round(view);
                if (index == selectedIndex) {
                    GaldurRender.drawFill(guiGraphics, innerX(), rowY, rowsWidth, rowHeight,
                            active ? GaldurTheme.accent() : GaldurTheme.background());
                } else if (index == hovered) {
                    GaldurRender.drawFill(guiGraphics, innerX(), rowY, rowsWidth, rowHeight,
                            (HOVER_ALPHA << 24) | (GaldurTheme.accent() & 0xFFFFFF));
                }
                int textY = rowY + (rowHeight - GaldurRender.lineHeight()) / 2 + 1;
                GaldurRender.text(guiGraphics, rows.get(index), innerX() + TEXT_PADDING, textY, textColor, false);
            }
        });

        if (hasScrollbar()) {
            int barWidth = GaldurTheme.scrollbarWidth();
            GaldurRender.drawNineSlice(guiGraphics, GaldurSprites.SCROLLBAR_TRACK, scrollbarX(), innerY(), barWidth,
                    innerHeight(), GaldurTheme.background());
            int thumbColor = draggingThumb ? GaldurTheme.accentHover() : GaldurTheme.textMuted();
            GaldurRender.drawNineSlice(guiGraphics, GaldurSprites.SCROLLBAR_THUMB, scrollbarX(), thumbY(), barWidth,
                    thumbHeight(), thumbColor);
        }

        if (active && (keyboardFocused || draggingThumb)) {
            GaldurRender.drawOutline(guiGraphics, getX(), getY(), getWidth(), getHeight(), GaldurTheme.accentHover());
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        boolean over = mouseX >= getX() && mouseX < getX() + getWidth()
                && mouseY >= getY() && mouseY < getY() + getHeight();
        if (!visible || !over || !hasScrollbar()) {
            return false;
        }
        setScroll(scroll - scrollY * GaldurTheme.listRowHeight());
        return true;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (isOverScrollbar(mouseX, mouseY)) {
            draggingThumb = true;
            int thumbTop = thumbY();
            if (mouseY < thumbTop || mouseY >= thumbTop + thumbHeight()) {
                scrollThumbTo(mouseY - thumbHeight() / 2.0);
            }
            return;
        }
        int index = rowAt(mouseX, mouseY);
        if (index >= 0 && index != selected.getAsInt()) {
            onSelect.accept(index);
        }
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
        if (draggingThumb) {
            int travel = innerHeight() - thumbHeight();
            if (travel > 0) {
                setScroll(scroll + dragY * maxScroll() / travel);
            }
        }
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        draggingThumb = false;
    }

    /** Place le haut du curseur à l'ordonnée {@code thumbTop} et fait défiler en conséquence. */
    private void scrollThumbTo(double thumbTop) {
        int travel = innerHeight() - thumbHeight();
        if (travel > 0) {
            setScroll((thumbTop - innerY()) * maxScroll() / travel);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!active || !visible) {
            return false;
        }
        int count = lines.get().size();
        if (count == 0 || (keyCode != GLFW.GLFW_KEY_UP && keyCode != GLFW.GLFW_KEY_DOWN)) {
            return false;
        }
        int current = selected.getAsInt();
        int next;
        if (current < 0 || current >= count) {
            next = keyCode == GLFW.GLFW_KEY_DOWN ? 0 : count - 1;
        } else {
            next = Mth.clamp(current + (keyCode == GLFW.GLFW_KEY_DOWN ? 1 : -1), 0, count - 1);
        }
        if (next != current) {
            onSelect.accept(next);
        }
        scrollToLine(next);
        return true;
    }

    /** Une liste ne fait pas le son d'un bouton quand on clique une ligne. */
    @Override
    public void playDownSound(SoundManager handler) {
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        // Ligne choisie : « Fer », puis « Ligne 2 sur 5 sélectionnée » (texte vanilla).
        // Sinon : « Liste : 5 lignes ».
        List<Component> rows = lines.get();
        int index = selected.getAsInt();
        Component title;
        if (index >= 0 && index < rows.size()) {
            title = rows.get(index);
            narrationElementOutput.add(NarratedElementType.POSITION,
                    Component.translatable("narrator.position.list", index + 1, rows.size()));
        } else {
            title = rows.isEmpty() ? GaldurNarration.ours("list.empty") : GaldurNarration.ours("list", rows.size());
        }
        narrationElementOutput.add(NarratedElementType.TITLE, GaldurNarration.withState(title, active));
        if (active && !rows.isEmpty()) {
            narrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.selection.usage"));
        }
    }
}
