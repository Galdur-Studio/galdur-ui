package com.galdurstudio.galdurui.testmod;

import com.galdurstudio.galdurui.api.screen.GaldurContainerScreen;
import com.galdurstudio.galdurui.api.widget.GaldurItemGrid;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * Test terminal: a {@link GaldurItemGrid} of every item above the player inventory, in a
 * {@link GaldurContainerScreen}. Tests the grid where it will be used, including with JEI and EMI
 * (R and U while hovering an item). Opened by {@code /galduruidev terminal}.
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
                .onClick((index, entry, button, shift) -> GaldurUITest.LOGGER.info("Terminal: {} x {}",
                        entry.stack().getHoverName().getString(), entry.amount()));
        // Grid cells line up with the inventory slots: frame at x - 1.
        grid.setPosition(leftPos + 7, topPos + TestTerminalMenu.GRID_Y - 1);
        grid.setScrollRow(scrollRow);
        addRenderableWidget(grid.onScroll(row -> scrollRow = row));
    }
}
