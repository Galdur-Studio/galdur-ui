package com.galdurstudio.galdurui.api.menu;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Base of a slot menu (logic side, client <strong>and</strong> server), ready to be shown by a
 * {@link com.galdurstudio.galdurui.api.screen.GaldurContainerScreen}. It places slot grids and
 * the player inventory, and handles Shift-click between them.
 *
 * <pre>{@code
 * public class ChestMenu extends GaldurMenu {
 *     private final Container chest;
 *
 *     public ChestMenu(int id, Inventory player, Container chest) {
 *         super(MY_MENUS.CHEST.get(), id);
 *         this.chest = chest;
 *         addSlotGrid(chest, 0, 9, 3, 8, 18);   // 27 slots
 *         addPlayerInventory(player, 8, 84);    // always last
 *     }
 *
 *     public boolean stillValid(Player player) {
 *         return chest.stillValid(player);
 *     }
 * }
 * }</pre>
 *
 * <p><strong>Rule</strong>: add the container slots first ({@link #addSlotGrid} or
 * {@code addSlot}), then the player inventory ({@link #addPlayerInventory}). Every slot added
 * before the player inventory counts as a container slot for Shift-click.</p>
 *
 * <p>Coordinates are Minecraft's: GUI pixels from the top-left corner of the menu panel, pointing
 * at the top-left corner of the 16 × 16 item; the slot frame extends one pixel around it.</p>
 */
public abstract class GaldurMenu extends AbstractContainerMenu {
    /** Distance between two neighbouring slots: 16 for the item and 2 for the frames. */
    public static final int SLOT_SIZE = 18;
    /** Vertical gap between the three inventory rows and the hotbar. */
    public static final int HOTBAR_GAP = 4;

    private int containerSlots = -1;
    private int playerStart = -1;
    private int playerEnd = -1;

    /**
     * @param type        the menu type, registered by the mod
     * @param containerId the window id given by Minecraft
     */
    protected GaldurMenu(@Nullable MenuType<?> type, int containerId) {
        super(type, containerId);
    }

    /**
     * Adds a grid of slots, row by row, left to right.
     *
     * @param container  the container whose slots are shown
     * @param firstIndex container index of the first grid slot
     * @param columns    number of columns
     * @param rows       number of rows
     * @param x          x of the first slot (top-left of the item)
     * @param y          y of the first slot
     * @throws IllegalStateException if the player inventory was already added
     */
    protected void addSlotGrid(Container container, int firstIndex, int columns, int rows, int x, int y) {
        if (playerStart >= 0) {
            throw new IllegalStateException("Add the container slots before addPlayerInventory.");
        }
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                addSlot(new Slot(container, firstIndex + row * columns + column,
                        x + column * SLOT_SIZE, y + row * SLOT_SIZE));
            }
        }
    }

    /**
     * Adds the player inventory: three rows of nine, then the hotbar {@value #HOTBAR_GAP} pixels
     * lower. Call once, after the container slots.
     *
     * @param inventory the player inventory
     * @param x         x of the first slot (top-left of the item)
     * @param y         y of the first row; the hotbar is at {@code y + 3 * 18 + 4}
     * @throws IllegalStateException if the player inventory was already added
     */
    protected void addPlayerInventory(Inventory inventory, int x, int y) {
        if (playerStart >= 0) {
            throw new IllegalStateException("The player inventory is already in this menu.");
        }
        containerSlots = slots.size();
        playerStart = slots.size();
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, 9 + row * 9 + column, x + column * SLOT_SIZE, y + row * SLOT_SIZE));
            }
        }
        int hotbarY = y + 3 * SLOT_SIZE + HOTBAR_GAP;
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, x + column * SLOT_SIZE, hotbarY));
        }
        playerEnd = slots.size();
    }

    /** {@return the number of container slots, or {@code -1} until the player inventory is added} */
    public int getContainerSlotCount() {
        return containerSlots;
    }

    /**
     * Shift-click: moves the stack from the container to the player inventory (hotbar first), or
     * from the player inventory to the container, like a vanilla chest. Moves nothing if the menu
     * has no player inventory.
     */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (playerStart < 0 || index < 0 || index >= slots.size()) {
            return ItemStack.EMPTY;
        }
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        boolean moved = index < containerSlots
                ? moveItemStackTo(stack, playerStart, playerEnd, true)
                : moveItemStackTo(stack, 0, containerSlots, false);
        if (!moved) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
    }
}
