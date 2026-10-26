package com.galdurstudio.galdurui.internal.window;

import java.util.Set;
import java.util.Map;
import java.util.IdentityHashMap;
import java.util.Collections;
import net.minecraft.client.gui.GuiGraphics;
import com.galdurstudio.galdurui.internal.render.GaldurRender;
import com.galdurstudio.galdurui.api.anim.GaldurEasing;
import com.galdurstudio.galdurui.api.anim.GaldurAnimations;
import com.galdurstudio.galdurui.internal.narration.GaldurNarration;
import com.galdurstudio.galdurui.api.layout.GaldurPanelLayout;
import com.galdurstudio.galdurui.api.theme.GaldurTheme;
import com.galdurstudio.galdurui.api.widget.GaldurTabs;
import com.galdurstudio.galdurui.api.window.ElementHandle;
import com.galdurstudio.galdurui.api.window.TabsHandle;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

/**
 * Minecraft screen showing the content of a {@link WindowModel} in a centred panel.
 *
 * <p>Minecraft calls {@link #init()} again on every resize, so the layout and widgets are rebuilt
 * each time from the model's descriptions.</p>
 *
 * <p>The screen remembers the one shown before it: Escape and the close button go back to it,
 * instead of closing the whole interface.</p>
 */
public class WindowScreen extends Screen {
    private final WindowModel model;
    private final Consumer<WindowScreen> onShown;
    private final Consumer<WindowScreen> onHidden;

    /** Screen shown before this one, restored on close; {@code null} = back to the game. */
    @Nullable
    private Screen previous;

    /** Distance the window travels while opening (it rises), in pixels. */
    private static final int OPEN_RISE = 8;
    /** Distance the content of a tab travels while arriving, in pixels. */
    private static final int PAGE_SLIDE = 12;

    /** Start of the opening animation; {@code -1} while the window has never been shown. */
    private long openStart = -1;
    /** Tab shown by each tab row on the last build. */
    private final Map<TabsHandle, Integer> builtTabs = new IdentityHashMap<>();
    /** Content of the tab just picked, found during the build; {@code null} otherwise. */
    @Nullable
    private LinearLayout arrivingPage;
    /** Widgets of the arriving tab, drawn offset during {@link GaldurAnimations#PAGE_MS}. */
    private final Set<AbstractWidget> slidingWidgets = Collections.newSetFromMap(new IdentityHashMap<>());
    /** Arrival side: {@code 1} from the right (next tab), {@code -1} from the left. */
    private int pageDirection;
    /** Start of the arriving tab's slide. */
    private long pageStart;

    /**
     * @param model    the content to show
     * @param onShown  called every time the screen becomes the current one (on open, or back from
     *                 a screen opened on top)
     * @param onHidden called every time the screen stops being the current one (Escape, close
     *                 button, another screen replacing it). Both callbacks notify the window
     *                 without this package depending on {@code api}.
     */
    public WindowScreen(WindowModel model, Consumer<WindowScreen> onShown, Consumer<WindowScreen> onHidden) {
        super(GaldurNarration.ours("window"));
        this.model = model;
        this.onShown = onShown;
        this.onHidden = onHidden;
    }

    /**
     * {@return what the narrator reads on open: the window's first title ({@code addTitle}), or
     * "Galdur window" if there is none}
     */
    @Override
    public Component getNarrationMessage() {
        for (WindowElement element : model.elements()) {
            if (element instanceof TitleElement title && title.handle().isVisible()) {
                return title.title();
            }
        }
        return getTitle();
    }

    /**
     * Remembers the screen to go back to on close. Call just before showing it.
     *
     * @param previous the screen shown until now, or {@code null} if none (in game)
     */
    public void setPrevious(@Nullable Screen previous) {
        this.previous = previous;
    }

    /**
     * Rebuilds the widgets from the model, for instance after an element was added while the
     * screen is shown. Call on the client thread.
     *
     * <p>Focus goes back to the widget of the same rank: a player navigating a list with the
     * keyboard keeps it when the selection changes another element's state. If an element appears
     * or disappears before it, focus may shift by one widget.</p>
     */
    public void rebuild() {
        int focusedIndex = children().indexOf(getFocused());
        rebuildWidgets();
        if (focusedIndex >= 0 && focusedIndex < children().size()) {
            setFocused(children().get(focusedIndex));
        }
    }

    /** Called by Minecraft when the screen becomes the current one. */
    @Override
    public void added() {
        super.added();
        if (openStart < 0) {
            openStart = GaldurAnimations.now();
        }
        onShown.accept(this);
    }

    /** {@return the vertical offset of the window while it opens, in pixels} */
    private float openOffset() {
        if (openStart < 0) {
            return 0;
        }
        double t = GaldurAnimations.progress(openStart, GaldurAnimations.OPEN_MS);
        return (float) (OPEN_RISE * (1 - GaldurEasing.OUT_CUBIC.applyAsDouble(t)));
    }

    /** {@return the horizontal offset of the arriving tab content, in pixels} */
    private float pageOffset() {
        double t = GaldurAnimations.progress(pageStart, GaldurAnimations.PAGE_MS);
        return (float) (pageDirection * PAGE_SLIDE * (1 - GaldurEasing.OUT_CUBIC.applyAsDouble(t)));
    }

    /**
     * Draws the screen: the window rises slightly on open. Click areas do not move; the animation
     * is too short to notice.
     */
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        float offset = openOffset();
        GaldurRender.drawOffset(guiGraphics, 0, offset,
                () -> super.render(guiGraphics, mouseX, mouseY - Math.round(offset), partialTick));
    }

    /** The background (blur, dim) does not follow the window rising: it covers the whole screen. */
    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        float offset = openOffset();
        GaldurRender.drawOffset(guiGraphics, 0, -offset,
                () -> super.renderBackground(guiGraphics, mouseX, mouseY, partialTick));
    }

    /**
     * Tells the window the screen is no longer shown.
     *
     * <p>Minecraft calls this on every screen change, but not on a plain resize (which only calls
     * {@link #init()} again).</p>
     */
    @Override
    public void removed() {
        super.removed();
        onHidden.accept(this);
    }

    /**
     * Closes the screen and goes back to the previous one. Called by Escape and by
     * {@code GaldurWindow.close()}, so both behave the same.
     */
    @Override
    public void onClose() {
        if (minecraft != null) {
            minecraft.setScreen(previous);
        }
    }

    /**
     * Builds an element's widget with the state inherited from what contains it.
     *
     * <p>An element is enabled only if it and its row are; without a tooltip of its own, it takes
     * its row's.</p>
     *
     * @param element       the element to build
     * @param parentEnabled {@code false} if what contains the element is disabled
     * @param parentTooltip the tooltip of what contains the element, or {@code null}
     * @return the widget, or {@code null} if the element is hidden and takes no space
     */
    @Nullable
    private LayoutElement build(WindowElement element, boolean parentEnabled, @Nullable Component parentTooltip) {
        ElementHandle handle = element.handle();
        if (!handle.isVisible()) {
            return null;
        }
        boolean enabled = parentEnabled && handle.isEnabled();
        Component tooltip = handle.getTooltip() != null ? handle.getTooltip() : parentTooltip;
        if (element instanceof RowElement row) {
            return buildRow(row, enabled, tooltip);
        }
        if (element instanceof TabsElement tabs) {
            return buildTabs(tabs, enabled, tooltip);
        }
        LayoutElement widget = element.createWidget(enabled);
        if (widget instanceof AbstractWidget abstractWidget) {
            applyState(abstractWidget, enabled, tooltip);
        }
        return widget;
    }

    /**
     * Builds a row: its visible elements side by side, centred vertically on each other.
     *
     * @return the row, or {@code null} if none of its elements is visible
     */
    @Nullable
    private LayoutElement buildRow(RowElement row, boolean enabled, @Nullable Component tooltip) {
        LinearLayout line = LinearLayout.horizontal().spacing(GaldurTheme.gap());
        boolean empty = true;
        for (WindowElement child : row.children().elements()) {
            LayoutElement widget = build(child, enabled, tooltip);
            if (widget != null) {
                line.addChild(widget, LayoutSettings::alignVerticallyMiddle);
                empty = false;
            }
        }
        return empty ? null : line;
    }

    /**
     * Builds a tab row: the tabs, then below them the content of the active tab.
     *
     * <p>Each tab's content is built then measured, and the content area takes the size of the
     * largest: the window does not resize when switching tabs. Only the active tab's widgets are
     * placed in the layout, and so added to the screen; the others are only measured and
     * dropped.</p>
     *
     * <p>The row's tooltip only applies to the tabs, not to the content; the disabled state
     * applies to both.</p>
     *
     * @return the row and its content, or {@code null} if there is no tab yet
     */
    @Nullable
    private LayoutElement buildTabs(TabsElement element, boolean enabled, @Nullable Component tooltip) {
        List<Component> titles = element.tabs().titles();
        List<WindowModel> contents = element.tabs().contents();
        if (titles.isEmpty()) {
            return null;
        }
        TabsHandle handle = element.handle();
        int selected = handle.getSelected();
        Integer before = builtTabs.put(handle, selected);
        LinearLayout shown = null;
        int contentWidth = 0;
        int contentHeight = 0;
        for (int index = 0; index < contents.size(); index++) {
            LinearLayout content = buildColumn(contents.get(index), enabled);
            content.arrangeElements();
            contentWidth = Math.max(contentWidth, content.getWidth());
            contentHeight = Math.max(contentHeight, content.getHeight());
            if (index == selected) {
                shown = content;
            }
        }

        GaldurTabs bar = GaldurTabs.of(contentWidth, titles, handle::getSelected, handle::setSelected);
        applyState(bar, enabled, tooltip);
        FrameLayout area = new FrameLayout(bar.getWidth(), contentHeight);
        area.defaultChildLayoutSetting().alignHorizontallyLeft().alignVerticallyTop();
        if (shown != null) {
            area.addChild(shown);
            if (before != null && before != selected) {
                // The player just switched tab: the new content slides in from its side.
                arrivingPage = shown;
                pageDirection = selected > before ? 1 : -1;
                pageStart = GaldurAnimations.now();
            }
        }

        LinearLayout column = LinearLayout.vertical().spacing(GaldurTheme.gap());
        column.addChild(bar);
        column.addChild(area);
        return column;
    }

    /**
     * Builds elements stacked top to bottom, like the window content.
     *
     * @param model   the elements, in order
     * @param enabled {@code false} if what contains them is disabled
     * @return the column, empty if no element is visible
     */
    private LinearLayout buildColumn(WindowModel model, boolean enabled) {
        LinearLayout column = LinearLayout.vertical().spacing(GaldurTheme.gap());
        for (WindowElement element : model.elements()) {
            LayoutElement widget = build(element, enabled, null);
            if (widget != null) {
                column.addChild(widget, element::adjustSettings);
            }
        }
        return column;
    }

    /**
     * Applies the disabled state and the tooltip to the widget.
     *
     * <p>Labels are always inactive ({@code active = false}, like {@code StringWidget}): their
     * disabled state shows through the colour, chosen by the element itself. The tooltip shows on
     * hover, whether the widget is active or not. Without a tooltip from the mod, the widget keeps
     * its own (an icon button shows its label).</p>
     */
    private static void applyState(AbstractWidget widget, boolean enabled, @Nullable Component tooltip) {
        if (!enabled) {
            widget.active = false;
        }
        if (tooltip != null) {
            widget.setTooltip(Tooltip.create(tooltip));
        }
    }

    @Override
    protected void init() {
        arrivingPage = null;
        LinearLayout layout = buildColumn(model, true);
        GaldurPanelLayout panel = new GaldurPanelLayout(layout);
        addRenderableOnly(panel);
        panel.arrangeElements();
        FrameLayout.centerInRectangle(panel, getRectangle());

        slidingWidgets.clear();
        if (arrivingPage != null) {
            arrivingPage.visitWidgets(slidingWidgets::add);
        }
        layout.visitWidgets(widget -> {
            if (!slidingWidgets.contains(widget)) {
                addRenderableWidget(widget);
                return;
            }
            // Widget of the arriving tab: it gets clicks and keys as usual, but is drawn offset
            // while the slide lasts.
            addWidget(widget);
            addRenderableOnly((guiGraphics, mouseX, mouseY, partialTick) -> {
                float offset = pageOffset();
                GaldurRender.drawOffset(guiGraphics, offset, 0,
                        () -> widget.render(guiGraphics, mouseX - Math.round(offset), mouseY, partialTick));
            });
        });
    }
}
