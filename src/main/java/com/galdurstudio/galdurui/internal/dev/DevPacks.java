package com.galdurstudio.galdurui.internal.dev;

import com.galdurstudio.galdurui.GaldurUI;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.AddPackFindersEvent;

/**
 * Packs de ressources de développement, proposés dans le menu « Packs de ressources » sans rien
 * copier dans le dossier du jeu.
 *
 * <p>{@code galdurui:resourcepacks/theme_bleu} remplace {@code assets/galdurui/theme.json} par
 * un thème bleu <strong>partiel</strong> : il sert à vérifier qu'un pack change le thème sans
 * recompiler, que les clés absentes retombent sur la charte et qu'une valeur invalide est
 * ignorée avec un avertissement. Le dossier {@code resourcepacks/} est exclu du jar
 * ({@code build.gradle}) : ces packs n'existent qu'en développement.</p>
 */
public final class DevPacks {
    private DevPacks() {
    }

    /**
     * Branche la déclaration des packs sur le bus du mod.
     *
     * @param modEventBus le bus du mod
     */
    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(AddPackFindersEvent.class, DevPacks::onAddPackFinders);
    }

    private static void onAddPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.CLIENT_RESOURCES) {
            return;
        }
        event.addPackFinders(ResourceLocation.fromNamespaceAndPath(GaldurUI.MODID, "resourcepacks/theme_bleu"),
                PackType.CLIENT_RESOURCES, Component.literal("Galdur UI : thème bleu (dev)"),
                PackSource.BUILT_IN, false, Pack.Position.TOP);
    }
}
