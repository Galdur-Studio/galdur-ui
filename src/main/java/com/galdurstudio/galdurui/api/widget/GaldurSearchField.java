package com.galdurstudio.galdurui.api.widget;

import com.galdurstudio.galdurui.internal.narration.GaldurNarration;
import com.galdurstudio.galdurui.api.theme.GaldurTheme;
import com.galdurstudio.galdurui.internal.render.GaldurRender;
import com.galdurstudio.galdurui.internal.render.GaldurSprites;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/**
 * Themed search field, usable in vanilla layouts.
 *
 * <p>Text editing itself (cursor, selection, copy and paste, Ctrl+A, text that scrolls when it
 * overflows) is handled by a vanilla {@link EditBox} placed inside: Galdur UI draws the frame,
 * the placeholder and the colours, and forwards mouse and keyboard to the edit box.</p>
 *
 * <p>Like {@link GaldurCheckbox}, the widget does not own the text: it <strong>reads it every
 * frame</strong> from a supplier and reports every keystroke. The text survives the widget being
 * rebuilt, and code can change it at any time.</p>
 *
 * <pre>{@code
 * String[] search = {""};
 * column.addChild(GaldurSearchField.of(120, Component.literal("Search..."),
 *         () -> search[0], text -> search[0] = text));
 * }</pre>
 *
 * <ul>
 *   <li><strong>Placeholder</strong>: shown in a muted colour while the field is empty.</li>
 *   <li><strong>Disabled</strong> ({@code active = false}): muted text, no input.</li>
 *   <li><strong>Escape</strong> closes the screen, as everywhere in the game; the up and down
 *       arrows move to the neighbouring widget.</li>
 * </ul>
 *
 * <p>Height from the theme: {@code spacing.field_height}. The text is limited to
 * {@value #MAX_LENGTH} characters.</p>
 */
public class GaldurSearchField extends AbstractWidget {
    /** Maximum length of the typed text. */
    public static final int MAX_LENGTH = 256;
    /** Gap between the field edge and the text, in pixels. */
    private static final int PADDING_X = 4;

    private final EditBox input;
    private final Component placeholder;
    private final Supplier<String> text;

    /**
     * Prefer {@link #of(int, Component, Supplier, Consumer)}.
     *
     * @param width       total width, frame included, in pixels
     * @param placeholder hint text, shown while the field is empty
     * @param text        supplier of the current text, called every frame
     * @param onChange    called with the new text on every change by the player
     */
    protected GaldurSearchField(int width, Component placeholder, Supplier<String> text, Consumer<String> onChange) {
        super(0, 0, width, GaldurTheme.fieldHeight(), placeholder);
        this.placeholder = placeholder;
        this.text = text;
        this.input = new EditBox(Minecraft.getInstance().font, Math.max(1, width - 2 * PADDING_X),
                GaldurRender.lineHeight(), placeholder);
        input.setBordered(false);
        input.setMaxLength(MAX_LENGTH);
        input.setValue(text.get());
        input.setResponder(onChange);
    }

    /**
     * {@return a search field at {@code (0, 0)}, for a layout to place}
     *
     * @param width       total width, frame included, in pixels
     * @param placeholder hint text, shown while the field is empty
     * @param text        supplier of the current text, called every frame
     * @param onChange    called with the new text on every change by the player
     */
    public static GaldurSearchField of(int width, Component placeholder, Supplier<String> text, Consumer<String> onChange) {
        return new GaldurSearchField(width, placeholder, text, onChange);
    }

    /**
     * {@return the width that shows the placeholder in full, or {@code minWidth} if larger}
     *
     * @param placeholder the hint text
     * @param minWidth    minimum width, in pixels
     */
    public static int preferredWidth(Component placeholder, int minWidth) {
        return Math.max(minWidth, GaldurRender.textWidth(placeholder) + 2 * PADDING_X + 2);
    }

    /**
     * Copies the supplier's text into the edit box when it was changed elsewhere (by the mod's
     * code). No effect while the player types: both are then equal.
     */
    private void syncFromSource() {
        String wanted = text.get();
        if (!input.getValue().equals(wanted)) {
            input.setValue(wanted);
        }
    }

    /** Places the edit box in the frame: horizontal padding, text centred vertically. */
    private void placeInput() {
        input.setX(getX() + PADDING_X);
        input.setY(getY() + (getHeight() - GaldurRender.lineHeight()) / 2 + 1);
        input.setWidth(Math.max(1, getWidth() - 2 * PADDING_X));
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        syncFromSource();
        placeInput();
        input.setEditable(active);
        input.setTextColor(GaldurTheme.text());
        input.setTextColorUneditable(GaldurTheme.textMuted());

        boolean highlighted = active && (isFocused() || isHovered());
        GaldurRender.drawNineSlice(guiGraphics, GaldurSprites.FIELD, getX(), getY(), getWidth(), getHeight(),
                GaldurTheme.surface());
        if (highlighted) {
            GaldurRender.drawOutline(guiGraphics, getX(), getY(), getWidth(), getHeight(), GaldurTheme.accentHover());
        }

        if (input.getValue().isEmpty()) {
            GaldurRender.drawClipped(guiGraphics, input.getX(), getY(), input.getWidth(), getHeight(),
                    () -> GaldurRender.text(guiGraphics, placeholder, input.getX(), input.getY(), GaldurTheme.textMuted(), false));
        }
        input.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        input.setFocused(focused);
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        placeInput();
        double insideX = Mth.clamp(mouseX, input.getX(), input.getX() + input.getWidth() - 1);
        input.onClick(insideX, input.getY());
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return active && visible && input.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return active && visible && input.charTyped(codePoint, modifiers);
    }

    /** A text field does not play the button sound when clicked. */
    @Override
    public void playDownSound(SoundManager handler) {
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        // "Search field: iron", or "Empty search field: Search...".
        String value = input.getValue();
        Component title = value.isEmpty()
                ? GaldurNarration.ours("search.empty", placeholder)
                : GaldurNarration.ours("search", value);
        narrationElementOutput.add(NarratedElementType.TITLE, GaldurNarration.withState(title, active));
    }
}
