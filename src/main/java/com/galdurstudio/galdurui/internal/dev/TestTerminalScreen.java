package com.galdurstudio.galdurui.internal.dev;

import com.galdurstudio.galdurui.GaldurUI;
import com.galdurstudio.galdurui.api.screen.GaldurContainerScreen;
import com.galdurstudio.galdurui.api.widget.GaldurItemGrid;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * Écran du terminal de test : une {@link GaldurItemGrid} de tous les objets du jeu au-dessus de
 * l'inventaire du joueur, dans un {@link GaldurContainerScreen}. Sert à tester la grille là où
 * elle sera utilisée (le Terminal Nexus), en particulier avec JEI et EMI : R et U au survol d'un
 * objet. Développement uniquement, ouvert par {@code /galduruidev terminal}.
 */
public class TestTerminalScreen extends GaldurContainerScreen<TestTerminalMenu> {
    private static List<GaldurItemGrid.Entry> entries;
    private int scrollRow;

    public TestTerminalScreen(TestTerminalMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        if (entries == null) {
            entries = DemoItems.allWithAmounts();
        }
        GaldurItemGrid grid = GaldurItemGrid.of(9, TestTerminalMenu.GRID_ROWS, () -> entries)
                .onClick((index, entry, button, shift) -> GaldurUI.LOGGER.info("Terminal : {} × {}",
                        entry.stack().getHoverName().getString(), entry.amount()));
        // Les cases de la grille s'alignent sur celles de l'inventaire : cadre à x - 1.
        grid.setPosition(leftPos + 7, topPos + TestTerminalMenu.GRID_Y - 1);
        grid.setScrollRow(scrollRow);
        addRenderableWidget(grid.onScroll(row -> scrollRow = row));
    }
}
