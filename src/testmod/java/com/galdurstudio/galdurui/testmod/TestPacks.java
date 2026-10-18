package com.galdurstudio.galdurui.testmod;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.AddPackFindersEvent;

/**
 * Adds the "blue theme" resource pack to the Resource Packs menu, without copying anything into
 * the game folder. The pack replaces {@code assets/galdurui/theme.json} with a partial theme, to
 * check that a pack changes the theme without recompiling, that missing keys fall back to the
 * defaults and that an invalid value is ignored with a warning.
 */
public final class TestPacks {
    private TestPacks() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(AddPackFindersEvent.class, TestPacks::onAddPackFinders);
    }

    private static void onAddPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.CLIENT_RESOURCES) {
            return;
        }
        event.addPackFinders(ResourceLocation.fromNamespaceAndPath(GaldurUITest.MODID, "resourcepacks/blue_theme"),
                PackType.CLIENT_RESOURCES, Component.literal("Galdur UI: blue theme (test)"),
                PackSource.BUILT_IN, false, Pack.Position.TOP);
    }
}
