package com.galdurstudio.galdurui.api.bridge;

import com.galdurstudio.galdurui.GaldurUI;
import com.galdurstudio.galdurui.api.screen.GaldurContainerScreen;
import com.galdurstudio.galdurui.internal.config.DefaultThemeScreen;
import com.galdurstudio.galdurui.internal.config.GaldurConfig;
import com.galdurstudio.galdurui.internal.bridge.BridgePainter;
import com.galdurstudio.galdurui.internal.bridge.ScreenFilter;
import com.galdurstudio.galdurui.internal.bridge.StyleRules;
import com.galdurstudio.galdurui.internal.client.Client;
import com.galdurstudio.galdurui.internal.mixin.AbstractContainerScreenAccessor;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.DispenserScreen;
import net.minecraft.client.gui.screens.inventory.HopperScreen;
import net.minecraft.client.gui.screens.inventory.ShulkerBoxScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import org.jetbrains.annotations.Nullable;

/**
 * Gives the Galdur UI look to the screens a mod already has, without rewriting them.
 *
 * <p>In a restyled screen, Galdur UI draws buttons and cycle buttons, text fields, checkboxes and
 * sliders with its theme. Minecraft still draws their text and handles input. Only these vanilla
 * widgets change: a mod's own widgets keep their look. Register the screens once on the client,
 * at startup:</p>
 *
 * <pre>{@code
 * GaldurBridge.restyle(MachineScreen.class);                  // one screen and its subclasses
 * GaldurBridge.restylePackage("com.example.mymod");           // every screen of the mod
 * GaldurBridge.restyle(CrateScreen.class, Background.REPLACE); // panel and slots too
 * GaldurBridge.restyle(Screen.class);                         // every screen of the game
 * }</pre>
 *
 * <p>For a screen with slots, {@link Background#KEEP} (the default) keeps the screen's own
 * texture. {@link Background#REPLACE} draws the Galdur panel and slot frames instead, with the
 * labels in the theme text colour, but whatever the screen drew in its background (arrows,
 * gauges) is gone. To keep those, stay on {@code KEEP} and call
 * {@link #drawContainerBackground(GuiGraphics, AbstractContainerScreen)} in place of the texture,
 * in the screen's {@code renderBg} ({@code renderBackground} since 26.1), then draw them on top.
 * The labels then take the theme text colour too.</p>
 *
 * <p>Galdur UI's own screens are never restyled.</p>
 *
 * <p>What a mod registers here is a default. Players choose, element by element ({@link Element}),
 * where the Galdur look applies: every screen, vanilla inventories or menus, a mod, one screen.
 * {@code restyleAll} in their settings restyles every menu at once. To keep a choice for your
 * screens, lock it: {@link #lock(String, Element, boolean)}; {@link #lockTheme(String)} keeps your
 * colours whatever theme the player chose. A modpack can lock values in
 * {@code config/galdurui-style.json} ({@code "locked"} section).</p>
 */
public final class GaldurBridge {
    /** What happens to the background of a restyled screen with slots. */
    public enum Background {
        /** The screen keeps its own background; only the vanilla widgets change. */
        KEEP,
        /** The Galdur panel and slot frames replace the background, and the labels take the theme text colour. */
        REPLACE
    }

    /** A part of the Galdur look, switched on or off per screen. */
    public enum Element {
        /** Buttons and cycle buttons. */
        BUTTONS("buttons"),
        /** Text fields. */
        TEXT_FIELDS("text_fields"),
        /** Checkboxes. */
        CHECKBOXES("checkboxes"),
        /** Sliders. */
        SLIDERS("sliders"),
        /** The Galdur panel and slot frames instead of the texture of a screen with slots. */
        CONTAINER_BACKGROUND("container_background"),
        /** Labels of screens with slots in the theme text colour. */
        LABELS("labels"),
        /** The colours set in the theme editor. When off, the screen keeps its mod's colours and the resource packs'. */
        PLAYER_THEME("player_theme");

        private final String id;

        Element(String id) {
            this.id = id;
        }

        /** {@return the name of the element in {@code galdurui-style.json}} */
        public String id() {
            return id;
        }

        @Nullable
        public static Element byId(String id) {
            for (Element element : values()) {
                if (element.id.equals(id)) {
                    return element;
                }
            }
            return null;
        }

        public static List<String> ids() {
            return Arrays.stream(values()).map(Element::id).toList();
        }
    }

    private static final Map<Class<?>, Background> CLASSES = new ConcurrentHashMap<>();
    private static final List<Map.Entry<String, Background>> PACKAGES = new CopyOnWriteArrayList<>();
    /** Answer per screen class, empty for "not restyled"; cleared on every registration. */
    private static final Map<Class<?>, Optional<Background>> RESOLVED = new ConcurrentHashMap<>();

    /**
     * Vanilla screens with slots whose background is only a texture: in the player's automatic
     * mode, the Galdur panel replaces it without losing anything (no arrow, flame or gauge).
     */
    private static final Set<Class<?>> PLAIN_CONTAINERS =
            Set.of(ContainerScreen.class, ShulkerBoxScreen.class, DispenserScreen.class, HopperScreen.class);
    /** Whether the player excluded each screen class; cleared when the exclusion list changes. */
    private static final Map<Class<?>, Boolean> EXCLUDED = new ConcurrentHashMap<>();
    /** Exclusion list {@link #EXCLUDED} was computed with. */
    private static volatile List<? extends String> exclusionSource = List.of();
    /** Screens already named in the log, so each one appears once. */
    private static final Set<String> LOGGED = ConcurrentHashMap.newKeySet();

    private GaldurBridge() {
    }

    /** Restyles a screen and its subclasses, keeping its background. */
    public static void restyle(Class<? extends Screen> screen) {
        restyle(screen, Background.KEEP);
    }

    /** Restyles a screen and its subclasses. */
    public static void restyle(Class<? extends Screen> screen, Background background) {
        CLASSES.put(screen, background);
        RESOLVED.clear();
    }

    /**
     * Restyles every screen whose class is in this package or below, keeping their background:
     * {@code "com.example.mymod"} for a whole mod.
     */
    public static void restylePackage(String packageName) {
        restylePackage(packageName, Background.KEEP);
    }

    /** Restyles every screen whose class is in this package or below. */
    public static void restylePackage(String packageName, Background background) {
        PACKAGES.add(Map.entry(packageName.endsWith(".") ? packageName : packageName + ".", background));
        RESOLVED.clear();
    }

    /**
     * Locks an element on your screens: players cannot change it. A modpack's lock on the same element
     * wins over yours. For instance
     * {@code lock("com.example.mymod", Element.CONTAINER_BACKGROUND, false)} keeps your textures.
     *
     * @param packageOrClass a package ({@code com.example.mymod}) or a fully qualified screen class name
     * @param element        what to lock
     * @param value          {@code true} to always apply it, {@code false} never
     */
    public static void lock(String packageOrClass, Element element, boolean value) {
        RULES.lockForMod(scopeOf(packageOrClass), element, value);
        changed();
    }

    /**
     * Keeps your colours on your screens, whatever theme the player chose: the player's colours do
     * not apply there.
     *
     * @param modIdOrPackage your mod id (for your menus with slots) or a package (for your screens)
     */
    public static void lockTheme(String modIdOrPackage) {
        String scope = modIdOrPackage.contains(".") ? scopeOf(modIdOrPackage) : "modid:" + modIdOrPackage;
        RULES.lockForMod(scope, Element.PLAYER_THEME, false);
        changed();
    }

    /** {@return {@code true} if {@code element} of the Galdur look applies to {@code screen}} */
    public static boolean applies(@Nullable Screen screen, Element element) {
        return answer(screen, element).value();
    }

    /** {@return whether {@code element} applies to {@code screen}, and who decided it} (settings screen) */
    public static StyleRules.Answer answer(@Nullable Screen screen, Element element) {
        if (screen == null) {
            return new StyleRules.Answer(element == Element.PLAYER_THEME, StyleRules.Source.DEFAULT, "");
        }
        if (element != Element.PLAYER_THEME
                && (screen instanceof GaldurContainerScreen<?> || isGaldurScreen(screen.getClass()))) {
            return new StyleRules.Answer(false, StyleRules.Source.DEFAULT, "");
        }
        return rules().resolve(target(screen), element, fallback(screen.getClass(), element));
    }

    /**
     * {@return whether {@code element} applies to the screen class {@code className}, and who decided
     * it} (settings screen, for a screen shown earlier); {@code null} if the class is not loaded
     */
    @Nullable
    public static StyleRules.Answer answerFor(String className, Element element) {
        Class<?> type;
        try {
            type = Class.forName(className, false, GaldurBridge.class.getClassLoader());
        } catch (ClassNotFoundException | LinkageError exception) {
            return null;
        }
        List<String> parents = new ArrayList<>();
        for (Class<?> parent = type.getSuperclass(); parent != null && parent != Screen.class && parent != Object.class;
             parent = parent.getSuperclass()) {
            parents.add(parent.getName());
        }
        StyleRules.Target target = new StyleRules.Target(className, parents,
                AbstractContainerScreen.class.isAssignableFrom(type), null);
        return rules().resolve(target, element, fallback(type, element));
    }

    /** {@return {@code true} if the player's colours apply to the screen shown} (cached per screen) */
    public static boolean playerThemeAllowedHere() {
        Screen screen = Client.screen();
        Object[] cache = themeCache;
        if (cache[0] == screen && (int) cache[1] == version) {
            return (boolean) cache[2];
        }
        // The settings screens keep the default theme: the way back when a theme went wrong.
        boolean allowed = !(screen instanceof DefaultThemeScreen) && applies(screen, Element.PLAYER_THEME);
        themeCache = new Object[] {screen, version, allowed};
        return allowed;
    }

    /** {@return {@code true} if a modpack locked the player's theme everywhere: the theme editor is off} */
    public static boolean playerThemeLockedEverywhere() {
        return Boolean.FALSE.equals(rules().lockedValue(StyleRules.ALL, Element.PLAYER_THEME));
    }

    /** {@return a number that changes whenever the rules change, for caches} */
    public static int rulesVersion() {
        return version;
    }

    /** {@return the player's and modpack's rules} (settings screen) */
    public static StyleRules rules() {
        if (!rulesLoaded && Minecraft.getInstance() != null) {
            rulesLoaded = true;
            loadRules();
        }
        return RULES;
    }

    /** Saves the player's rules and applies them. */
    public static void saveRules() {
        rules();
        RULES.save(rulesFile());
        changed();
    }

    /** Reads {@code config/galdurui-style.json} again; called at startup. */
    public static void loadRules() {
        RULES.load(rulesFile());
        changed();
    }

    /** {@return the screens shown since the game started, by class name, for the settings} */
    public static List<String> seenScreens() {
        List<String> seen = new ArrayList<>(SEEN);
        Collections.sort(seen);
        return seen;
    }

    private static final StyleRules RULES = new StyleRules();
    private static final Set<String> SEEN = Collections.synchronizedSet(new LinkedHashSet<>());
    private static volatile int version;
    private static volatile boolean rulesLoaded;
    private static volatile Object[] themeCache = {null, -1, true};

    private static void changed() {
        version++;
        RESOLVED.clear();
    }

    private static Path rulesFile() {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve("galdurui-style.json");
    }

    private static String scopeOf(String packageOrClass) {
        String last = packageOrClass.substring(packageOrClass.lastIndexOf('.') + 1);
        return !last.isEmpty() && Character.isUpperCase(last.charAt(0)) ? "screen:" + packageOrClass : "mod:" + packageOrClass;
    }

    private static StyleRules.Target target(Screen screen) {
        Class<?> type = screen.getClass();
        SEEN.add(type.getName());
        List<String> parents = new ArrayList<>();
        for (Class<?> parent = type.getSuperclass(); parent != null && parent != Screen.class && parent != Object.class;
             parent = parent.getSuperclass()) {
            parents.add(parent.getName());
        }
        String namespace = null;
        if (screen instanceof AbstractContainerScreen<?> container) {
            try {
                var key = BuiltInRegistries.MENU.getKey(container.getMenu().getType());
                namespace = key == null ? null : key.getNamespace();
            } catch (UnsupportedOperationException exception) {
                // The player inventory has no menu type.
            }
        }
        return new StyleRules.Target(type.getName(), parents, screen instanceof AbstractContainerScreen<?>, namespace);
    }

    /** What applies when neither the player nor a lock says anything: the mods' registrations and restyleAll. */
    private static boolean fallback(Class<?> type, Element element) {
        if (element == Element.PLAYER_THEME) {
            return true;
        }
        Background registered = RESOLVED.computeIfAbsent(type, key -> Optional.ofNullable(resolve(key))).orElse(null);
        Background chosen = registered != null ? registered : automatic(type);
        if (chosen == null || excluded(type)) {
            return false;
        }
        return switch (element) {
            case CONTAINER_BACKGROUND, LABELS -> chosen == Background.REPLACE;
            default -> true;
        };
    }

    /** {@return {@code true} if this screen is restyled} */
    public static boolean isRestyled(@Nullable Screen screen) {
        return background(screen) != null;
    }

    /** {@return how this screen's background is restyled, {@code null} if the screen is not restyled} */
    @Nullable
    public static Background background(@Nullable Screen screen) {
        if (screen == null) {
            return null;
        }
        if (applies(screen, Element.CONTAINER_BACKGROUND)) {
            return Background.REPLACE;
        }
        for (Element element : List.of(Element.BUTTONS, Element.TEXT_FIELDS, Element.CHECKBOXES, Element.SLIDERS)) {
            if (applies(screen, element)) {
                return Background.KEEP;
            }
        }
        return null;
    }

    /**
     * Draws the Galdur panel and a frame for each active slot, where the screen's background
     * texture used to go. Call it from the screen's {@code renderBg} ({@code renderBackground}
     * since 26.1) in place of the texture; the screen's labels then take the theme text colour.
     */
    public static void drawContainerBackground(GuiGraphics graphics, AbstractContainerScreen<?> screen) {
        BridgePainter.containerBackground(graphics, screen, (AbstractContainerScreenAccessor) screen);
    }

    /** {@return the restyle of the screen currently shown, {@code null} if none} */
    @Nullable
    public static Background current() {
        return background(Client.screen());
    }

    /** The player's automatic mode: every menu not excluded in the settings. */
    @Nullable
    private static Background automatic(Class<?> type) {
        if (!GaldurConfig.restyleAll() || excluded(type)) {
            return null;
        }
        // Named once per menu, so a player can find what to put in excludedScreens.
        if (LOGGED.add(type.getName())) {
            GaldurUI.LOGGER.info("Galdur look given to {}.", type.getName());
        }
        // The player and creative inventories keep their tabs, bar and player: their textures are swapped one by one.
        boolean replaced = PLAIN_CONTAINERS.contains(type) || type == InventoryScreen.class
                || type == CreativeModeInventoryScreen.class;
        return replaced ? Background.REPLACE : Background.KEEP;
    }

    /** {@return {@code true} if the player's {@code excludedScreens} names this screen, a parent class or its package} */
    private static boolean excluded(Class<?> type) {
        List<? extends String> excluded = GaldurConfig.excludedScreens();
        if (excluded != exclusionSource) {
            exclusionSource = excluded;
            EXCLUDED.clear();
        }
        return EXCLUDED.computeIfAbsent(type, key -> ScreenFilter.excludes(key, excluded));
    }

    /** Galdur UI's own screens (windows, settings...) already use the theme. */
    private static boolean isGaldurScreen(Class<?> type) {
        String name = type.getName();
        return name.startsWith("com.galdurstudio.galdurui.api.") || name.startsWith("com.galdurstudio.galdurui.internal.");
    }

    @Nullable
    private static Background resolve(Class<?> type) {
        for (Class<?> current = type; current != null && current != Object.class; current = current.getSuperclass()) {
            Background exact = CLASSES.get(current);
            if (exact != null) {
                return exact;
            }
        }
        String name = type.getName();
        for (Map.Entry<String, Background> entry : PACKAGES) {
            if (name.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }
}
