package com.galdurstudio.galdurui.api.widget;

import com.galdurstudio.galdurui.api.theme.GaldurTheme;
import com.galdurstudio.galdurui.internal.render.GaldurRender;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

/**
 * Rangée d'onglets aux couleurs de la charte, utilisable dans les layouts vanilla. Le joueur
 * change d'onglet d'un clic, ou avec les flèches gauche et droite.
 *
 * <p>Comme {@link GaldurScrollList}, le widget ne garde pas l'onglet actif : il le
 * <strong>relit à chaque image</strong> auprès d'un fournisseur, et signale l'onglet que le joueur
 * choisit. C'est au code appelant d'afficher le contenu qui correspond ; dans une
 * {@code GaldurWindow}, {@code addTabs()} s'en charge.</p>
 *
 * <pre>{@code
 * List<Component> titres = List.of(Component.literal("Stockage"), Component.literal("Artisanat"));
 * int[] actif = {0};
 * colonne.addChild(GaldurTabs.of(titres, () -> actif[0], index -> actif[0] = index));
 * }</pre>
 *
 * <ul>
 *   <li><strong>Onglet actif</strong> : rempli en {@code accent}, relié au trait du bas de la
 *       même couleur.</li>
 *   <li><strong>Autres onglets</strong> : plus bas, en {@code surface}, texte en
 *       {@code text_muted} ; surlignés au survol.</li>
 *   <li><strong>Clavier</strong> : flèches gauche et droite quand la rangée a le focus ; l'onglet
 *       actif est alors entouré en {@code accent_hover}.</li>
 *   <li><strong>Désactivée</strong> ({@code active = false}) : couleurs atténuées, ni clic ni
 *       flèches.</li>
 * </ul>
 *
 * <p>Tailles lues dans le thème : {@code spacing.tab_height} et {@code spacing.tab_padding_x}.
 * La largeur peut dépasser celle des onglets (voir {@link #of(int, List, IntSupplier, IntConsumer)}) :
 * le trait du bas court alors sur toute la largeur, les onglets restent à gauche.</p>
 */
public class GaldurTabs extends AbstractWidget {
    /** Écart entre deux onglets, en pixels. */
    private static final int TAB_GAP = 2;
    /** Épaisseur du trait sous les onglets, en pixels. */
    private static final int UNDERLINE = 2;
    /** Décalage vers le bas des onglets inactifs, pour que l'onglet actif ressorte. */
    private static final int INACTIVE_INSET = 2;
    /** Opacité du surlignage d'un onglet survolé (0 à 255). */
    private static final int HOVER_ALPHA = 0x40;

    private final List<Component> titles;
    private final IntSupplier selected;
    private final IntConsumer onSelect;

    /**
     * Crée une rangée d'onglets. Préférer {@link #of(List, IntSupplier, IntConsumer)}.
     *
     * @param width    largeur voulue, en pixels ; jamais moins que celle des onglets
     * @param titles   les titres des onglets, dans l'ordre ; la liste est copiée
     * @param selected fournisseur de l'index de l'onglet actif, appelé à chaque image
     * @param onSelect appelée avec l'index de l'onglet que le joueur choisit
     */
    protected GaldurTabs(int width, List<Component> titles, IntSupplier selected, IntConsumer onSelect) {
        super(0, 0, Math.max(width, preferredWidth(titles)), GaldurTheme.tabHeight(), Component.empty());
        this.titles = List.copyOf(titles);
        this.selected = selected;
        this.onSelect = onSelect;
    }

    /**
     * {@return une rangée d'onglets juste assez large pour ses titres, positionnée en {@code (0, 0)} :
     * c'est le layout qui la place}
     *
     * @param titles   les titres des onglets, dans l'ordre ; la liste est copiée
     * @param selected fournisseur de l'index de l'onglet actif, appelé à chaque image
     * @param onSelect appelée avec l'index de l'onglet que le joueur choisit
     */
    public static GaldurTabs of(List<Component> titles, IntSupplier selected, IntConsumer onSelect) {
        return new GaldurTabs(0, titles, selected, onSelect);
    }

    /**
     * {@return une rangée d'onglets d'au moins {@code width} pixels de large, positionnée en
     * {@code (0, 0)}}
     *
     * <p>Utile pour aligner le trait du bas sur la largeur du contenu affiché dessous.</p>
     *
     * @param width    largeur minimale, en pixels
     * @param titles   les titres des onglets, dans l'ordre ; la liste est copiée
     * @param selected fournisseur de l'index de l'onglet actif, appelé à chaque image
     * @param onSelect appelée avec l'index de l'onglet que le joueur choisit
     */
    public static GaldurTabs of(int width, List<Component> titles, IntSupplier selected, IntConsumer onSelect) {
        return new GaldurTabs(width, titles, selected, onSelect);
    }

    /**
     * {@return la largeur qui affiche tous les onglets côte à côte, écarts compris}
     *
     * @param titles les titres des onglets
     */
    public static int preferredWidth(List<Component> titles) {
        int total = 0;
        for (Component title : titles) {
            total += tabWidth(title);
        }
        return total + TAB_GAP * Math.max(0, titles.size() - 1);
    }

    /** {@return les titres des onglets, en lecture seule} */
    public List<Component> getTitles() {
        return titles;
    }

    /** {@return la largeur d'un onglet : son titre et les marges du thème} */
    private static int tabWidth(Component title) {
        return GaldurRender.textWidth(title) + 2 * GaldurTheme.tabPaddingX();
    }

    /** {@return l'index de l'onglet sous ce point, ou {@code -1} s'il n'y en a pas} */
    private int tabAt(double mouseX, double mouseY) {
        if (mouseY < getY() || mouseY >= getY() + getHeight()) {
            return -1;
        }
        int x = getX();
        for (int index = 0; index < titles.size(); index++) {
            int width = tabWidth(titles.get(index));
            if (mouseX >= x && mouseX < x + width) {
                return index;
            }
            x += width + TAB_GAP;
        }
        return -1;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int selectedIndex = selected.getAsInt();
        int hovered = active && isHovered() ? tabAt(mouseX, mouseY) : -1;
        boolean keyboardFocused = active && isFocused() && Minecraft.getInstance().getLastInputType().isKeyboard();
        int lineY = getY() + getHeight() - UNDERLINE;
        int activeFill = active ? GaldurTheme.accent() : GaldurTheme.background();

        GaldurRender.drawFill(guiGraphics, getX(), lineY, getWidth(), UNDERLINE,
                active ? GaldurTheme.accent() : GaldurTheme.textMuted());

        int x = getX();
        for (int index = 0; index < titles.size(); index++) {
            Component title = titles.get(index);
            int width = tabWidth(title);
            boolean isSelected = index == selectedIndex;
            int top = isSelected ? getY() : getY() + INACTIVE_INSET;
            int height = lineY - top;
            int textColor;
            if (isSelected) {
                GaldurRender.drawFill(guiGraphics, x, top, width, height, activeFill);
                textColor = active ? GaldurTheme.text() : GaldurTheme.textMuted();
            } else {
                GaldurRender.drawFill(guiGraphics, x, top, width, height, GaldurTheme.surface());
                if (index == hovered) {
                    GaldurRender.drawFill(guiGraphics, x, top, width, height,
                            (HOVER_ALPHA << 24) | (GaldurTheme.accent() & 0xFFFFFF));
                }
                textColor = index == hovered ? GaldurTheme.text() : GaldurTheme.textMuted();
            }
            int textY = top + (height - GaldurRender.lineHeight()) / 2 + 1;
            GaldurRender.text(guiGraphics, title, x + GaldurTheme.tabPaddingX(), textY, textColor, false);
            if (isSelected && keyboardFocused) {
                GaldurRender.drawOutline(guiGraphics, x, top, width, getHeight(), GaldurTheme.accentHover());
            }
            x += width + TAB_GAP;
        }
    }

    /** Seul un clic sur un onglet compte : pas de son ni de focus sur le trait ou l'espace vide. */
    @Override
    protected boolean clicked(double mouseX, double mouseY) {
        return super.clicked(mouseX, mouseY) && tabAt(mouseX, mouseY) >= 0;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        int index = tabAt(mouseX, mouseY);
        if (index >= 0 && index != selected.getAsInt()) {
            onSelect.accept(index);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!active || !visible || titles.isEmpty()
                || (keyCode != GLFW.GLFW_KEY_LEFT && keyCode != GLFW.GLFW_KEY_RIGHT)) {
            return false;
        }
        int current = selected.getAsInt();
        int step = keyCode == GLFW.GLFW_KEY_RIGHT ? 1 : -1;
        int next = current < 0 || current >= titles.size() ? 0 : Mth.clamp(current + step, 0, titles.size() - 1);
        if (next != current) {
            onSelect.accept(next);
            playDownSound(Minecraft.getInstance().getSoundManager());
        }
        return true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        int index = selected.getAsInt();
        if (index >= 0 && index < titles.size()) {
            narrationElementOutput.add(NarratedElementType.TITLE, titles.get(index));
            narrationElementOutput.add(NarratedElementType.POSITION,
                    Component.literal("Onglet " + (index + 1) + " / " + titles.size()));
        } else {
            narrationElementOutput.add(NarratedElementType.TITLE, Component.literal(titles.size() + " onglets"));
        }
    }
}
