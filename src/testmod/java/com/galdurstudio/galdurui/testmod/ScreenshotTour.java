package com.galdurstudio.galdurui.testmod;

import com.galdurstudio.galdurui.internal.client.Client;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import com.galdurstudio.galdurui.api.theme.GaldurPaint;
import com.galdurstudio.galdurui.api.theme.GaldurTheme;
import com.galdurstudio.galdurui.internal.config.GaldurConfig;
import com.galdurstudio.galdurui.internal.config.GaldurSettingsScreen;
import com.galdurstudio.galdurui.internal.config.StyleRulesScreen;
import com.galdurstudio.galdurui.internal.theme.ThemeEditorScreen;
import com.galdurstudio.galdurui.internal.theme.ThemeStore;
import java.util.Map;
//? if >=1.21 {
import net.minecraft.client.gui.screens.options.OptionsScreen;
//?} else {
/*import net.minecraft.client.gui.screens.OptionsScreen;
*///?}
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;

/**
 * Screenshot tour, to check the widgets on each Minecraft version without clicking.
 *
 * <p>Started with the Gradle property {@code galduruitest.screenshots} (for instance
 * {@code ./gradlew :1.20.1-fabric:runClient -Pgalduruitest.screenshots}): once the title
 * screen is shown, the client opens each tab of the demo screen, then the demo window, saves a
 * screenshot of each in {@code run/screenshots/galduruitest-<n>.png}, and quits. The test mod of
 * each loader calls {@link #tick} at the end of every client tick.</p>
 *
 * <p>Since 26.1 an item only exists once a world is loaded: the tour first opens the flat
 * creative world {@code galduruitest}, created again on each run. In a world, a loader's test mod
 * can add last steps with {@link #MENU_COMMANDS}, to show menu screens.</p>
 *
 * <p>With {@code galduruitest.containers} as well, the tour opens that world on every version and
 * only shows the vanilla screens with slots, without then with the Galdur look:
 * {@code run/screenshots/galduruitest-<screen>-vanilla.png} and {@code -galdur.png}.</p>
 */
public final class ScreenshotTour {
    /** {@code true} when the tour was asked for. */
    public static final boolean ENABLED = Boolean.getBoolean("galduruitest.screenshots");
    /** {@code true} to only show the vanilla screens with slots. */
    private static final boolean CONTAINERS = Boolean.getBoolean("galduruitest.containers");

    /** Ticks to wait on each screen, so the opening animation is over (one second). */
    private static final int WAIT_TICKS = 20;
    private static final int TABS = 6;

    /** Name of the test world (26.1 and later). */
    private static final String WORLD = "galduruitest";

    private static int step = -1;
    private static int wait;
    private static TestScreen screen;
    private static boolean worldRequested;
    /** The player's automatic mode setting before the tour turned it on. */
    private static boolean restyleAllBefore;
    /** The player's theme colours before the tour changed them. */
    private static Map<String, GaldurPaint> themeBefore = Map.of();

    /**
     * Commands (without "/") sent at the end of the tour when a world is loaded, each opening a
     * menu screen for one more screenshot. Each loader's test mod adds its own.
     */
    public static final List<String> MENU_COMMANDS = new ArrayList<>();

    /** What each step shows; the screenshot is taken one wait later. */
    private static final List<Runnable> STEPS = new ArrayList<>();
    /** The file name of each step's screenshot, {@code null} for a numbered one. */
    private static final List<String> NAMES = new ArrayList<>();

    private ScreenshotTour() {
    }

    public static void tick(Minecraft minecraft) {
        if (!ENABLED) {
            return;
        }
        if (step < 0) {
            if (ready(minecraft)) {
                planSteps(minecraft);
                step = 0;
                STEPS.get(0).run();
            }
            return;
        }
        if (++wait < WAIT_TICKS) {
            return;
        }
        wait = 0;
        if (step < STEPS.size()) {
            String name = step < NAMES.size() && NAMES.get(step) != null ? NAMES.get(step)
                    : "galduruitest-" + step + ".png";
            //? if >=26.1 {
            /*Screenshot.grab(minecraft.gameDirectory, name, minecraft.gameRenderer.mainRenderTarget(), 1,
                    message -> TestLog.LOGGER.info("Screenshot tour: {}", message.getString()));
            *///?} else {
            Screenshot.grab(minecraft.gameDirectory, name, minecraft.getMainRenderTarget(),
                    message -> TestLog.LOGGER.info("Screenshot tour: {}", message.getString()));
            //?}
        }
        step++;
        if (step < STEPS.size()) {
            STEPS.get(step).run();
        } else if (step > STEPS.size()) {
            // Quits one wait after the last screenshot, so it is written first.
            GaldurConfig.setRestyleAll(restyleAllBefore);
            ThemeStore.setPlayerTheme(themeBefore, Map.of());
            TestLog.LOGGER.info("Screenshot tour done.");
            minecraft.stop();
        }
    }

    /** The demo screen tabs, the demo window, the bridge demo, then the menus when in a world. */
    private static void planSteps(Minecraft minecraft) {
        STEPS.clear();
        NAMES.clear();
        if (CONTAINERS) {
            restyleAllBefore = GaldurConfig.restyleAll();
            // Survival, because in creative the inventory key opens the creative screen.
            minecraft.gameMode.setLocalMode(GameType.SURVIVAL);
            ContainerScreens.all(minecraft).forEach((name, screen) -> {
                STEPS.add(() -> {
                    GaldurConfig.setRestyleAll(false);
                    Client.setScreen(screen.get());
                });
                NAMES.add("galduruitest-" + name + "-vanilla.png");
                STEPS.add(() -> {
                    GaldurConfig.setRestyleAll(true);
                    Client.setScreen(screen.get());
                });
                NAMES.add("galduruitest-" + name + "-galdur.png");
            });
            return;
        }
        STEPS.add(() -> {
            screen = new TestScreen();
            Client.setScreen(screen);
        });
        for (int tab = 1; tab < TABS; tab++) {
            int shown = tab;
            STEPS.add(() -> screen.showTab(shown));
        }
        STEPS.add(DemoWindow::open);
        STEPS.add(() -> Client.setScreen(new BridgeDemoScreen()));
        // The player's automatic mode, on a vanilla screen; the setting is put back at the end.
        STEPS.add(() -> {
            restyleAllBefore = GaldurConfig.restyleAll();
            GaldurConfig.setRestyleAll(true);
            Client.setScreen(new OptionsScreen(null, minecraft.options));
        });
        // Settings screen, then the theme editor, then the editor with a blue accent: the whole
        // interface follows at once. The player's theme is put back at the end, nothing is saved.
        STEPS.add(() -> {
            GaldurConfig.setRestyleAll(restyleAllBefore);
            Client.setScreen(new GaldurSettingsScreen(null));
        });
        STEPS.add(() -> Client.setScreen(new StyleRulesScreen(null)));
        STEPS.add(() -> Client.setScreen(new ThemeEditorScreen(null)));
        STEPS.add(() -> {
            themeBefore = ThemeStore.playerPaints();
            ThemeStore.setPlayerTheme(Map.of(GaldurTheme.ACCENT, GaldurPaint.solid(0xFF2F6FB5),
                    GaldurTheme.ACCENT_HOVER, GaldurPaint.solid(0xFF4A8BD0),
                    GaldurTheme.ACCENT_PRESSED, GaldurPaint.solid(0xFF24578F),
                    // A gradient, to see it on every panel.
                    GaldurTheme.PANEL, GaldurPaint.gradient(GaldurPaint.Direction.VERTICAL, 0xFF1E2A38, 0xFF2A2420)),
                    Map.of());
            Client.setScreen(new ThemeEditorScreen(null));
        });
        STEPS.add(() -> Client.setScreen(new ThemeEditorScreen(null).shareScreen()));
        if (minecraft.player != null) {
            // The player and creative inventories, in the player's automatic mode.
            STEPS.add(() -> {
                GaldurConfig.setRestyleAll(true);
                // Survival, because in creative the inventory key opens the creative screen.
                minecraft.gameMode.setLocalMode(net.minecraft.world.level.GameType.SURVIVAL);
                Client.setScreen(new net.minecraft.client.gui.screens.inventory.InventoryScreen(minecraft.player));
            });
            STEPS.add(() -> {
                minecraft.gameMode.setLocalMode(net.minecraft.world.level.GameType.CREATIVE);
                Client.setScreen(new net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen(
                    minecraft.player, minecraft.player.connection.enabledFeatures(), true));
            });
            STEPS.add(() -> {
                GaldurConfig.setRestyleAll(restyleAllBefore);
                Client.setScreen(null);
            });
            for (String command : MENU_COMMANDS) {
                STEPS.add(() -> {
                    Client.setScreen(null);
                    minecraft.player.connection.sendCommand(command);
                });
            }
        }
    }

    /** {@return {@code true} once the demo screen can be opened} */
    private static boolean ready(Minecraft minecraft) {
        //? if >=26.1 {
        /*boolean loading = minecraft.gui.overlay() != null;
        boolean needsWorld = true;
        *///?} else {
        boolean loading = minecraft.getOverlay() != null;
        boolean needsWorld = CONTAINERS;
        //?}
        if (loading) {
            return false;
        }
        if (!needsWorld) {
            return Client.screen() instanceof TitleScreen;
        }
        if (minecraft.level == null) {
            if (!worldRequested && Client.screen() instanceof TitleScreen title) {
                worldRequested = true;
                createWorld(minecraft, title);
            }
            return false;
        }
        if (Client.screen() instanceof net.minecraft.client.gui.screens.PauseScreen) {
            Client.setScreen(null);
        }
        // In the world: give it a few seconds to settle (chunks, loading screen).
        if (Client.screen() != null || ++wait < WAIT_TICKS * 3) {
            return false;
        }
        wait = 0;
        return true;
    }

    /**
     * Creates the flat test world and opens it. A new world every time: reopening it asks to
     * confirm its "experimental settings" (26.1 and later), which would stop the tour.
     */
    private static void createWorld(Minecraft minecraft, TitleScreen title) {
        if (minecraft.getLevelSource().levelExists(WORLD)) {
            try (var access = minecraft.getLevelSource().createAccess(WORLD)) {
                access.deleteLevel();
            } catch (java.io.IOException exception) {
                TestLog.LOGGER.warn("Screenshot tour: could not delete the old test world", exception);
            }
        }
        minecraft.options.pauseOnLostFocus = false;
        //? if >=26.1 {
        /*LevelSettings settings = new LevelSettings(WORLD, GameType.CREATIVE,
                LevelSettings.DifficultySettings.DEFAULT, true, WorldDataConfiguration.DEFAULT);
        minecraft.createWorldOpenFlows().createFreshLevel(WORLD, settings, new WorldOptions(0L, false, false),
                WorldPresets::createTestWorldDimensions, title);
        *///?} else {
        LevelSettings settings = new LevelSettings(WORLD, GameType.CREATIVE, false,
                net.minecraft.world.Difficulty.PEACEFUL, true, new net.minecraft.world.level.GameRules(),
                WorldDataConfiguration.DEFAULT);
        java.util.function.Function<net.minecraft.core.RegistryAccess, net.minecraft.world.level.levelgen.WorldDimensions> flat =
                access -> access.registryOrThrow(net.minecraft.core.registries.Registries.WORLD_PRESET)
                        .getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions();
        //? if >=1.21 {
        minecraft.createWorldOpenFlows().createFreshLevel(WORLD, settings, new WorldOptions(0L, false, false), flat, title);
        //?} else {
        /*minecraft.createWorldOpenFlows().createFreshLevel(WORLD, settings, new WorldOptions(0L, false, false), flat);
        *///?}
        //?}
    }
}
