package com.galdurstudio.galdurui.testmod;

import com.galdurstudio.galdurui.api.menu.GaldurMenu;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

/**
 * Test terminal menu: only the player inventory, below the space taken by the item grid that
 * {@link TestTerminalScreen} shows on the client. Same split as a storage terminal: the grid is
 * not made of real slots.
 */
public final class TestTerminalMenu extends GaldurMenu {
    static final int GRID_Y = 18;
    static final int GRID_ROWS = 6;
    static final int INVENTORY_Y = GRID_Y + GRID_ROWS * SLOT_SIZE + 14;

    public TestTerminalMenu(int containerId, Inventory inventory) {
        super(TestMenus.TEST_TERMINAL.get(), containerId);
        addPlayerInventory(inventory, 8, INVENTORY_Y);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
