package com.galdurstudio.galdurui.internal.dev;

import com.galdurstudio.galdurui.api.menu.GaldurMenu;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

/**
 * Menu de test : un coffre de 27 cases et l'inventaire du joueur, disposés comme un coffre
 * vanilla. Sert à éprouver {@link GaldurMenu} et
 * {@link com.galdurstudio.galdurui.api.screen.GaldurContainerScreen} (développement uniquement).
 */
public final class TestChestMenu extends GaldurMenu {
    /** Nombre de cases du coffre de test. */
    public static final int SIZE = 27;

    private final Container chest;

    /** Constructeur côté client : le contenu arrive ensuite du serveur. */
    public TestChestMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, new SimpleContainer(SIZE));
    }

    /** Constructeur côté serveur, avec le vrai contenu du coffre. */
    public TestChestMenu(int containerId, Inventory inventory, Container chest) {
        super(DevMenus.TEST_CHEST.get(), containerId);
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
