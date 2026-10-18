package com.galdurstudio.galdurui.api.widget;

import com.galdurstudio.galdurui.api.anim.AnimatedValue;
import com.galdurstudio.galdurui.api.anim.GaldurAnimations;
import com.galdurstudio.galdurui.api.anim.GaldurEasing;
import com.galdurstudio.galdurui.api.theme.GaldurTheme;
import com.galdurstudio.galdurui.internal.input.GaldurInput;
import com.galdurstudio.galdurui.internal.narration.GaldurNarration;
import com.galdurstudio.galdurui.internal.render.GaldurRender;
import com.galdurstudio.galdurui.internal.render.GaldurSprites;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Themed button with a centred label.
 *
 * <p>Four states, each with a grayscale 9-slice sprite tinted by a theme colour: idle
 * ({@code accent}), hovered or keyboard-focused ({@code accent_hover}, faded in), pressed
 * ({@code accent_pressed}) and disabled ({@code surface}, label in {@code text_muted}). Disable it
 * with the inherited {@code active} field.</p>
 *
 * <pre>{@code
 * layout.addChild(GaldurButton.of(Component.literal("Confirm"), button -> confirm()));
 * }</pre>
 *
 * <p>Colours are read from the theme every frame. The size is fixed at construction:
 * {@link #of(Component, OnPress)} sizes the button from its label and the theme spacings.</p>
 */
public class GaldurButton extends AbstractWidget {
    /** Action run when the button is activated, by mouse or keyboard. */
    @FunctionalInterface
    public interface OnPress {
        /** @param button the activated button */
        void onPress(GaldurButton button);
    }

    private final OnPress onPress;

    private ResourceLocation spriteNormal = GaldurSprites.BUTTON;
    private ResourceLocation spriteHovered = GaldurSprites.BUTTON_HOVER;
    private ResourceLocation spritePressed = GaldurSprites.BUTTON_PRESSED;
    private ResourceLocation spriteDisabled = GaldurSprites.BUTTON_DISABLED;

    /**
     * True between the mouse press and release on this button. Cleared in {@link #onRelease},
     * on every frame where the left mouse button is up, and by {@link #clearHeld()}; never in
     * {@code setFocused(false)}, because Minecraft removes and gives back focus between press and
     * release on every click.
     */
    private boolean held;

    private final AnimatedValue hover = new AnimatedValue(0, GaldurAnimations.HOVER_MS, GaldurEasing.OUT_QUAD);

    /**
     * Creates a button with a given position and size; {@link #of(Component, OnPress)} sizes it from
     * its label instead.
     */
    public GaldurButton(int x, int y, int width, int height, Component label, OnPress onPress) {
        super(x, y, width, height, label);
        this.onPress = onPress;
    }

    /**
     * {@return a button sized from its label, at {@code (0, 0)} for a layout to place}: label
     * width plus {@code spacing.button_padding_x} on each side, at least
     * {@code spacing.button_min_width}, {@code spacing.button_height} high
     */
    public static GaldurButton of(Component label, OnPress onPress) {
        int width = Math.max(GaldurTheme.buttonMinWidth(), preferredWidth(label));
        return new GaldurButton(0, 0, width, GaldurTheme.buttonHeight(), label, onPress);
    }

    private static int preferredWidth(Component label) {
        return GaldurRender.textWidth(label) + 2 * GaldurTheme.buttonPaddingX();
    }

    /**
     * Replaces the sprites. They are tinted like the default ones, so provide them in grayscale:
     * white takes the exact theme colour.
     *
     * @return this button
     */
    public GaldurButton withSprites(ResourceLocation normal, ResourceLocation hovered,
                                    ResourceLocation pressed, ResourceLocation disabled) {
        this.spriteNormal = normal;
        this.spriteHovered = hovered;
        this.spritePressed = pressed;
        this.spriteDisabled = disabled;
        return this;
    }

    /** {@return the smallest width that shows the whole label} */
    public int getPreferredWidth() {
        return preferredWidth(getMessage());
    }

    private enum State { NORMAL, HOVERED, PRESSED, DISABLED }

    private State currentState() {
        if (!active) {
            return State.DISABLED;
        }
        // A press in progress always has focus. If focus left during the press (Tab with the mouse
        // held), the release will go to another widget: stop showing the pressed state.
        if (held && isHovered() && isFocused()) {
            return State.PRESSED;
        }
        return isHovered() || isKeyboardFocused() ? State.HOVERED : State.NORMAL;
    }

    /** A mouse click also gives focus; only keyboard focus should look like hover. */
    private boolean isKeyboardFocused() {
        return isFocused() && Minecraft.getInstance().getLastInputType().isKeyboard();
    }

    private ResourceLocation sprite(State state) {
        return switch (state) {
            case NORMAL -> spriteNormal;
            case HOVERED -> spriteHovered;
            case PRESSED -> spritePressed;
            case DISABLED -> spriteDisabled;
        };
    }

    private static int withAlpha(int argb, double factor) {
        int alpha = (int) Math.round(((argb >>> 24) & 0xFF) * Math.max(0, Math.min(1, factor)));
        return (alpha << 24) | (argb & 0xFFFFFF);
    }

    private static int tint(State state) {
        return switch (state) {
            case NORMAL -> GaldurTheme.accent();
            case HOVERED -> GaldurTheme.accentHover();
            case PRESSED -> GaldurTheme.accentPressed();
            case DISABLED -> GaldurTheme.surface();
        };
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Mouse released: the press is over, even if onRelease went to another widget.
        if (held && !GaldurInput.isLeftMouseDown()) {
            held = false;
        }
        State state = currentState();
        hover.setTarget(state == State.HOVERED || state == State.PRESSED ? 1 : 0);
        if (state == State.PRESSED || state == State.DISABLED) {
            GaldurRender.drawNineSlice(guiGraphics, sprite(state), getX(), getY(), getWidth(), getHeight(), tint(state));
        } else {
            // Hover fade: the hover sprite is drawn over the idle one, more and more opaque.
            double amount = hover.get();
            GaldurRender.drawNineSlice(guiGraphics, sprite(State.NORMAL), getX(), getY(), getWidth(), getHeight(),
                    tint(State.NORMAL));
            if (amount > 0) {
                GaldurRender.drawNineSlice(guiGraphics, sprite(State.HOVERED), getX(), getY(), getWidth(), getHeight(),
                        withAlpha(tint(State.HOVERED), amount));
            }
        }
        renderContent(guiGraphics);
    }

    /**
     * Draws the button contents over its background: by default the centred label, in
     * {@code text} or {@code text_muted} when disabled. Override it to show something else while
     * keeping the states and behaviour (see {@link GaldurIconButton}).
     */
    protected void renderContent(GuiGraphics guiGraphics) {
        int labelColor = active ? GaldurTheme.text() : GaldurTheme.textMuted();
        int labelY = getY() + (getHeight() - GaldurRender.lineHeight()) / 2;
        GaldurRender.drawCenteredText(guiGraphics, getMessage(), getX() + getWidth() / 2, labelY, labelColor);
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        held = true;
    }

    /** Fires only if the press started on this button and ends on it: dragging away cancels, as everywhere in the game. */
    @Override
    public void onRelease(double mouseX, double mouseY) {
        boolean wasHeld = held;
        held = false;
        if (wasHeld && clicked(mouseX, mouseY)) {
            onPress.onPress(this);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!active || !visible || !CommonInputs.selected(keyCode)) {
            return false;
        }
        playDownSound(Minecraft.getInstance().getSoundManager());
        onPress.onPress(this);
        return true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, GaldurNarration.withState(createNarrationMessage(), active));
        GaldurNarration.usage(narrationElementOutput, active, isFocused(),
                "narration.button.usage.focused", "narration.button.usage.hovered");
    }

    /** {@return {@code true} while the mouse button is held down on this button} */
    public boolean isHeld() {
        return held;
    }

    /**
     * Forgets the pressed state now. Rarely needed: a press whose release was not seen is forgotten
     * on the first frame where the mouse button is up. Useful for a {@link Screen} that keeps its
     * buttons across {@code init()} calls and wants a clean state before the next frame.
     */
    public void clearHeld() {
        held = false;
    }
}
