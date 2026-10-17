package com.galdurstudio.galdurui.api.widget;

import com.galdurstudio.galdurui.internal.narration.GaldurNarration;
import com.galdurstudio.galdurui.api.text.GaldurFormat;
import com.galdurstudio.galdurui.api.theme.GaldurTheme;
import com.galdurstudio.galdurui.internal.render.GaldurRender;
import com.galdurstudio.galdurui.internal.render.GaldurSprites;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

/**
 * Grille d'objets défilante aux couleurs de la charte, pour afficher des milliers d'objets avec
 * leur quantité : le contenu d'un réseau de stockage, un catalogue, une liste de recettes...
 *
 * <p>Ce ne sont <strong>pas</strong> de vrais slots de menu : la grille ne fait qu'afficher une
 * liste d'{@link Entry} et signaler les clics. C'est au mod de tenir la liste à jour (par exemple
 * depuis des paquets du serveur), de la filtrer et de la trier, et d'envoyer au serveur ce que
 * le joueur veut faire d'un clic. La liste est <strong>relue à chaque image</strong> : elle peut
 * changer à tout moment sans recréer le widget.</p>
 *
 * <pre>{@code
 * List<GaldurItemGrid.Entry> contenu = ...;   // mis à jour par le mod
 * GaldurItemGrid grille = GaldurItemGrid.of(9, 6, () -> contenu)
 *         .onClick((index, entry, button, shift) -> demanderAuServeur(entry, button, shift));
 * }</pre>
 *
 * <ul>
 *   <li><strong>Rendu</strong> : seules les rangées visibles sont dessinées (colonnes × rangées
 *       cases), quelle que soit la taille de la liste. Chaque case reprend le sprite {@code slot}
 *       et la quantité abrégée de {@link GaldurItemSlot} (« 64 », « 1,2k », « 64k »).</li>
 *   <li><strong>Défilement</strong> : par rangée entière, à la molette (une rangée par cran) ou à
 *       la barre de défilement, qu'on peut saisir et glisser. La barre n'apparaît que si les
 *       objets ne tiennent pas.</li>
 *   <li><strong>Survol</strong> : contour {@code accent_hover}, voile {@code accent} et
 *       infobulle de l'objet, avec la quantité exacte au-delà de 1 000.</li>
 *   <li><strong>Clic</strong> : gauche ou droit, avec l'état de Maj, sur une case occupée.</li>
 *   <li><strong>Désactivée</strong> ({@code active = false}) : quantités atténuées, ni clic ni
 *       survol ; le défilement reste possible pour consulter.</li>
 *   <li><strong>JEI / EMI</strong> : si l'un d'eux est installé, survoler un objet puis appuyer
 *       sur R ou U montre ses recettes, sur n'importe quel écran.</li>
 * </ul>
 *
 * <p>Tailles lues dans le thème : {@code spacing.scrollbar_width}. Une case fait 18 × 18 pixels,
 * comme un slot vanilla.</p>
 */
public class GaldurItemGrid extends AbstractWidget {
    /** Côté d'une case, cadre compris. */
    public static final int CELL_SIZE = GaldurItemSlot.SIZE;
    /** Écart entre la dernière colonne et la barre de défilement. */
    private static final int SCROLLBAR_GAP = 2;
    /** Hauteur minimale du curseur de la barre de défilement. */
    private static final int MIN_THUMB_HEIGHT = 8;
    /** Opacité du voile posé sur la case survolée (0 à 255). */
    private static final int HOVER_ALPHA = 0x60;

    /**
     * Un objet affiché par la grille.
     *
     * @param stack  l'objet ; sa propre quantité est ignorée, c'est {@code amount} qui est affichée
     * @param amount la quantité à afficher ; {@code 1} n'affiche rien, comme en vanilla, et
     *               {@code 0} ou moins non plus (objet connu mais absent, à fabriquer...)
     */
    public record Entry(ItemStack stack, long amount) {
    }

    /** Action déclenchée par un clic sur une case occupée. */
    @FunctionalInterface
    public interface ClickHandler {
        /**
         * @param index  l'index de l'objet dans la liste
         * @param entry  l'objet cliqué
         * @param button le bouton de souris ({@code 0} gauche, {@code 1} droit)
         * @param shift  {@code true} si Maj était enfoncée
         */
        void onClick(int index, Entry entry, int button, boolean shift);
    }

    private final int columns;
    private final int rows;
    private final Supplier<List<Entry>> entries;
    private ClickHandler onClick = (index, entry, button, shift) -> {};
    private IntConsumer onScroll = row -> {};

    /** Première rangée visible. */
    private int scrollRow;
    /** Vrai pendant que le joueur glisse le curseur de la barre de défilement. */
    private boolean draggingThumb;
    /** Ordonnée de la souris dans le curseur au moment où on l'a saisi. */
    private double grabOffset;
    /** Index de l'objet survolé à la dernière image, ou {@code -1} ; lu par la narration. */
    private int hoveredIndex = -1;

    /**
     * Crée une grille. Préférer {@link #of(int, int, Supplier)}.
     *
     * @param columns nombre de colonnes (au moins 1)
     * @param rows    nombre de rangées visibles (au moins 1)
     * @param entries fournisseur de la liste des objets, appelé à chaque image
     */
    protected GaldurItemGrid(int columns, int rows, Supplier<List<Entry>> entries) {
        super(0, 0, Math.max(1, columns) * CELL_SIZE + SCROLLBAR_GAP + GaldurTheme.scrollbarWidth(),
                Math.max(1, rows) * CELL_SIZE, Component.empty());
        this.columns = Math.max(1, columns);
        this.rows = Math.max(1, rows);
        this.entries = entries;
    }

    /**
     * {@return une grille de {@code columns} × {@code rows} cases visibles, barre de défilement
     * comprise, positionnée en {@code (0, 0)} : c'est le layout qui la place}
     *
     * @param columns nombre de colonnes (au moins 1)
     * @param rows    nombre de rangées visibles (au moins 1)
     * @param entries fournisseur de la liste des objets, appelé à chaque image
     */
    public static GaldurItemGrid of(int columns, int rows, Supplier<List<Entry>> entries) {
        return new GaldurItemGrid(columns, rows, entries);
    }

    /**
     * Définit l'action déclenchée par un clic sur une case occupée. Une seule action est
     * conservée.
     *
     * @param onClick reçoit l'index, l'objet, le bouton et l'état de Maj
     * @return cette grille, pour chaîner les appels
     */
    public GaldurItemGrid onClick(ClickHandler onClick) {
        this.onClick = onClick;
        return this;
    }

    /**
     * Définit l'action appelée quand la première rangée visible change, pour la conserver quand le
     * widget est recréé (redimensionnement). Une seule action est conservée.
     *
     * @param onScroll reçoit la nouvelle première rangée visible
     * @return cette grille, pour chaîner les appels
     */
    public GaldurItemGrid onScroll(IntConsumer onScroll) {
        this.onScroll = onScroll;
        return this;
    }

    /** {@return la première rangée visible} */
    public int getScrollRow() {
        return scrollRow;
    }

    /**
     * Fait défiler la grille, ramenée entre la première et la dernière rangée possible.
     *
     * @param row la première rangée à afficher
     */
    public void setScrollRow(int row) {
        int clamped = Mth.clamp(row, 0, maxScrollRow());
        if (clamped != scrollRow) {
            scrollRow = clamped;
            onScroll.accept(clamped);
        }
    }

    private int totalRows() {
        return Mth.positiveCeilDiv(entries.get().size(), columns);
    }

    private int maxScrollRow() {
        return Math.max(0, totalRows() - rows);
    }

    private boolean hasScrollbar() {
        return totalRows() > rows;
    }

    private int gridWidth() {
        return columns * CELL_SIZE;
    }

    private int scrollbarX() {
        return getX() + gridWidth() + SCROLLBAR_GAP;
    }

    private int thumbHeight() {
        return Mth.clamp(getHeight() * rows / Math.max(1, totalRows()), MIN_THUMB_HEIGHT, getHeight());
    }

    private int thumbY() {
        int travel = getHeight() - thumbHeight();
        int max = maxScrollRow();
        return getY() + (max == 0 ? 0 : scrollRow * travel / max);
    }

    /** {@return l'index, dans la liste, de l'objet sous ce point, ou {@code -1}} */
    private int indexAt(double mouseX, double mouseY) {
        if (mouseX < getX() || mouseX >= getX() + gridWidth() || mouseY < getY() || mouseY >= getY() + getHeight()) {
            return -1;
        }
        int column = (int) ((mouseX - getX()) / CELL_SIZE);
        int row = (int) ((mouseY - getY()) / CELL_SIZE);
        int index = (scrollRow + row) * columns + column;
        return index < entries.get().size() ? index : -1;
    }

    private boolean isOverScrollbar(double mouseX, double mouseY) {
        return hasScrollbar() && mouseX >= scrollbarX() && mouseX < scrollbarX() + GaldurTheme.scrollbarWidth()
                && mouseY >= getY() && mouseY < getY() + getHeight();
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        setScrollRow(scrollRow); // la liste a pu raccourcir depuis l'image précédente
        List<Entry> list = entries.get();
        int hovered = active && isHovered() ? indexAt(mouseX, mouseY) : -1;
        hoveredIndex = hovered;
        int countColor = active ? GaldurTheme.text() : GaldurTheme.textMuted();
        Entry hoveredEntry = null;

        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                int cellX = getX() + column * CELL_SIZE;
                int cellY = getY() + row * CELL_SIZE;
                GaldurRender.drawNineSlice(guiGraphics, GaldurSprites.SLOT, cellX, cellY, CELL_SIZE, CELL_SIZE,
                        GaldurTheme.background());
                int index = (scrollRow + row) * columns + column;
                if (index >= list.size()) {
                    continue;
                }
                Entry entry = list.get(index);
                if (index == hovered) {
                    hoveredEntry = entry;
                    GaldurRender.drawOutline(guiGraphics, cellX, cellY, CELL_SIZE, CELL_SIZE, GaldurTheme.accentHover());
                    GaldurRender.drawFill(guiGraphics, cellX + 1, cellY + 1, GaldurRender.ITEM_SIZE,
                            GaldurRender.ITEM_SIZE, (HOVER_ALPHA << 24) | (GaldurTheme.accent() & 0xFFFFFF));
                }
                GaldurRender.drawItem(guiGraphics, entry.stack(), cellX + 1, cellY + 1);
                if (entry.amount() > 1) {
                    GaldurRender.drawItemCount(guiGraphics, GaldurFormat.amount(entry.amount()), cellX + 1, cellY + 1,
                            countColor);
                }
            }
        }

        if (hasScrollbar()) {
            int barWidth = GaldurTheme.scrollbarWidth();
            GaldurRender.drawNineSlice(guiGraphics, GaldurSprites.SCROLLBAR_TRACK, scrollbarX(), getY(), barWidth,
                    getHeight(), GaldurTheme.background());
            int thumbColor = draggingThumb ? GaldurTheme.accentHover() : GaldurTheme.textMuted();
            GaldurRender.drawNineSlice(guiGraphics, GaldurSprites.SCROLLBAR_THUMB, scrollbarX(), thumbY(), barWidth,
                    thumbHeight(), thumbColor);
        }

        if (hoveredEntry != null && !hoveredEntry.stack().isEmpty()) {
            long amount = hoveredEntry.amount();
            GaldurRender.showItemTooltip(hoveredEntry.stack(), amount >= 1000
                    ? List.of(Component.literal(GaldurFormat.fullAmount(amount)).withStyle(ChatFormatting.GRAY))
                    : List.of());
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!visible || !isMouseOver(mouseX, mouseY) || !hasScrollbar() || scrollY == 0) {
            return false;
        }
        setScrollRow(scrollRow - (int) Math.signum(scrollY));
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible || !isMouseOver(mouseX, mouseY)) {
            return false;
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && isOverScrollbar(mouseX, mouseY)) {
            draggingThumb = true;
            int thumbTop = thumbY();
            if (mouseY < thumbTop || mouseY >= thumbTop + thumbHeight()) {
                grabOffset = thumbHeight() / 2.0;
                dragThumbTo(mouseY);
            } else {
                grabOffset = mouseY - thumbTop;
            }
            return true;
        }
        if (!active || (button != GLFW.GLFW_MOUSE_BUTTON_LEFT && button != GLFW.GLFW_MOUSE_BUTTON_RIGHT)) {
            return false;
        }
        int index = indexAt(mouseX, mouseY);
        if (index >= 0) {
            onClick.onClick(index, entries.get().get(index), button, Screen.hasShiftDown());
        }
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (!draggingThumb) {
            return false;
        }
        dragThumbTo(mouseY);
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        boolean wasDragging = draggingThumb;
        draggingThumb = false;
        return wasDragging;
    }

    /** Fait défiler pour que le curseur, saisi à {@code grabOffset}, suive la souris. */
    private void dragThumbTo(double mouseY) {
        int travel = getHeight() - thumbHeight();
        if (travel > 0) {
            double position = (mouseY - grabOffset - getY()) / travel;
            setScrollRow((int) Math.round(position * maxScrollRow()));
        }
    }

    /**
     * {@return l'objet sous la souris, ou {@code null}} Utile à un écran qui veut, par exemple,
     * afficher sa recette dans JEI/EMI.
     *
     * @param mouseX abscisse de la souris
     * @param mouseY ordonnée de la souris
     */
    @Nullable
    public Entry getEntryAt(double mouseX, double mouseY) {
        int index = indexAt(mouseX, mouseY);
        return index >= 0 ? entries.get().get(index) : null;
    }

    /**
     * {@return le cadre, en coordonnées d'écran, de la case occupée sous ce point, ou {@code null}}
     *
     * <p>Sert aux intégrations JEI et EMI, qui ont besoin de la zone de l'objet survolé.</p>
     *
     * @param mouseX abscisse de la souris
     * @param mouseY ordonnée de la souris
     */
    @Nullable
    public Rect2i getCellAreaAt(double mouseX, double mouseY) {
        if (indexAt(mouseX, mouseY) < 0) {
            return null;
        }
        int column = (int) ((mouseX - getX()) / CELL_SIZE);
        int row = (int) ((mouseY - getY()) / CELL_SIZE);
        return new Rect2i(getX() + column * CELL_SIZE, getY() + row * CELL_SIZE, CELL_SIZE, CELL_SIZE);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        // Case survolée : « Lingot de fer, 1 234 », puis « Objet 5 sur 120 ».
        // Sinon : « Grille d'objets : 120 objets ».
        List<Entry> list = entries.get();
        Component title;
        if (hoveredIndex >= 0 && hoveredIndex < list.size()) {
            Entry entry = list.get(hoveredIndex);
            title = GaldurNarration.item(entry.stack(), entry.amount());
            narrationElementOutput.add(NarratedElementType.POSITION,
                    GaldurNarration.ours("grid.position", hoveredIndex + 1, list.size()));
        } else {
            title = list.isEmpty() ? GaldurNarration.ours("grid.empty") : GaldurNarration.ours("grid", list.size());
        }
        narrationElementOutput.add(NarratedElementType.TITLE, GaldurNarration.withState(title, active));
    }
}
