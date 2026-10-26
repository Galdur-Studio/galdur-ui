package com.galdurstudio.galdurui.api.widget;

import com.galdurstudio.galdurui.api.anim.GaldurEasing;
import com.galdurstudio.galdurui.api.anim.GaldurAnimations;
import com.galdurstudio.galdurui.api.anim.AnimatedValue;
import com.galdurstudio.galdurui.internal.narration.GaldurNarration;
import com.galdurstudio.galdurui.api.theme.GaldurTheme;
import com.galdurstudio.galdurui.internal.render.GaldurRender;
import com.galdurstudio.galdurui.internal.render.GaldurSprites;
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
 * Themed row of tabs, usable in vanilla layouts. The player switches tab with a click, or with
 * the left and right arrows.
 *
 * <p>Like {@link GaldurScrollList}, the widget does not keep the active tab: it <strong>reads it
 * every frame</strong> from a supplier and reports the tab the player picks. Showing the matching
 * content is up to the caller; in a {@code GaldurWindow}, {@code addTabs()} does it.</p>
 *
 * <pre>{@code
 * List<Component> titles = List.of(Component.literal("Storage"), Component.literal("Crafting"));
 * int[] active = {0};
 * column.addChild(GaldurTabs.of(titles, () -> active[0], index -> active[0] = index));
 * }</pre>
 *
 * <ul>
 *   <li><strong>Active tab</strong>: {@code tab_selected} sprite tinted {@code accent}, joined to
 *       the bottom line of the same colour.</li>
 *   <li><strong>Other tabs</strong>: lower, {@code tab} sprite tinted {@code surface},
 *       {@code text_muted} text; the tint moves towards the accent on hover.</li>
 *   <li><strong>Keyboard</strong>: left and right arrows while the row has focus; the active tab
 *       is then outlined in {@code accent_hover}.</li>
 *   <li><strong>Disabled</strong> ({@code active = false}): muted colours, no click or
 *       arrows.</li>
 * </ul>
 *
 * <p>Sizes from the theme: {@code spacing.tab_height} and {@code spacing.tab_padding_x}. The
 * width can exceed the tabs' (see {@link #of(int, List, IntSupplier, IntConsumer)}): the bottom
 * line then runs the full width and the tabs stay on the left.</p>
 */
public class GaldurTabs extends AbstractWidget {
    /** Gap between two tabs, in pixels. */
    private static final int TAB_GAP = 2;
    /** Thickness of the line under the tabs, in pixels. */
    private static final int UNDERLINE = 2;
    /** Downward offset of inactive tabs, so the active tab stands out. */
    private static final int INACTIVE_INSET = 2;
    /** Share of accent mixed into a hovered tab's background (0 to 1). */
    private static final double HOVER_MIX = 0.3;

    private final List<Component> titles;
    private final IntSupplier selected;
    private final IntConsumer onSelect;
    /** Shown hover amount per tab, from 0 to 1: fades the tint and the text. */
    private final AnimatedValue[] hover;

    /**
     * Prefer {@link #of(List, IntSupplier, IntConsumer)}.
     *
     * @param width    wanted width, in pixels; never less than the tabs'
     * @param titles   tab titles, in order; the list is copied
     * @param selected supplier of the active tab index, called every frame
     * @param onSelect called with the index of the tab the player picks
     */
    protected GaldurTabs(int width, List<Component> titles, IntSupplier selected, IntConsumer onSelect) {
        super(0, 0, Math.max(width, preferredWidth(titles)), GaldurTheme.tabHeight(), Component.empty());
        this.titles = List.copyOf(titles);
        this.selected = selected;
        this.onSelect = onSelect;
        this.hover = new AnimatedValue[this.titles.size()];
        for (int index = 0; index < hover.length; index++) {
            hover[index] = new AnimatedValue(0, GaldurAnimations.HOVER_MS, GaldurEasing.OUT_QUAD);
        }
    }

    /**
     * {@return a tab row just wide enough for its titles, at {@code (0, 0)} for a layout to place}
     *
     * @param titles   tab titles, in order; the list is copied
     * @param selected supplier of the active tab index, called every frame
     * @param onSelect called with the index of the tab the player picks
     */
    public static GaldurTabs of(List<Component> titles, IntSupplier selected, IntConsumer onSelect) {
        return new GaldurTabs(0, titles, selected, onSelect);
    }

    /**
     * {@return a tab row at least {@code width} pixels wide, at {@code (0, 0)}}
     *
     * <p>Useful to line the bottom line up with the width of the content shown below.</p>
     *
     * @param width    minimum width, in pixels
     * @param titles   tab titles, in order; the list is copied
     * @param selected supplier of the active tab index, called every frame
     * @param onSelect called with the index of the tab the player picks
     */
    public static GaldurTabs of(int width, List<Component> titles, IntSupplier selected, IntConsumer onSelect) {
        return new GaldurTabs(width, titles, selected, onSelect);
    }

    /**
     * {@return the width that shows all tabs side by side, gaps included}
     *
     * @param titles the tab titles
     */
    public static int preferredWidth(List<Component> titles) {
        int total = 0;
        for (Component title : titles) {
            total += tabWidth(title);
        }
        return total + TAB_GAP * Math.max(0, titles.size() - 1);
    }

    /** {@return the tab titles, read-only} */
    public List<Component> getTitles() {
        return titles;
    }

    /** {@return the width of one tab: its title plus the theme padding} */
    private static int tabWidth(Component title) {
        return GaldurRender.textWidth(title) + 2 * GaldurTheme.tabPaddingX();
    }

    /** {@return the index of the tab under this point, or {@code -1} if none} */
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

    /** {@return the colour at {@code t} (0 to 1) between {@code from} and {@code to}, per channel} */
    private static int mix(int from, int to, double t) {
        int result = 0;
        for (int shift = 0; shift <= 24; shift += 8) {
            int a = (from >>> shift) & 0xFF;
            int b = (to >>> shift) & 0xFF;
            result |= ((int) Math.round(a + (b - a) * t) & 0xFF) << shift;
        }
        return result;
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
            hover[index].setTarget(!isSelected && index == hovered ? 1 : 0);
            double amount = hover[index].get();
            int textColor;
            if (isSelected) {
                GaldurRender.drawNineSlice(guiGraphics, GaldurSprites.TAB_SELECTED, x, top, width, height, activeFill);
                textColor = active ? GaldurTheme.text() : GaldurTheme.textMuted();
            } else {
                int tint = mix(GaldurTheme.surface(), GaldurTheme.accent(), HOVER_MIX * amount);
                GaldurRender.drawNineSlice(guiGraphics, GaldurSprites.TAB, x, top, width, height, tint);
                textColor = mix(GaldurTheme.textMuted(), GaldurTheme.text(), amount);
            }
            int textY = top + (height - GaldurRender.lineHeight()) / 2 + 1;
            GaldurRender.text(guiGraphics, title, x + GaldurTheme.tabPaddingX(), textY, textColor, false);
            if (isSelected && keyboardFocused) {
                GaldurRender.drawOutline(guiGraphics, x, top, width, getHeight(), GaldurTheme.accentHover());
            }
            x += width + TAB_GAP;
        }
    }

    /** Only a click on a tab counts: no sound or focus on the line or the empty space. */
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
        // "Storage tab", then "Tab 1 of 3 selected" (vanilla texts).
        int index = selected.getAsInt();
        Component title;
        if (index >= 0 && index < titles.size()) {
            title = Component.translatable("gui.narrate.tab", titles.get(index));
            narrationElementOutput.add(NarratedElementType.POSITION,
                    Component.translatable("narrator.position.tab", index + 1, titles.size()));
        } else {
            title = GaldurNarration.ours("tabs", titles.size());
        }
        narrationElementOutput.add(NarratedElementType.TITLE, GaldurNarration.withState(title, active));
        if (active && isFocused() && titles.size() > 1) {
            narrationElementOutput.add(NarratedElementType.USAGE, GaldurNarration.ours("tabs.usage"));
        }
    }
}
