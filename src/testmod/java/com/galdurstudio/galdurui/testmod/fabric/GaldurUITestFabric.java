package com.galdurstudio.galdurui.testmod.fabric;

import com.galdurstudio.galdurui.internal.client.Client;
import com.galdurstudio.galdurui.testmod.DemoWindow;
import com.galdurstudio.galdurui.testmod.BridgeDemoScreen;
import com.galdurstudio.galdurui.testmod.MediaSession;
import com.galdurstudio.galdurui.testmod.ScreenshotTour;
import com.galdurstudio.galdurui.testmod.TestScreen;
import com.mojang.brigadier.Command;
import net.fabricmc.api.ClientModInitializer;
//? if >=26.1 {
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
//?} else {
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
//?}
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;

/**
 * Fabric test mod: the demo screens of Galdur UI, to look at the widgets by eye.
 * {@code /galdurui test} opens every widget, {@code /galdurui window} the declarative window demo.
 * Development only, never in the library jar.
 */
public final class GaldurUITestFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(ScreenshotTour::tick);
        ClientTickEvents.END_CLIENT_TICK.register(MediaSession::tick);
        BridgeDemoScreen.register();
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(literal("galdurui")
                        .then(literal("test").executes(context -> {
                            // The chat screen is still open while the command runs: open on the next loop,
                            // otherwise closing the chat would replace our screen.
                            Minecraft minecraft = Minecraft.getInstance();
                            Client.later(() -> Client.setScreen(new TestScreen()));
                            return Command.SINGLE_SUCCESS;
                        }))
                        .then(literal("window").executes(context -> {
                            DemoWindow.open();
                            return Command.SINGLE_SUCCESS;
                        }))));
    }

    /** Fabric API renamed ClientCommandManager to ClientCommands in 26.1. */
    private static LiteralArgumentBuilder<FabricClientCommandSource> literal(String name) {
        //? if >=26.1 {
        return ClientCommands.literal(name);
        //?} else {
        return ClientCommandManager.literal(name);
        //?}
    }
}
