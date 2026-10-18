package com.galdurstudio.galdurui.testmod;

import com.galdurstudio.galdurui.api.menu.GaldurMenu;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

/** A 27-slot chest and the player inventory, laid out like a vanilla chest. */
public final class TestChestMenu extends GaldurMenu {
    public static final int SIZE = 27;

    private final Container chest;

    /** Client side: the contents come from the server afterwards. */
    public TestChestMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, new SimpleContainer(SIZE));
    }

    /** Server side, with the real chest contents. */
    public TestChestMenu(int containerId, Inventory inventory, Container chest) {
        super(TestMenus.TEST_CHEST.get(), containerId);
        this.chest = chest;
        chest.startOpen(inventory.player);
        addSlotGrid(chest, 0, 9, 3, 8, 18);
        addPlayerInventory(inventory, 8, 84);
    }

    @Override
    public boolean stillValid(Player player) {
        return chest.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        chest.stopOpen(player);
    }
}
