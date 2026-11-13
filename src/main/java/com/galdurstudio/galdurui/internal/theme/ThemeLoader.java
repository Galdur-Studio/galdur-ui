package com.galdurstudio.galdurui.internal.theme;

import com.galdurstudio.galdurui.GaldurUI;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import com.galdurstudio.galdurui.internal.render.GuiSprites;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

/**
 * Loads {@code assets/galdurui/theme.json} into {@link ThemeStore} on every client resource
 * reload: F3+T (when no screen is open) applies colour changes without restarting, and a resource
 * pack can replace the file to restyle the library. Missing or invalid keys fall back to
 * {@link ThemeDefaults}.
 *
 * <p>Each loader module registers an instance as a client reload listener (NeoForge and Forge:
 * {@code RegisterClientReloadListenersEvent}; Fabric: {@code ResourceManagerHelper}).</p>
 */
public final class ThemeLoader implements ResourceManagerReloadListener {
    private static final ResourceLocation THEME = GaldurUI.id("theme.json");

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        // Sprites read by Galdur UI itself (Minecraft 1.20.1) must be read again from the new packs.
        GuiSprites.clearCache();
        com.galdurstudio.galdurui.internal.bridge.TextureRecolor.clear();
        loadPackTheme(resourceManager);
        // The colours chosen in the theme editor go over the packs' theme.
        PlayerTheme.load();
    }

    private static void loadPackTheme(ResourceManager resourceManager) {
        Optional<Resource> resource = resourceManager.getResource(THEME);
        if (resource.isEmpty()) {
            GaldurUI.LOGGER.warn("theme.json not found ({}): using the default theme.", THEME);
            ThemeStore.resetToDefaults();
            return;
        }

        try (BufferedReader reader = resource.get().openAsReader()) {
            ThemeParser.Theme theme = ThemeParser.parse(reader, GaldurUI.LOGGER::warn);
            ThemeStore.apply(theme.paints(), theme.spacing());
            GaldurUI.LOGGER.debug("Galdur UI theme loaded ({} colours, {} spacings).",
                    ThemeStore.all().size(), ThemeStore.allSpacing().size());
        } catch (IOException | RuntimeException exception) {
            // A broken theme must never stop the game from starting.
            GaldurUI.LOGGER.warn("Could not read theme.json: using the default theme.", exception);
            ThemeStore.resetToDefaults();
        }
    }
}
