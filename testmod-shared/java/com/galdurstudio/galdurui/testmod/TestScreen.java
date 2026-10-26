package com.galdurstudio.galdurui.testmod;

import com.galdurstudio.galdurui.api.layout.GaldurPanelLayout;
import com.galdurstudio.galdurui.api.theme.GaldurTheme;
import com.galdurstudio.galdurui.api.widget.GaldurButton;
import com.galdurstudio.galdurui.api.widget.GaldurCheckbox;
import com.galdurstudio.galdurui.api.widget.GaldurIconButton;
import com.galdurstudio.galdurui.api.widget.GaldurItemGrid;
import com.galdurstudio.galdurui.api.widget.GaldurItemSlot;
import com.galdurstudio.galdurui.api.widget.GaldurLabel;
import com.galdurstudio.galdurui.api.widget.GaldurProgressBar;
import com.galdurstudio.galdurui.api.widget.GaldurScrollList;
import com.galdurstudio.galdurui.api.widget.GaldurSearchField;
import com.galdurstudio.galdurui.api.widget.GaldurTabs;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Demo of every Galdur UI widget, with its states (hover, keyboard focus, disabled...), opened by
 * {@code /galdurui test}. Uses the widgets directly in vanilla layouts; {@code /galdurui window}
 * shows the same components through the declarative API.
 *
 * <p>Tabs: Buttons, Text (including the clipping demo), List, Items, Grid (every item in the game,
 * searchable and sortable; JEI and EMI only work on menu screens, see {@code /galduruidev
 * terminal}) and Progress.</p>
 *
 * <p>All state lives in fields, so it survives tab switches and resizes, which both go through
 * {@link #init()}. Minecraft ignores F3+T while a screen is open: "Reload theme" calls
 * {@code reloadResourcePacks()} itself, and the loading screen then rebuilds this screen.</p>
 */
public class TestScreen extends Screen {
    private static final int CONTENT_WIDTH = 230;
    private static final List<Component> TABS = List.of(
            Component.literal("Buttons"), Component.literal("Text"), Component.literal("List"),
            Component.literal("Items"), Component.literal("Grid"), Component.literal("Progress"));
    private static final List<String> ORES = List.of("Coal", "Iron", "Copper", "Gold", "Redstone",
            "Lapis Lazuli", "Diamond", "Emerald", "Nether Quartz", "Ancient Debris");

    private int tab;
    private int clicks;
    private boolean checked = true;
    private String query = "";
    private int selectedOre = -1;
    private double oreScroll;
    private double progress = 0.25;
    private String gridQuery = "";
    private boolean gridByAmount;
    private int gridScroll;
    private List<GaldurItemGrid.Entry> gridAll;
    /** Lower-case item names, computed once: sorting a thousand items reads each name many times. */
    private final Map<GaldurItemGrid.Entry, String> gridNames = new IdentityHashMap<>();
    private List<GaldurItemGrid.Entry> gridShown = List.of();

    private GaldurPanelLayout panel;
    private GaldurLabel counter;
    private GaldurTabs tabs;
    private GaldurLabel gridCount;

    public TestScreen() {
        super(Component.literal("Galdur UI — demo screen"));
    }

    @Override
    protected void init() {
        LinearLayout column = LinearLayout.vertical().spacing(GaldurTheme.gap());
        column.defaultCellSetting().alignHorizontallyCenter();

        column.addChild(GaldurLabel.of(getTitle()));
        tabs = column.addChild(GaldurTabs.of(CONTENT_WIDTH, TABS, () -> tab, this::selectTab));

        FrameLayout area = column.addChild(new FrameLayout(CONTENT_WIDTH, 0));
        area.defaultChildLayoutSetting().alignHorizontallyCenter().alignVerticallyTop();
        area.addChild(switch (tab) {
            case 0 -> buttonsTab();
            case 1 -> textTab();
            case 2 -> listTab();
            case 3 -> itemsTab();
            case 4 -> gridTab();
            default -> progressTab();
        });

        LinearLayout footer = column.addChild(LinearLayout.horizontal().spacing(GaldurTheme.gap()));
        footer.addChild(GaldurButton.of(Component.literal("Open a Galdur window"),
                button -> DemoWindow.open()));
        footer.addChild(GaldurButton.of(Component.literal("Reload theme"),
                button -> Minecraft.getInstance().reloadResourcePacks()));

        panel = new GaldurPanelLayout(column);
        addRenderableOnly(panel);
        panel.visitWidgets(this::addRenderableWidget);
        layoutPanel();
    }

    /**
     * Switches tab. The rebuild is deferred to the next loop because we are called from a click or
     * key press while the screen iterates its widgets. Focus goes back to the tabs so arrow keys
     * keep working.
     */
    private void selectTab(int index) {
        tab = index;
        Minecraft.getInstance().tell(() -> {
            if (Minecraft.getInstance().screen == this) {
                rebuildWidgets();
                setFocused(tabs);
            }
        });
    }

    private LinearLayout buttonsTab() {
        LinearLayout content = column();
        counter = content.addChild(GaldurLabel.muted(counterText()));
        LinearLayout actions = content.addChild(LinearLayout.horizontal().spacing(GaldurTheme.gap()));
        actions.addChild(GaldurButton.of(Component.literal("Click me"), button -> onClick()));
        actions.addChild(disabled(GaldurButton.of(Component.literal("Unavailable"), button -> {
        })));

        LinearLayout sizes = content.addChild(LinearLayout.horizontal().spacing(GaldurTheme.gap()));
        sizes.addChild(GaldurButton.of(Component.literal("OK"), button -> {
        }));
        sizes.addChild(GaldurButton.of(Component.literal("A noticeably longer label"), button -> {
        }));

        LinearLayout icons = content.addChild(LinearLayout.horizontal().spacing(GaldurTheme.gap()));
        icons.addChild(GaldurIconButton.ofItem(new ItemStack(Items.HOPPER), Component.literal("Sort"),
                button -> TestLog.LOGGER.info("Icon: sort")));
        icons.addChild(GaldurIconButton.ofItem(new ItemStack(Items.COMPASS), Component.literal("Locate"),
                button -> TestLog.LOGGER.info("Icon: locate")));
        icons.addChild(disabled(GaldurIconButton.ofItem(new ItemStack(Items.CRAFTING_TABLE),
                Component.literal("Request (disabled)"), button -> {
                })));

        content.addChild(GaldurCheckbox.of(Component.literal("Enabled checkbox"), () -> checked, value -> checked = value));
        content.addChild(disabled(GaldurCheckbox.of(Component.literal("Disabled checkbox"), () -> true, value -> {
        })));
        return content;
    }

    private LinearLayout textTab() {
        LinearLayout content = column();
        content.addChild(GaldurLabel.of(Component.literal("Normal label")));
        content.addChild(GaldurLabel.muted(Component.literal("Muted label")));
        content.addChild(GaldurLabel.of(Component.literal("A label far too long for its space, truncated"), 120));
        content.addChild(new ClipDemo());
        return content;
    }

    private LinearLayout listTab() {
        LinearLayout content = column();
        content.addChild(GaldurSearchField.of(140, Component.literal("Search an ore..."),
                () -> query, text -> {
                    query = text;
                    selectedOre = -1;
                }));
        GaldurScrollList list = GaldurScrollList.of(140, 5, this::filteredOres,
                () -> selectedOre, index -> {
                    selectedOre = index;
                    TestLog.LOGGER.info("Ore: {}", filteredOres().get(index).getString());
                });
        list.setScroll(oreScroll);
        content.addChild(list.onScroll(scroll -> oreScroll = scroll));

        LinearLayout disabledRow = content.addChild(LinearLayout.horizontal().spacing(GaldurTheme.gap()));
        disabledRow.addChild(disabled(GaldurSearchField.of(80, Component.literal("Disabled"), () -> "", text -> {
        })));
        disabledRow.addChild(disabled(GaldurScrollList.of(80, 2,
                () -> List.of(Component.literal("Disabled"), Component.literal("list")), () -> 0, index -> {
                })));
        return content;
    }

    private LinearLayout itemsTab() {
        LinearLayout content = column();
        content.addChild(GaldurLabel.muted(Component.literal("Hover: tooltip; left or right click: log")));
        LinearLayout slots = content.addChild(LinearLayout.horizontal().spacing(2));
        slots.addChild(GaldurItemSlot.of(() -> new ItemStack(Items.DIRT)));
        slots.addChild(GaldurItemSlot.of(() -> new ItemStack(Items.COBBLESTONE, 64)));
        slots.addChild(slotWithAmount(Items.IRON_INGOT, 1_234));
        slots.addChild(slotWithAmount(Items.DIAMOND, 64_000));
        slots.addChild(slotWithAmount(Items.REDSTONE, 1_250_000));
        slots.addChild(slotWithAmount(Items.EMERALD, 9_876_543_210L));
        slots.addChild(GaldurItemSlot.of(TestScreen::damagedSword));
        slots.addChild(GaldurItemSlot.of(() -> ItemStack.EMPTY));
        slots.addChild(disabled(slotWithAmount(Items.GOLD_INGOT, 500)));
        return content;
    }

    private LinearLayout gridTab() {
        if (gridAll == null) {
            gridAll = DemoItems.allWithAmounts();
            for (GaldurItemGrid.Entry entry : gridAll) {
                gridNames.put(entry, entry.stack().getHoverName().getString().toLowerCase(Locale.ROOT));
            }
            refreshGrid();
        }
        LinearLayout content = column();
        LinearLayout tools = content.addChild(LinearLayout.horizontal().spacing(GaldurTheme.gap()));
        tools.addChild(GaldurSearchField.of(140, Component.literal("Search an item..."), () -> gridQuery, text -> {
            gridQuery = text;
            refreshGrid();
        }));
        tools.addChild(GaldurIconButton.ofItem(new ItemStack(Items.HOPPER),
                Component.literal("Sort by name or amount"), button -> {
                    gridByAmount = !gridByAmount;
                    refreshGrid();
                }));

        GaldurItemGrid grid = GaldurItemGrid.of(9, 6, () -> gridShown)
                .onClick((index, entry, button, shift) -> TestLog.LOGGER.info("Grid: {} x {}, {} click{}",
                        entry.stack().getHoverName().getString(), entry.amount(),
                        button == 0 ? "left" : "right", shift ? " + Shift" : ""));
        grid.setScrollRow(gridScroll);
        content.addChild(grid.onScroll(row -> gridScroll = row));
        gridCount = content.addChild(GaldurLabel.muted(gridCountText()));
        return content;
    }

    private void refreshGrid() {
        String wanted = gridQuery.trim().toLowerCase(Locale.ROOT);
        List<GaldurItemGrid.Entry> shown = new ArrayList<>();
        for (GaldurItemGrid.Entry entry : gridAll) {
            if (gridNames.get(entry).contains(wanted)) {
                shown.add(entry);
            }
        }
        shown.sort(gridByAmount
                ? Comparator.comparingLong(GaldurItemGrid.Entry::amount).reversed()
                : Comparator.comparing(gridNames::get));
        gridShown = shown;
        if (gridCount != null) {
            int previousWidth = gridCount.getWidth();
            gridCount.setText(gridCountText());
            if (gridCount.getWidth() != previousWidth) {
                layoutPanel();
            }
        }
    }

    private Component gridCountText() {
        return Component.literal(gridShown.size() + " items, sorted by " + (gridByAmount ? "amount" : "name"));
    }

    private LinearLayout progressTab() {
        LinearLayout content = column();
        content.addChild(GaldurProgressBar.of(160, () -> progress,
                () -> Component.literal(Math.round(progress * 100) + " %")));
        content.addChild(GaldurProgressBar.of(160, () -> progress));
        content.addChild(disabled(GaldurProgressBar.of(160, () -> progress, () -> Component.literal("Disabled"))));

        LinearLayout actions = content.addChild(LinearLayout.horizontal().spacing(GaldurTheme.gap()));
        actions.addChild(GaldurButton.of(Component.literal("+10 %"),
                button -> progress = Math.min(1, progress + 0.1)));
        actions.addChild(GaldurButton.of(Component.literal("Full"), button -> progress = 1));
        actions.addChild(GaldurButton.of(Component.literal("Empty"), button -> progress = 0));
        return content;
    }

    private static LinearLayout column() {
        LinearLayout content = LinearLayout.vertical().spacing(GaldurTheme.gap());
        content.defaultCellSetting().alignHorizontallyCenter();
        return content;
    }

    private static <W extends net.minecraft.client.gui.components.AbstractWidget> W disabled(W widget) {
        widget.active = false;
        return widget;
    }

    private List<Component> filteredOres() {
        String wanted = query.trim().toLowerCase(Locale.ROOT);
        return ORES.stream()
                .filter(ore -> ore.toLowerCase(Locale.ROOT).contains(wanted))
                .map(ore -> (Component) Component.literal(ore))
                .toList();
    }

    private void layoutPanel() {
        panel.arrangeElements();
        FrameLayout.centerInRectangle(panel, getRectangle());
    }

    private void onClick() {
        clicks++;
        int previousWidth = counter.getWidth();
        counter.setText(counterText());
        // "Clicks: 9" -> "Clicks: 10" widens the label: re-center the panel.
        if (counter.getWidth() != previousWidth) {
            layoutPanel();
        }
    }

    private static GaldurItemSlot slotWithAmount(Item item, long amount) {
        return GaldurItemSlot.of(() -> new ItemStack(item), () -> amount)
                .onPress(button -> TestLog.LOGGER.info("Slot {}: {} click", item, button == 0 ? "left" : "right"));
    }

    private static ItemStack damagedSword() {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.setDamageValue(sword.getMaxDamage() * 2 / 3);
        return sword;
    }

    private Component counterText() {
        return Component.literal("Clicks: " + clicks);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
