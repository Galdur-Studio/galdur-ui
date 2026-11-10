package com.galdurstudio.galdurui.api.layout;

import java.util.function.Consumer;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LayoutSettings;

/**
 * A row or a column of elements with a fixed gap between them.
 *
 * <p>Same result as {@code LinearLayout.horizontal().spacing(gap)} or
 * {@code LinearLayout.vertical().spacing(gap)}, but these do not exist before Minecraft 1.20.2
 * (the 1.20.1 {@code LinearLayout} spreads its children over a fixed size). This class works the
 * same on every version Galdur UI supports: use it in code that must compile on all of them.</p>
 *
 * <pre>{@code
 * GaldurStack column = GaldurStack.vertical(GaldurTheme.gap());
 * column.add(GaldurLabel.of(title));
 * GaldurStack buttons = column.add(GaldurStack.horizontal(GaldurTheme.gap()));
 * buttons.add(GaldurButton.of(Component.literal("OK"), button -> onClose()));
 * }</pre>
 */
public final class GaldurStack extends GridLayout {
    private final boolean horizontal;
    private int count;

    private GaldurStack(boolean horizontal, int gap) {
        this.horizontal = horizontal;
        if (horizontal) {
            columnSpacing(gap);
        } else {
            rowSpacing(gap);
        }
    }

    /**
     * @param gap space between two elements, in GUI pixels
     * @return an empty row, filled left to right
     */
    public static GaldurStack horizontal(int gap) {
        return new GaldurStack(true, gap);
    }

    /**
     * @param gap space between two elements, in GUI pixels
     * @return an empty column, filled top to bottom
     */
    public static GaldurStack vertical(int gap) {
        return new GaldurStack(false, gap);
    }

    /**
     * Adds an element after the previous ones, aligned with the default cell settings (top left).
     *
     * @param child the element
     * @param <T>   type of the element
     * @return {@code child}, to keep configuring it
     */
    public <T extends LayoutElement> T add(T child) {
        return add(child, settings -> {
        });
    }

    /**
     * Adds an element after the previous ones, with its own alignment, for instance
     * {@code LayoutSettings::alignVerticallyMiddle} in a row.
     *
     * @param child    the element
     * @param settings changes to the element's cell settings
     * @param <T>      type of the element
     * @return {@code child}, to keep configuring it
     */
    public <T extends LayoutElement> T add(T child, Consumer<LayoutSettings> settings) {
        LayoutSettings cell = newCellSettings();
        settings.accept(cell);
        int index = count++;
        return addChild(child, horizontal ? 0 : index, horizontal ? index : 0, cell);
    }
}
