package com.galdurstudio.galdurui.api.widget;

import com.galdurstudio.galdurui.internal.narration.GaldurNarration;
import com.galdurstudio.galdurui.api.text.GaldurFormat;
import com.galdurstudio.galdurui.api.theme.GaldurTheme;
import com.galdurstudio.galdurui.internal.render.GaldurRender;
import com.galdurstudio.galdurui.internal.render.GaldurSprites;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

/**
 * Themed item cell: an item icon and its short amount ("64", "1,2k", "64k", "1,2M"), usable in
 * vanilla layouts.
 *
 * <p>This is a <strong>display</strong> cell, not a real menu slot: it moves no item and does not
 * talk to the server. It shows items in large numbers (a storage network's contents, a recipe)
 * and tells the mod when it is clicked; the mod decides what to do.</p>
 *
 * <p>Like {@link GaldurCheckbox}, the widget reads the item and the amount <strong>every
 * frame</strong> from suppliers, so the cell follows the contents without being rebuilt. The
 * amount is a {@code long}, separate from the stack size, so it can go past 64 and even
 * {@code Integer.MAX_VALUE}.</p>
 *
 * <pre>{@code
 * GaldurItemSlot iron = GaldurItemSlot.of(() -> new ItemStack(Items.IRON_INGOT), () -> network.amount(iron))
 *         .onPress(button -> button == 0 ? takeStack() : takeHalf());
 * }</pre>
 *
 * <ul>
 *   <li><strong>Amount</strong>: hidden when it is 1, as in vanilla.</li>
 *   <li><strong>Tooltip</strong>: the item's own, as in an inventory, followed by the exact
 *       amount when it is shortened.</li>
 *   <li><strong>Click</strong>: left ({@code 0}) or right ({@code 1}), passed to the mod; Enter or
 *       Space count as a left click.</li>
 *   <li><strong>Disabled</strong> ({@code active = false}): muted amount, no click; the tooltip
 *       stays.</li>
 *   <li><strong>Empty</strong>: an empty stack shows an empty cell, without tooltip.</li>
 * </ul>
 */
public class GaldurItemSlot extends AbstractWidget {
    /** Side of a cell, frame included, in pixels: the size of a vanilla slot. */
    public static final int SIZE = GaldurRender.ITEM_SIZE + 2;
    /** Opacity of the hover highlight (0 to 255). */
    private static final int HOVER_ALPHA = 0x60;

    private final Supplier<ItemStack> stack;
    private final LongSupplier amount;
    private IntConsumer onPress = button -> {};

    /**
     * Prefer {@link #of(Supplier)} or {@link #of(Supplier, LongSupplier)}.
     *
     * @param stack  supplier of the shown item, called every frame
     * @param amount supplier of the shown amount, called every frame
     */
    protected GaldurItemSlot(Supplier<ItemStack> stack, LongSupplier amount) {
        super(0, 0, SIZE, SIZE, Component.empty());
        this.stack = stack;
        this.amount = amount;
    }

    /**
     * {@return a cell that shows the stack size as amount}
     *
     * @param stack supplier of the shown item, called every frame
     */
    public static GaldurItemSlot of(Supplier<ItemStack> stack) {
        return new GaldurItemSlot(stack, () -> stack.get().getCount());
    }

    /**
     * {@return a cell whose shown amount is supplied separately}
     *
     * @param stack  supplier of the shown item, called every frame; its size is ignored
     * @param amount supplier of the shown amount, called every frame
     */
    public static GaldurItemSlot of(Supplier<ItemStack> stack, LongSupplier amount) {
        return new GaldurItemSlot(stack, amount);
    }

    /**
     * Sets the action called when the cell is clicked. Only one action is kept.
     *
     * @param onPress receives the mouse button: {@code 0} left, {@code 1} right
     * @return this cell, for chaining
     */
    public GaldurItemSlot onPress(IntConsumer onPress) {
        this.onPress = onPress;
        return this;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        ItemStack item = stack.get();
        long count = amount.getAsLong();
        int itemX = getX() + 1;
        int itemY = getY() + 1;
        boolean keyboardFocused = isFocused() && Minecraft.getInstance().getLastInputType().isKeyboard();
        boolean highlighted = active && (isHovered() || keyboardFocused);

        GaldurRender.drawNineSlice(guiGraphics, GaldurSprites.SLOT, getX(), getY(), SIZE, SIZE, GaldurTheme.background());
        if (highlighted) {
            GaldurRender.drawOutline(guiGraphics, getX(), getY(), SIZE, SIZE, GaldurTheme.accentHover());
        }
        if (highlighted) {
            GaldurRender.drawFill(guiGraphics, itemX, itemY, GaldurRender.ITEM_SIZE, GaldurRender.ITEM_SIZE,
                    (HOVER_ALPHA << 24) | (GaldurTheme.accent() & 0xFFFFFF));
        }
        if (item.isEmpty()) {
            return;
        }
        GaldurRender.drawItem(guiGraphics, item, itemX, itemY);
        if (count != 1) {
            GaldurRender.drawItemCount(guiGraphics, GaldurFormat.amount(count), itemX, itemY,
                    active ? GaldurTheme.text() : GaldurTheme.textMuted());
        }
        if (isHovered()) {
            GaldurRender.showItemTooltip(item, count >= 1000
                    ? List.of(Component.literal(GaldurFormat.fullAmount(count)).withStyle(ChatFormatting.GRAY))
                    : List.of());
        }
    }

    /** Accepts left and right clicks. */
    @Override
    protected boolean isValidClickButton(int button) {
        return button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!active || !visible || !isValidClickButton(button) || !clicked(mouseX, mouseY)) {
            return false;
        }
        onPress.accept(button);
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!active || !visible || !CommonInputs.selected(keyCode)) {
            return false;
        }
        onPress.accept(GLFW.GLFW_MOUSE_BUTTON_LEFT);
        return true;
    }

    /** An item cell does not play the button sound. */
    @Override
    public void playDownSound(SoundManager handler) {
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        ItemStack item = stack.get();
        Component title = item.isEmpty()
                ? GaldurNarration.ours("slot.empty")
                : GaldurNarration.item(item, amount.getAsLong());
        // No usage hint: what a click does depends on the mod.
        narrationElementOutput.add(NarratedElementType.TITLE, GaldurNarration.withState(title, active));
    }
}
