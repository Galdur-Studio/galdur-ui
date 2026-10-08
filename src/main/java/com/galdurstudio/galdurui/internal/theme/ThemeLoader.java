package com.galdurstudio.galdurui.internal.theme;

import com.galdurstudio.galdurui.GaldurUI;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;

/**
 * Charge {@code assets/galdurui/theme.json} et alimente {@link ThemeStore}.
 *
 * <p>Branché sur le gestionnaire de ressources client : le thème est donc relu à chaque
 * rechargement des ressources, y compris via F3+T, ce qui permet d'ajuster les couleurs
 * sans redémarrer le jeu. Comme le fichier vit dans {@code assets/}, un pack de ressources
 * peut le remplacer pour retoucher la charte.</p>
 *
 * <p>Le format est décrit et lu par {@link ThemeParser}. Les clés absentes ou invalides
 * retombent sur {@link ThemeDefaults}.</p>
 */
public final class ThemeLoader implements ResourceManagerReloadListener {
    /** Emplacement du thème, résolu en {@code assets/galdurui/theme.json}. */
    private static final ResourceLocation THEME = ResourceLocation.fromNamespaceAndPath(GaldurUI.MODID, "theme.json");

    /**
     * Enregistre le chargeur sur le gestionnaire de ressources client.
     *
     * @param modEventBus bus d'événements du mod, fourni au constructeur du point d'entrée client
     */
    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(RegisterClientReloadListenersEvent.class,
                event -> event.registerReloadListener(new ThemeLoader()));
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        Optional<Resource> resource = resourceManager.getResource(THEME);
        if (resource.isEmpty()) {
            GaldurUI.LOGGER.warn("theme.json introuvable ({}) : utilisation du thème par défaut.", THEME);
            ThemeStore.resetToDefaults();
            return;
        }

        try (BufferedReader reader = resource.get().openAsReader()) {
            ThemeParser.Theme theme = ThemeParser.parse(reader, GaldurUI.LOGGER::warn);
            ThemeStore.apply(theme.colors(), theme.spacing());
            GaldurUI.LOGGER.debug("Thème Galdur UI chargé ({} couleurs, {} espacements).",
                    ThemeStore.all().size(), ThemeStore.allSpacing().size());
        } catch (IOException | RuntimeException exception) {
            // Un thème cassé ne doit jamais empêcher le jeu de démarrer : on se rabat sur la charte.
            GaldurUI.LOGGER.warn("Lecture de theme.json impossible : utilisation du thème par défaut.", exception);
            ThemeStore.resetToDefaults();
        }
    }
}
