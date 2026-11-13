package com.galdurstudio.galdurui.testmod;

import net.minecraft.client.Minecraft;

//? if >=1.21 <1.21.2 {
import com.galdurstudio.galdurui.api.theme.GaldurPaint;
import com.galdurstudio.galdurui.api.theme.GaldurTheme;
import com.galdurstudio.galdurui.internal.client.Client;
import com.galdurstudio.galdurui.internal.config.GaldurConfig;
import com.galdurstudio.galdurui.internal.config.GaldurSettingsScreen;
import com.galdurstudio.galdurui.internal.config.StyleRulesScreen;
import com.galdurstudio.galdurui.internal.theme.ThemeEditorScreen;
import com.galdurstudio.galdurui.internal.theme.ThemeStore;
import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import org.lwjgl.glfw.GLFW;
//?}

/**
 * Captures the photos and the short clips of the wiki, in a real Minecraft session: it opens the
 * screens, moves a visible cursor, clicks and drags like a player would, and saves a screenshot per
 * game tick. Run with {@code ./gradlew :1.21.1-neoforge:runClient -Pgalduruitest.media=en_us}
 * (or {@code fr_fr}); the files go to {@code run/media/<language>/}, and {@code tools/media.sh}
 * turns them into WebP photos and WebM/MP4 clips.
 *
 * <p>Only written for 1.21.1: on the other versions this class does nothing.</p>
 */
public final class MediaSession {
    /** The language of the session ({@code en_us}, {@code fr_fr}), or {@code null} when not asked for. */
    public static final String LANGUAGE = System.getProperty("galduruitest.media");
    public static final boolean ENABLED = LANGUAGE != null;
    /** {@code -Pgalduruitest.containers}: only the photos of the vanilla screens with slots. */
    private static final boolean CONTAINERS_ONLY = Boolean.getBoolean("galduruitest.containers");

    private MediaSession() {
    }

    public static void tick(Minecraft minecraft) {
        //? if >=1.21 <1.21.2 {
        if (ENABLED) {
            Session.tick(minecraft);
        }
        //?}
    }

    /** Called after each frame is drawn: saves the frames of a clip (NeoForge test mod). */
    public static void frame(Minecraft minecraft) {
        //? if >=1.21 <1.21.2 {
        if (ENABLED) {
            Session.frame(minecraft);
        }
        //?}
    }

    //? if >=1.21 <1.21.2 {
    private static final class Session {
        private static final String WORLD = "galduruitest";
        private static final java.util.Set<String> LOGGED = new java.util.HashSet<>();
        private static final int WIDTH = 1280;
        private static final int HEIGHT = 720;
        private static final String[] CURSOR = {
            "X          ", "XX         ", "X.X        ", "X..X       ", "X...X      ", "X....X     ", "X.....X    ",
            "X......X   ", "X.......X  ", "X........X ", "X.....XXXXX", "X..X..X    ", "X.X X..X   ", "XX  X..X   ",
            "X    X..X  ", "     X..X  ", "      XX   ",
        };

        /** One thing to do, over one or more ticks; returns {@code true} when it is over. */
        private interface Step {
            boolean tick(int ticks);
        }

        private static final List<Step> STEPS = new ArrayList<>();
        private static int index = -1;
        private static int ticksInStep;
        private static int startWait;
        private static boolean worldRequested;
        private static boolean planned;
        private static boolean configured;

        private static Path outDir;
        private static String recording;
        private static int frame;
        private static final List<Long> FRAME_TIMES = new ArrayList<>();
        private static boolean cursorShown;
        /** Where the cursor was at the previous tick. */
        private static double previousX = 200;
        private static double previousY = 120;
        private static final java.util.concurrent.ExecutorService WRITERS = java.util.concurrent.Executors.newFixedThreadPool(3);
        private static final List<java.util.concurrent.Future<?>> PENDING = new ArrayList<>();
        private static double cursorX = 200;
        private static double cursorY = 120;

        private static boolean restyleBefore;
        private static Map<String, GaldurPaint> themeBefore = Map.of();

        static void tick(Minecraft minecraft) {
            if (!planned) {
                if (!ready(minecraft)) {
                    return;
                }
                if (!configured) {
                    configure(minecraft);
                    configured = true;
                    return;
                }
                if (++startWait < 20) {
                    return;
                }
                planned = true;
                outDir = minecraft.gameDirectory.toPath().resolve("media").resolve(LANGUAGE);
                restyleBefore = GaldurConfig.restyleAll();
                themeBefore = ThemeStore.playerPaints();
                plan(minecraft);
                index = 0;
            }
            if (index < 0) {
                return;
            }
            if (index >= STEPS.size()) {
                finish(minecraft);
                return;
            }
            previousX = cursorX;
            previousY = cursorY;
            if (STEPS.get(index).tick(ticksInStep++)) {
                index++;
                ticksInStep = 0;
            }
        }

        /** One image per drawn frame (at most 30 a second), so the clips are smooth. */
        static void frame(Minecraft minecraft) {
            if (recording == null) {
                return;
            }
            long now = System.nanoTime();
            if (!FRAME_TIMES.isEmpty() && now - FRAME_TIMES.get(FRAME_TIMES.size() - 1) < 33_000_000L) {
                return;
            }
            // The cursor moves once per tick: between two ticks it is shown part of the way.
            float partial = minecraft.getTimer().getGameTimeDeltaPartialTick(false);
            double shownX = previousX + (cursorX - previousX) * partial;
            double shownY = previousY + (cursorY - previousY) * partial;
            String file = recording + "/f" + String.format("%04d", ++frame) + ".png";
            NativeImage image = Screenshot.takeScreenshot(minecraft.getMainRenderTarget());
            double scale = image.getWidth() / (double) minecraft.getWindow().getGuiScaledWidth();
            boolean cursor = cursorShown;
            FRAME_TIMES.add(now);
            PENDING.add(WRITERS.submit(() -> {
                try (image) {
                    if (cursor) {
                        drawCursor(image, shownX * scale, shownY * scale);
                    }
                    Path path = outDir.resolve(file);
                    Files.createDirectories(path.getParent());
                    image.writeToFile(path);
                } catch (IOException exception) {
                    TestLog.LOGGER.warn("Media session: could not save {}", file, exception);
                }
            }));
        }

        private static boolean ready(Minecraft minecraft) {
            return Client.screen() instanceof TitleScreen && minecraft.getOverlay() == null || planned;
        }

        /** Language, window size and interface scale, once the game is on the title screen. */
        private static void configure(Minecraft minecraft) {
            minecraft.options.languageCode = LANGUAGE;
            minecraft.getLanguageManager().setSelected(LANGUAGE);
            minecraft.options.guiScale().set(2);
            // The window may lose the focus while the session runs: no pause screen then.
            minecraft.options.pauseOnLostFocus = false;
            minecraft.options.tutorialStep = TutorialSteps.NONE;
            minecraft.getTutorial().setStep(TutorialSteps.NONE);
            GLFW.glfwSetWindowSize(minecraft.getWindow().getWindow(), WIDTH, HEIGHT);
            minecraft.reloadResourcePacks();
            startWait = -40;
        }

        private static void finish(Minecraft minecraft) {
            GaldurConfig.setRestyleAll(restyleBefore);
            ThemeStore.setPlayerTheme(themeBefore, Map.of());
            WRITERS.shutdown();
            TestLog.LOGGER.info("Media session done.");
            minecraft.stop();
        }

        // ---- The script ---------------------------------------------------------------------

        private static void plan(Minecraft minecraft) {
            if (CONTAINERS_ONLY) {
                containers(minecraft);
                return;
            }
            // Photos: the same screen without and with the Galdur look.
            add(scale(3));
            add(run(() -> setLook(false, false)));
            add(openAndShoot("options-vanilla", () -> new OptionsScreen(new TitleScreen(), minecraft.options)));
            add(run(() -> setLook(true, false)));
            add(openAndShoot("options-galdur", () -> new OptionsScreen(new TitleScreen(), minecraft.options)));

            // The settings screens and the editor.
            add(openAndShoot("settings", () -> new GaldurSettingsScreen(null)));
            add(openAndShoot("style", () -> new StyleRulesScreen(null)));
            add(scale(2));
            add(run(() -> setLook(true, false)));
            add(openAndShoot("editor", () -> new ThemeEditorScreen(null)));
            add(run(() -> ThemeStore.setPlayerTheme(Map.of(GaldurTheme.ACCENT, GaldurPaint.solid(0xFF2F6FB5),
                    GaldurTheme.ACCENT_HOVER, GaldurPaint.solid(0xFF4A8BD0),
                    GaldurTheme.ACCENT_PRESSED, GaldurPaint.solid(0xFF24578F),
                    GaldurTheme.PANEL, GaldurPaint.gradient(GaldurPaint.Direction.VERTICAL, 0xFF1E2A38, 0xFF2A2420)),
                    Map.of())));
            add(openAndShoot("editor-gradient", () -> new ThemeEditorScreen(null)));
            add(openAndShoot("share", () -> new ThemeEditorScreen(null).shareScreen()));
            add(run(() -> ThemeStore.setPlayerTheme(Map.of(), Map.of())));

            // Clip: turning the look on for every menu.
            add(scale(3));
            add(run(() -> setLook(false, false)));
            add(record("clip-restyle"));
            add(run(() -> Client.setScreen(new OptionsScreen(new TitleScreen(), minecraft.options))));
            add(pause(30));
            add(run(() -> Client.setScreen(new GaldurSettingsScreen(null))));
            add(pause(14));
            add(move(() -> centerOf("galdurui.configuration.restyleAll"), 22));
            add(pause(1));
            add(click());
            add(move(() -> centerOf("galdurui.theme.galdur"), 12));
            add(pause(10));
            add(run(() -> Client.setScreen(new OptionsScreen(new TitleScreen(), minecraft.options))));
            add(pause(40));
            add(stop());
            add(scale(2));

            // Clip: picking a colour; the whole interface follows live.
            add(run(() -> ThemeStore.setPlayerTheme(Map.of(), Map.of())));
            add(record("clip-theme"));
            add(run(() -> Client.setScreen(new ThemeEditorScreen(null))));
            add(pause(16));
            add(drag(() -> pickerPoint(0F, 0.03F, true), () -> pickerPoint(0F, 0.62F, true), 28));
            add(pause(6));
            add(drag(() -> pickerPoint(0.4F, 0.55F, false), () -> pickerPoint(0.92F, 0.12F, false), 22));
            add(pause(8));
            add(move(() -> centerOf("galdurui.theme.solid"), 14));
            add(toggle("galdurui.theme.solid"));
            add(pause(6));
            add(move(() -> centerOf("galdurui.theme.stop", 1, 2), 12));
            add(toggle("galdurui.theme.stop", 1, 2));
            add(pause(8));
            add(drag(() -> pickerPoint(0F, 0.62F, true), () -> pickerPoint(0F, 0.33F, true), 22));
            add(pause(30));
            add(stop());

            // Clip: sharing a theme.
            add(record("clip-share"));
            add(run(() -> Client.setScreen(new ThemeEditorScreen(null))));
            add(pause(16));
            add(move(() -> centerOf("galdurui.theme.share"), 22));
            add(toggle("galdurui.theme.share"));
            add(pause(16));
            add(move(() -> centerOf("galdurui.theme.share.name"), 16));
            add(click());
            add(type("Ocean"));
            add(pause(8));
            add(move(() -> centerOf("galdurui.theme.share.copy"), 14));
            add(click());
            add(pause(36));
            add(stop());

            // Clip: back to the Galdur theme in one click.
            add(run(() -> ThemeStore.setPlayerTheme(Map.of(GaldurTheme.ACCENT, GaldurPaint.solid(0xFF2F6FB5),
                    GaldurTheme.ACCENT_HOVER, GaldurPaint.solid(0xFF4A8BD0),
                    GaldurTheme.ACCENT_PRESSED, GaldurPaint.solid(0xFF24578F),
                    GaldurTheme.PANEL, GaldurPaint.gradient(GaldurPaint.Direction.VERTICAL, 0xFF1E2A38, 0xFF2A2420)),
                    Map.of())));
            add(record("clip-reset"));
            add(run(() -> Client.setScreen(new ThemeEditorScreen(null))));
            add(pause(30));
            add(move(() -> centerOf("galdurui.theme.galdur"), 24));
            add(pause(4));
            add(click());
            add(pause(40));
            add(stop());
            add(run(() -> ThemeStore.setPlayerTheme(Map.of(), Map.of())));

            // Photos in a world: the inventories, without and with the look, then in three themes.
            add(run(() -> Client.setScreen(new TitleScreen())));
            add(worldStep(minecraft));
            add(scale(3));
            add(run(() -> minecraft.gameMode.setLocalMode(GameType.SURVIVAL)));
            add(run(() -> setLook(false, true)));
            add(openAndShoot("inventory-vanilla", () -> new InventoryScreen(minecraft.player)));
            add(run(() -> setLook(true, true)));
            add(openAndShoot("inventory-galdur", () -> new InventoryScreen(minecraft.player)));
            for (Theme theme : THEMES) {
                add(run(() -> ThemeStore.setPlayerTheme(theme.paints(), Map.of())));
                add(openAndShoot("inventory-" + theme.name(), () -> new InventoryScreen(minecraft.player)));
            }
            add(run(() -> ThemeStore.setPlayerTheme(Map.of(), Map.of())));
            // A furnace halfway through cooking (the flame and the arrow are drawn by Minecraft over the
            // panel), then a crafting table.
            add(run(() -> setLook(false, true)));
            add(openAndShoot("furnace-vanilla", () -> furnace(minecraft)));
            add(run(() -> setLook(true, true)));
            add(openAndShoot("furnace-galdur", () -> furnace(minecraft)));
            add(run(() -> setLook(false, true)));
            add(openAndShoot("crafting-vanilla", () -> crafting(minecraft)));
            add(run(() -> setLook(true, true)));
            add(openAndShoot("crafting-galdur", () -> crafting(minecraft)));
            add(run(() -> minecraft.gameMode.setLocalMode(GameType.CREATIVE)));
            add(run(() -> setLook(false, true)));
            add(openAndShoot("creative-vanilla", () -> creative(minecraft)));
            add(run(() -> setLook(true, true)));
            add(openAndShoot("creative-galdur", () -> creative(minecraft)));
            for (String tab : List.of("inventory", "search")) {
                add(run(() -> setLook(false, true)));
                add(openAndShoot("creative-" + tab + "-vanilla", () -> creative(minecraft, tab)));
                add(run(() -> setLook(true, true)));
                add(openAndShoot("creative-" + tab + "-galdur", () -> creative(minecraft, tab)));
            }
            add(run(() -> Client.setScreen(null)));
        }

        /** Each vanilla screen with slots, without and with the look, then in a dark theme. */
        private static void containers(Minecraft minecraft) {
            add(worldStep(minecraft));
            add(scale(3));
            add(run(() -> minecraft.gameMode.setLocalMode(GameType.SURVIVAL)));
            Map<String, java.util.function.Supplier<Screen>> screens = new java.util.LinkedHashMap<>();
            screens.put("brewing", () -> brewing(minecraft));
            screens.put("anvil", () -> new net.minecraft.client.gui.screens.inventory.AnvilScreen(
                    new net.minecraft.world.inventory.AnvilMenu(0, minecraft.player.getInventory()), minecraft.player.getInventory(), Component.translatable("container.repair")));
            screens.put("smithing", () -> new net.minecraft.client.gui.screens.inventory.SmithingScreen(
                    new net.minecraft.world.inventory.SmithingMenu(0, minecraft.player.getInventory()), minecraft.player.getInventory(), Component.translatable("container.upgrade")));
            screens.put("grindstone", () -> new net.minecraft.client.gui.screens.inventory.GrindstoneScreen(
                    new net.minecraft.world.inventory.GrindstoneMenu(0, minecraft.player.getInventory()), minecraft.player.getInventory(), Component.translatable("container.grindstone_title")));
            screens.put("stonecutter", () -> new net.minecraft.client.gui.screens.inventory.StonecutterScreen(
                    new net.minecraft.world.inventory.StonecutterMenu(0, minecraft.player.getInventory()), minecraft.player.getInventory(), Component.translatable("container.stonecutter")));
            screens.put("cartography", () -> new net.minecraft.client.gui.screens.inventory.CartographyTableScreen(
                    new net.minecraft.world.inventory.CartographyTableMenu(0, minecraft.player.getInventory()), minecraft.player.getInventory(), Component.translatable("container.cartography_table")));
            screens.forEach((name, screen) -> {
                add(run(() -> setLook(false, true)));
                add(openAndShoot(name + "-vanilla", screen::get));
                add(run(() -> setLook(true, true)));
                add(openAndShoot(name + "-galdur", screen::get));
                add(run(() -> ThemeStore.setPlayerTheme(THEMES.get(0).paints(), Map.of())));
                add(openAndShoot(name + "-ocean", screen::get));
                add(run(() -> ThemeStore.setPlayerTheme(Map.of(), Map.of())));
            });
            add(run(() -> Client.setScreen(null)));
        }

        private static Screen brewing(Minecraft minecraft) {
            var inventory = minecraft.player.getInventory();
            var slots = new net.minecraft.world.SimpleContainer(5);
            slots.setItem(0, net.minecraft.world.item.alchemy.PotionContents.createItemStack(
                    net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.WATER));
            slots.setItem(1, net.minecraft.world.item.alchemy.PotionContents.createItemStack(
                    net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.WATER));
            slots.setItem(3, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.NETHER_WART, 8));
            // Halfway through brewing, with some fuel left.
            var data = new net.minecraft.world.inventory.SimpleContainerData(2);
            data.set(0, 200);
            data.set(1, 12);
            return new net.minecraft.client.gui.screens.inventory.BrewingStandScreen(
                    new net.minecraft.world.inventory.BrewingStandMenu(0, inventory, slots, data), inventory,
                    Component.translatable("container.brewing"));
        }

        private record Theme(String name, Map<String, GaldurPaint> paints) {
        }

        private static final List<Theme> THEMES = List.of(
            new Theme("ocean", Map.of(GaldurTheme.ACCENT, GaldurPaint.solid(0xFF2F6FB5),
                GaldurTheme.ACCENT_HOVER, GaldurPaint.solid(0xFF4A8BD0),
                GaldurTheme.PANEL, GaldurPaint.gradient(GaldurPaint.Direction.VERTICAL, 0xFF16324A, 0xFF1E2A38),
                GaldurTheme.SLOT, GaldurPaint.solid(0xFF0F2233))),
            new Theme("forest", Map.of(GaldurTheme.ACCENT, GaldurPaint.solid(0xFF5B8C32),
                GaldurTheme.ACCENT_HOVER, GaldurPaint.solid(0xFF76AA48),
                GaldurTheme.PANEL, GaldurPaint.gradient(GaldurPaint.Direction.VERTICAL, 0xFF24331E, 0xFF1A2216),
                GaldurTheme.SLOT, GaldurPaint.solid(0xFF141C10))),
            new Theme("dusk", Map.of(GaldurTheme.ACCENT, GaldurPaint.solid(0xFFD98A1F),
                GaldurTheme.ACCENT_HOVER, GaldurPaint.solid(0xFFEBA43A),
                GaldurTheme.PANEL, GaldurPaint.gradient(GaldurPaint.Direction.HORIZONTAL, 0xFF3A2147, 0xFF5A2A2A),
                GaldurTheme.SLOT, GaldurPaint.solid(0xFF24142C))));

        private static Screen furnace(Minecraft minecraft) {
            var inventory = minecraft.player.getInventory();
            var slots = new net.minecraft.world.SimpleContainer(3);
            slots.setItem(0, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.RAW_IRON, 12));
            slots.setItem(1, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.COAL, 5));
            slots.setItem(2, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_INGOT, 3));
            // Burning for half its fuel, halfway through the item.
            var data = new net.minecraft.world.inventory.SimpleContainerData(4);
            data.set(0, 800);
            data.set(1, 1600);
            data.set(2, 100);
            data.set(3, 200);
            return new net.minecraft.client.gui.screens.inventory.FurnaceScreen(
                    new net.minecraft.world.inventory.FurnaceMenu(0, inventory, slots, data), inventory,
                    Component.translatable("container.furnace"));
        }

        private static Screen crafting(Minecraft minecraft) {
            var inventory = minecraft.player.getInventory();
            return new net.minecraft.client.gui.screens.inventory.CraftingScreen(
                    new net.minecraft.world.inventory.CraftingMenu(0, inventory), inventory,
                    Component.translatable("container.crafting"));
        }

        /** The creative inventory opened on another tab ({@code inventory}, {@code search}). */
        private static Screen creative(Minecraft minecraft, String tab) {
            try {
                var key = net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.CREATIVE_MODE_TAB,
                        ResourceLocation.withDefaultNamespace(tab));
                Field selected = CreativeModeInventoryScreen.class.getDeclaredField("selectedTab");
                selected.setAccessible(true);
                selected.set(null, net.minecraft.core.registries.BuiltInRegistries.CREATIVE_MODE_TAB.getOrThrow(key));
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException("Creative tab " + tab, exception);
            }
            return creative(minecraft);
        }

        private static Screen creative(Minecraft minecraft) {
            return new CreativeModeInventoryScreen(minecraft.player, minecraft.player.connection.enabledFeatures(), true);
        }

        /** Interface scale: 3 for screens that fit, 2 for the theme editor (it is wide). */
        private static Step scale(int value) {
            return ticks -> {
                Minecraft minecraft = Minecraft.getInstance();
                minecraft.options.guiScale().set(value);
                minecraft.resizeDisplay();
                return true;
            };
        }

        private static void setLook(boolean on, boolean inWorld) {
            GaldurConfig.setRestyleAll(on);
        }

        // ---- Steps -------------------------------------------------------------------------

        private static void add(Step step) {
            STEPS.add(step);
        }

        private static Step run(Runnable action) {
            return ticks -> {
                action.run();
                return true;
            };
        }

        private static Step pause(int duration) {
            return ticks -> ticks >= duration;
        }

        /** Opens a screen, waits for its opening animation and saves a photo of it. */
        private static Step openAndShoot(String name, Supplier<Screen> screen) {
            return ticks -> {
                Minecraft minecraft = Minecraft.getInstance();
                if (ticks == 0) {
                    cursorShown = false;
                    Client.setScreen(screen.get());
                } else if (ticks == 3 || ticks == 20) {
                    // After the screen opened: Minecraft puts the mouse back in the middle when it does.
                    var window = minecraft.getWindow();
                    place(window.getGuiScaledWidth() - 2, window.getGuiScaledHeight() - 2);
                } else if (ticks == 26) {
                    minecraft.gui.getChat().clearMessages(true);
                } else if (ticks == 30) {
                    capture(minecraft, name + ".png", false);
                    return true;
                }
                return false;
            };
        }

        private static Step record(String name) {
            return ticks -> {
                recording = name;
                frame = 0;
                FRAME_TIMES.clear();
                cursorShown = true;
                Minecraft.getInstance().gui.getChat().clearMessages(true);
                return true;
            };
        }

        /** Ends a clip: writes the list of frames with their durations, for ffmpeg. */
        private static Step stop() {
            return ticks -> {
                recording = recording == null ? "" : recording;
                String clip = recording;
                cursorShown = false;
                try {
                    for (var future : PENDING) {
                        future.get();
                    }
                    PENDING.clear();
                } catch (InterruptedException | java.util.concurrent.ExecutionException exception) {
                    TestLog.LOGGER.warn("Media session: a frame could not be written", exception);
                }
                recording = clip;
                try {
                    Path dir = outDir.resolve(recording);
                    StringBuilder list = new StringBuilder();
                    for (int i = 0; i < FRAME_TIMES.size(); i++) {
                        long next = i + 1 < FRAME_TIMES.size() ? FRAME_TIMES.get(i + 1) : FRAME_TIMES.get(i) + 50_000_000L;
                        list.append("file 'f").append(String.format("%04d", i + 1)).append(".png'\n")
                                .append("duration ").append((next - FRAME_TIMES.get(i)) / 1e9).append('\n');
                    }
                    Files.writeString(dir.resolve("frames.txt"), list);
                } catch (IOException exception) {
                    TestLog.LOGGER.warn("Media session: could not write the frame list", exception);
                }
                recording = null;
                cursorShown = false;
                return true;
            };
        }

        private static Step move(Supplier<double[]> target, int duration) {
            double[][] from = new double[1][];
            return ticks -> {
                double[] to = target.get();
                if (to == null) {
                    // The screen may still be rebuilding after a click: wait for the widget up to three seconds.
                    return ticks > 60;
                }
                if (ticks == 0) {
                    from[0] = new double[] {cursorX, cursorY};
                }
                double t = Math.min(1, (ticks + 1) / (double) duration);
                double eased = t * t * (3 - 2 * t);
                place(from[0][0] + (to[0] - from[0][0]) * eased, from[0][1] + (to[1] - from[0][1]) * eased);
                return t >= 1;
            };
        }

        private static Step click() {
            return ticks -> {
                Screen screen = Client.screen();
                if (screen == null) {
                    return true;
                }
                // Press and release in the same tick: a Galdur button forgets a press on every frame
                // where the real mouse button is up.
                screen.mouseClicked(guiX(), guiY(), 0);
                screen.mouseReleased(guiX(), guiY(), 0);
                return true;
            };
        }

        /**
         * Clicks the widget with this text until it is gone from the screen (a button that changes its own
         * label, or that rebuilds the screen): up to three tries, so a click lost while the screen was
         * rebuilding does not break the clip.
         */
        private static Step toggle(String key, Object... args) {
            return ticks -> {
                Screen screen = Client.screen();
                int attempt = ticks / 16;
                int phase = ticks % 16;
                if (screen == null || attempt >= 3) {
                    return true;
                }
                if (phase == 0) {
                    double[] center = find(key, args);
                    if (center == null) {
                        return true;
                    }
                    place(center[0], center[1]);
                    screen.mouseClicked(guiX(), guiY(), 0);
                    screen.mouseReleased(guiX(), guiY(), 0);
                }
                return false;
            };
        }

        private static Step drag(Supplier<double[]> from, Supplier<double[]> to, int duration) {
            return ticks -> {
                Screen screen = Client.screen();
                double[] start = from.get();
                double[] end = to.get();
                if (screen == null || start == null || end == null) {
                    return true;
                }
                if (ticks == 0) {
                    place(start[0], start[1]);
                    screen.mouseClicked(guiX(), guiY(), 0);
                    return false;
                }
                double t = Math.min(1, ticks / (double) duration);
                double oldX = cursorX;
                double oldY = cursorY;
                place(start[0] + (end[0] - start[0]) * t, start[1] + (end[1] - start[1]) * t);
                screen.mouseDragged(guiX(), guiY(), 0, cursorX - oldX, cursorY - oldY);
                if (t >= 1) {
                    screen.mouseReleased(guiX(), guiY(), 0);
                    return true;
                }
                return false;
            };
        }

        /** Types a text into the focused field, one character per tick. */
        private static Step type(String text) {
            return ticks -> {
                Screen screen = Client.screen();
                if (screen == null) {
                    return true;
                }
                if (ticks < text.length()) {
                    screen.charTyped(text.charAt(ticks), 0);
                    return false;
                }
                return true;
            };
        }

        private static Step worldStep(Minecraft minecraft) {
            return ticks -> {
                if (!worldRequested && Client.screen() instanceof TitleScreen title) {
                    worldRequested = true;
                    if (minecraft.getLevelSource().levelExists(WORLD)) {
                        try (var access = minecraft.getLevelSource().createAccess(WORLD)) {
                            access.deleteLevel();
                        } catch (IOException exception) {
                            TestLog.LOGGER.warn("Media session: could not delete the old test world", exception);
                        }
                    }
                    LevelSettings settings = new LevelSettings(WORLD, GameType.CREATIVE, false, Difficulty.PEACEFUL,
                            true, new GameRules(), WorldDataConfiguration.DEFAULT);
                    minecraft.createWorldOpenFlows().createFreshLevel(WORLD, settings, new WorldOptions(0L, false, false),
                            access -> access.registryOrThrow(net.minecraft.core.registries.Registries.WORLD_PRESET)
                                    .getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(), title);
                    return false;
                }
                if (Client.screen() instanceof net.minecraft.client.gui.screens.PauseScreen) {
                    Client.setScreen(null);
                }
                // In the world: a few seconds for the chunks, the loading screen and the tutorial toast.
                if (minecraft.level != null && minecraft.player != null && Client.screen() == null && ticks > 100) {
                    minecraft.options.tutorialStep = TutorialSteps.NONE;
                    var server = minecraft.getSingleplayerServer();
                    server.execute(() -> {
                        var commands = server.getCommands();
                        commands.performPrefixedCommand(server.createCommandSourceStack(), "gamerule doDaylightCycle false");
                        commands.performPrefixedCommand(server.createCommandSourceStack(), "time set noon");
                        commands.performPrefixedCommand(server.createCommandSourceStack(), "weather clear");
                    });
                    return true;
                }
                return false;
            };
        }

        // ---- Cursor, widgets and frames ----------------------------------------------------

        private static double guiX() {
            return cursorX;
        }

        private static double guiY() {
            return cursorY;
        }

        /** Puts the cursor at GUI coordinates; the game reads it as the mouse position (hover states). */
        private static void place(double x, double y) {
            cursorX = x;
            cursorY = y;
            var window = Minecraft.getInstance().getWindow();
            double scaleX = window.getScreenWidth() / (double) window.getGuiScaledWidth();
            double scaleY = window.getScreenHeight() / (double) window.getGuiScaledHeight();
            setField("xpos", x * scaleX);
            setField("ypos", y * scaleY);
        }

        private static void setField(String name, double value) {
            try {
                Field field = net.minecraft.client.MouseHandler.class.getDeclaredField(name);
                field.setAccessible(true);
                field.setDouble(Minecraft.getInstance().mouseHandler, value);
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException("Mouse position field " + name, exception);
            }
        }

        /** {@return the centre of the first widget of the screen whose text is the translation of a key} */
        private static double[] find(String key, Object... args) {
            Screen screen = Client.screen();
            if (screen == null) {
                return null;
            }
            String wanted = Component.translatable(key, args).getString();
            for (var child : screen.children()) {
                if (child instanceof AbstractWidget widget && widget.visible && widget.getMessage().getString().equals(wanted)) {
                    return new double[] {widget.getX() + widget.getWidth() / 2.0, widget.getY() + widget.getHeight() / 2.0};
                }
            }
            return null;
        }

        private static double[] centerOf(String key, Object... args) {
            double[] found = find(key, args);
            if (found != null) {
                return found;
            }
            Screen screen = Client.screen();
            if (screen == null) {
                return null;
            }
            String wanted = Component.translatable(key, args).getString();
            if (LOGGED.add(wanted)) {
                StringBuilder known = new StringBuilder();
                for (var child : screen.children()) {
                    if (child instanceof AbstractWidget widget) {
                        known.append('[').append(widget.getMessage().getString()).append(']');
                    }
                }
                TestLog.LOGGER.warn("Media session: no widget '{}' on {}: {}", wanted, screen.getClass().getSimpleName(), known);
            }
            return null;
        }

        /** A point of the colour picker: in the square (hue bar {@code false}) or on the hue bar ({@code true}). */
        private static double[] pickerPoint(float across, float down, boolean hueBar) {
            Screen screen = Client.screen();
            if (screen == null) {
                return null;
            }
            for (var child : screen.children()) {
                if (child instanceof AbstractWidget widget && child.getClass().getSimpleName().equals("ColorPicker")) {
                    double square = widget.getWidth() * 0.86;
                    double x = hueBar ? widget.getX() + widget.getWidth() - 3 : widget.getX() + across * square;
                    return new double[] {x, widget.getY() + down * widget.getHeight()};
                }
            }
            return null;
        }

        private static void capture(Minecraft minecraft, String file, boolean frameOfClip) {
            try (NativeImage image = Screenshot.takeScreenshot(minecraft.getMainRenderTarget())) {
                if (cursorShown) {
                    drawCursor(image, cursorX * image.getWidth() / minecraft.getWindow().getGuiScaledWidth(),
                            cursorY * image.getWidth() / minecraft.getWindow().getGuiScaledWidth());
                }
                Path path = outDir.resolve(file);
                Files.createDirectories(path.getParent());
                image.writeToFile(path);
                if (frameOfClip) {
                    FRAME_TIMES.add(System.nanoTime());
                }
            } catch (IOException exception) {
                TestLog.LOGGER.warn("Media session: could not save {}", file, exception);
            }
        }

        private static void drawCursor(NativeImage image, double pixelX, double pixelY) {
            int originX = (int) Math.round(pixelX);
            int originY = (int) Math.round(pixelY);
            int size = Math.max(2, image.getWidth() / 640);
            for (int row = 0; row < CURSOR.length; row++) {
                for (int column = 0; column < CURSOR[row].length(); column++) {
                    char mark = CURSOR[row].charAt(column);
                    if (mark == ' ') {
                        continue;
                    }
                    int color = mark == 'X' ? 0xFF000000 : 0xFFFFFFFF;
                    for (int dy = 0; dy < size; dy++) {
                        for (int dx = 0; dx < size; dx++) {
                            int x = originX + column * size + dx;
                            int y = originY + row * size + dy;
                            if (x < image.getWidth() && y < image.getHeight()) {
                                image.setPixelRGBA(x, y, color);
                            }
                        }
                    }
                }
            }
        }
    }
    //?}
}
