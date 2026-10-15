package com.galdurstudio.galdurui.internal.dev;

import com.galdurstudio.galdurui.api.menu.GaldurMenu;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

/**
 * Menu du terminal de test : seulement l'inventaire du joueur, sous la place réservée à la
 * grille d'objets que l'écran ({@link TestTerminalScreen}) affiche côté client. Même découpage que
 * le futur Terminal Nexus : la grille n'est pas faite de vrais slots. Développement uniquement.
 */
public final class TestTerminalMenu extends GaldurMenu {
    /** Ordonnée de la grille dans le panneau (sous le titre). */
    static final int GRID_Y = 18;
    /** Nombre de rangées visibles de la grille. */
    static final int GRID_ROWS = 6;
    /** Ordonnée de la première rangée de l'inventaire du joueur. */
    static final int INVENTORY_Y = GRID_Y + GRID_ROWS * SLOT_SIZE + 14;

    public TestTerminalMenu(int containerId, Inventory inventory) {
        super(DevMenus.TEST_TERMINAL.get(), containerId);
        addPlayerInventory(inventory, 8, INVENTORY_Y);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
