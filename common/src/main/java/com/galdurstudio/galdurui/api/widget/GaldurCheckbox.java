package com.galdurstudio.galdurui.api.widget;

import com.galdurstudio.galdurui.api.anim.AnimatedValue;
import com.galdurstudio.galdurui.api.anim.GaldurAnimations;
import com.galdurstudio.galdurui.api.anim.GaldurEasing;
import com.galdurstudio.galdurui.api.theme.GaldurTheme;
import com.galdurstudio.galdurui.internal.narration.GaldurNarration;
import com.galdurstudio.galdurui.internal.render.GaldurRender;
import com.galdurstudio.galdurui.internal.render.GaldurSprites;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

/**
 * Themed checkbox.
 *
 * <p>The widget does not store the state: it <strong>reads it every frame</strong> from a
 * supplier and asks for changes through a callback. The state lives elsewhere (a
 * {@code GaldurWindow} handle, for instance), so it survives the widget being rebuilt on
 * resize.</p>
 *
 * <pre>{@code
 * boolean[] sound = {false};
 * column.addChild(GaldurCheckbox.of(Component.literal("Enable sound"),
 *         () -> sound[0], value -> sound[0] = value));
 * }</pre>
 */
public class GaldurCheckbox extends AbstractWidget {
    private static final int BOX_SIZE = 9;
    private static final int GAP = 4;

    private final BooleanSupplier checked;
    private final Consumer<Boolean> onToggle;
    private boolean held;
    /** Shown size of the mark, from 0 (none) to 1 (full): it grows and shrinks. */
    private final AnimatedValue mark;

    /**
     * @param label    text right of the box
     * @param checked  supplier of the current state, called every frame
     * @param onToggle receives the wanted value when the player toggles the box
     */
    protected GaldurCheckbox(Component label, BooleanSupplier checked, Consumer<Boolean> onToggle) {
        super(0, 0, BOX_SIZE + GAP + GaldurRender.textWidth(label), Math.max(BOX_SIZE, GaldurRender.lineHeight()), label);
        this.checked = checked;
        this.onToggle = onToggle;
        this.mark = new AnimatedValue(checked.getAsBoolean() ? 1 : 0, GaldurAnimations.TOGGLE_MS, GaldurEasing.OUT_CUBIC);
    }

    /** {@return a checkbox at {@code (0, 0)}, for a layout to place} */
    public static GaldurCheckbox of(Component label, BooleanSupplier checked, Consumer<Boolean> onToggle) {
        return new GaldurCheckbox(label, checked, onToggle);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int boxY = getY() + (getHeight() - BOX_SIZE) / 2 - 1;
        int labelY = getY() + (getHeight() - GaldurRender.lineHeight()) / 2;
        boolean highlighted = active
                && (isHovered() || (isFocused() && Minecraft.getInstance().getLastInputType().isKeyboard()));
        int markColor = active ? GaldurTheme.accent() : GaldurTheme.textMuted();
        int labelColor = active ? GaldurTheme.text() : GaldurTheme.textMuted();

        GaldurRender.drawNineSlice(guiGraphics, GaldurSprites.FIELD, getX(), boxY, BOX_SIZE, BOX_SIZE, GaldurTheme.surface());
        if (highlighted) {
            GaldurRender.drawOutline(guiGraphics, getX(), boxY, BOX_SIZE, BOX_SIZE, GaldurTheme.accentHover());
        }
        mark.setTarget(checked.getAsBoolean() ? 1 : 0);
        double amount = mark.get();
        if (amount > 0) {
            int full = BOX_SIZE - 4;
            int size = Math.max(1, (int) Math.round(full * amount));
            int offset = 2 + (full - size) / 2;
            GaldurRender.drawFill(guiGraphics, getX() + offset, boxY + offset, size, size, markColor);
        }
        GaldurRender.text(guiGraphics, getMessage(), getX() + GAP + BOX_SIZE, labelY, labelColor, true);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        Component state = GaldurNarration.ours(checked.getAsBoolean() ? "checked" : "unchecked");
        Component title = CommonComponents.joinForNarration(
                Component.translatable("narration.checkbox", getMessage()), state);
        narrationElementOutput.add(NarratedElementType.TITLE, GaldurNarration.withState(title, active));
        GaldurNarration.usage(narrationElementOutput, active, isFocused(),
                "narration.checkbox.usage.focused", "narration.checkbox.usage.hovered");
    }

    /** Only remembers the press: the box toggles on release, so the player can cancel by releasing elsewhere. */
    @Override
    public void onClick(double mouseX, double mouseY) {
        held = true;
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        boolean wasHeld = held;
        held = false;
        if (wasHeld && clicked(mouseX, mouseY)) {
            toggle();
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!active || !visible || !CommonInputs.selected(keyCode)) {
            return false;
        }
        playDownSound(Minecraft.getInstance().getSoundManager());
        toggle();
        return true;
    }

    private void toggle() {
        onToggle.accept(!checked.getAsBoolean());
    }
}
