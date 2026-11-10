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
//? if >=26.1 {
/*import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
*///?}

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
 */
public final class ScreenshotTour {
    /** {@code true} when the tour was asked for. */
    public static final boolean ENABLED = Boolean.getBoolean("galduruitest.screenshots");

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
            String name = "galduruitest-" + step + ".png";
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
        /*if (minecraft.gui.overlay() != null) {
            return false;
        }
        if (minecraft.level == null) {
            if (!worldRequested && Client.screen() instanceof TitleScreen title) {
                worldRequested = true;
                // A new world every time: reopening this flat test world asks to confirm its
                // "experimental settings", which would stop the tour.
                if (minecraft.getLevelSource().levelExists(WORLD)) {
                    try (var access = minecraft.getLevelSource().createAccess(WORLD)) {
                        access.deleteLevel();
                    } catch (java.io.IOException exception) {
                        TestLog.LOGGER.warn("Screenshot tour: could not delete the old test world", exception);
                    }
                }
                LevelSettings settings = new LevelSettings(WORLD, GameType.CREATIVE,
                        LevelSettings.DifficultySettings.DEFAULT, true, WorldDataConfiguration.DEFAULT);
                minecraft.createWorldOpenFlows().createFreshLevel(WORLD, settings, new WorldOptions(0L, false, false),
                        WorldPresets::createTestWorldDimensions, title);
            }
            return false;
        }
        // In the world: give it a few seconds to settle (chunks, loading screen).
        if (Client.screen() != null || ++wait < WAIT_TICKS * 3) {
            return false;
        }
        wait = 0;
        return true;
        *///?} else {
        return Client.screen() instanceof TitleScreen && minecraft.getOverlay() == null;
        //?}
    }
}
