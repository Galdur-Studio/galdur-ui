package com.galdurstudio.galdurui.testmod;

import com.galdurstudio.galdurui.api.GaldurLib;
import com.galdurstudio.galdurui.api.window.CheckboxHandle;
import com.galdurstudio.galdurui.api.window.ElementHandle;
import com.galdurstudio.galdurui.api.window.GaldurWindow;
import com.galdurstudio.galdurui.api.window.ListHandle;
import com.galdurstudio.galdurui.api.window.ProgressHandle;
import com.galdurstudio.galdurui.api.window.RowHandle;
import com.galdurstudio.galdurui.api.window.SearchHandle;
import com.galdurstudio.galdurui.api.window.TabHandle;
import com.galdurstudio.galdurui.api.window.TabsHandle;
import com.mojang.brigadier.Command;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Client commands of the test mod:
 * <ul>
 *   <li>{@code /galdurui test} opens {@link TestScreen}, every widget used directly;</li>
 *   <li>{@code /galdurui window} opens a window built only with the public declarative API;</li>
 *   <li>{@code /galdurui debug} toggles widget outlines ({@link LayoutDebug});</li>
 *   <li>{@code /galdurui fps} toggles the FPS counter ({@link FpsOverlay}).</li>
 * </ul>
 */
public final class TestCommands {
    private static final long CRAFT_TOTAL = 64;
    private static final List<String> ORES = List.of("Coal", "Iron", "Copper", "Gold", "Redstone",
            "Lapis Lazuli", "Diamond", "Emerald", "Nether Quartz", "Ancient Debris");

    private TestCommands() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(RegisterClientCommandsEvent.class, TestCommands::onRegisterClientCommands);
    }

    private static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("galdurui")
                .then(Commands.literal("test").executes(context -> {
                    // The chat screen is still open while the command runs: open on the next loop,
                    // otherwise closing the chat would replace our screen.
                    Minecraft minecraft = Minecraft.getInstance();
                    minecraft.tell(() -> minecraft.setScreen(new TestScreen()));
                    return Command.SINGLE_SUCCESS;
                }))
                .then(Commands.literal("window").executes(context -> {
                    openDemoWindow();
                    return Command.SINGLE_SUCCESS;
                }))
                .then(Commands.literal("debug").executes(context -> {
                    boolean on = LayoutDebug.toggle();
                    context.getSource().sendSuccess(() -> Component.literal("Galdur UI: layout debug " + (on ? "on." : "off.")), false);
                    return Command.SINGLE_SUCCESS;
                }))
                .then(Commands.literal("fps").executes(context -> {
                    boolean on = FpsOverlay.toggle();
                    context.getSource().sendSuccess(() -> Component.literal("Galdur UI: FPS counter "
                            + (on ? "shown on open screens." : "hidden.")), false);
                    return Command.SINGLE_SUCCESS;
                })));
    }

    /**
     * Opens a demo window shaped like a storage terminal, built only with the public API:
     * <ul>
     *   <li><strong>Storage</strong>: a search field filtering a list of ores, icon buttons (reverse
     *       order; clear the search, disabled while empty; a disabled one with a tooltip), "Take",
     *       enabled once an ore is selected, and "Advanced options", enabled from another tab;</li>
     *   <li><strong>Crafting</strong>: a hidden text that frees its space, shown by a checkbox;</li>
     *   <li><strong>Autocraft</strong>: the "Advanced mode" checkbox, which enables "Advanced
     *       options" in the Storage tab (a change in a hidden tab must be visible when coming back),
     *       and a progress bar driven by buttons.</li>
     * </ul>
     * <p>Below the tabs, "Next tab" switches tabs from code and "Quit" closes the window. Opened
     * from the test screen, Escape and "Quit" go back to it.</p>
     */
    static void openDemoWindow() {
        GaldurWindow window = GaldurLib.createWindow();
        window.addTitle(Component.literal("Terminal"), true);

        TabsHandle tabs = window.addTabs(true);
        tabs.setTooltip(Component.literal("Left and right arrows switch tabs"));
        tabs.onSelect(index -> GaldurUITest.LOGGER.info("Tab: {}", index));

        TabHandle storage = tabs.addTab(Component.literal("Storage"));
        SearchHandle search = storage.addSearch(Component.literal("Search an ore..."));
        ListHandle ores = storage.addList(5);
        boolean[] reversed = {false};
        ores.setLines(filterOres("", false));
        RowHandle tools = storage.addRow();
        tools.addIconButton(new ItemStack(Items.HOPPER), Component.literal("Reverse order"), () -> {
            reversed[0] = !reversed[0];
            ores.setLines(filterOres(search.getText(), reversed[0]));
        });
        ElementHandle clear = tools.addIconButton(new ItemStack(Items.BARRIER),
                Component.literal("Clear search"), () -> search.setText(""));
        clear.setEnabled(false);
        search.onChange(query -> {
            ores.setLines(filterOres(query, reversed[0]));
            clear.setEnabled(!query.isEmpty());
        });
        ElementHandle soon = tools.addIconButton(new ItemStack(Items.CRAFTING_TABLE),
                Component.literal("Request"), () -> {});
        soon.setEnabled(false);
        soon.setTooltip(Component.literal("Request: coming soon"));
        RowHandle storageActions = storage.addRow();
        ElementHandle take = storageActions.addButton(Component.literal("Take"),
                () -> GaldurUITest.LOGGER.info("Take: {}", ores.getLines().get(ores.getSelected()).getString()));
        take.setEnabled(false);
        take.setTooltip(Component.literal("Select an ore in the list first"));
        ores.onSelect(index -> take.setEnabled(index >= 0));
        ElementHandle options = storageActions.addButton(Component.literal("Advanced options"),
                () -> GaldurUITest.LOGGER.info("Advanced options"));
        options.setEnabled(false);
        options.setTooltip(Component.literal("Tick \"Advanced mode\" in the Autocraft tab"));

        TabHandle crafting = tabs.addTab(Component.literal("Crafting"));
        crafting.addText(Component.literal("3 x 3 grid: coming soon"));
        ElementHandle detail = crafting.addText(Component.literal("12 items, 3 different kinds"));
        detail.setVisible(false);
        CheckboxHandle showDetail = crafting.addCheckbox(Component.literal("Show details"), false);
        showDetail.onChange(detail::setVisible);

        TabHandle autocraft = tabs.addTab(Component.literal("Autocraft"));
        autocraft.addText(Component.literal("No running request"));
        CheckboxHandle advanced = autocraft.addCheckbox(Component.literal("Advanced mode"), false);
        advanced.setTooltip(Component.literal("Unlocks the advanced storage options"));
        advanced.onChange(options::setEnabled);
        long[] crafted = {0};
        ProgressHandle craft = autocraft.addProgress(craftLabel(0));
        RowHandle craftActions = autocraft.addRow();
        ElementHandle advance = craftActions.addButton(Component.literal("Advance by 8"), () -> {
            crafted[0] = Math.min(CRAFT_TOTAL, crafted[0] + 8);
            craft.setProgress(crafted[0], CRAFT_TOTAL);
            craft.setLabel(craftLabel(crafted[0]));
        });
        craftActions.addButton(Component.literal("Reset"), () -> {
            crafted[0] = 0;
            craft.setProgress(0);
            craft.setLabel(craftLabel(0));
            craft.setEnabled(true);
            advance.setEnabled(true);
        });
        ElementHandle pause = craftActions.addButton(Component.literal("Pause"), () -> {
            craft.setEnabled(!craft.isEnabled());
            advance.setEnabled(craft.isEnabled());
        });
        pause.setTooltip(Component.literal("Pauses the bar (disabled state)"));

        RowHandle actions = window.addRow(true);
        actions.addButton(Component.literal("Next tab"),
                () -> tabs.setSelected((tabs.getSelected() + 1) % tabs.getTabs().size()));
        actions.addButton(Component.literal("Quit"), window::close);
        window.open();
    }

    private static Component craftLabel(long crafted) {
        return Component.literal("Iron ingots: " + crafted + " / " + CRAFT_TOTAL);
    }

    /** {@return the ores whose name contains {@code query}, ignoring case and accents} */
    private static List<Component> filterOres(String query, boolean reversed) {
        String wanted = simplify(query.trim());
        List<Component> found = ORES.stream()
                .filter(ore -> simplify(ore).contains(wanted))
                .map(ore -> (Component) Component.literal(ore))
                .toList();
        return reversed ? found.reversed() : found;
    }

    private static String simplify(String text) {
        return Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }
}
