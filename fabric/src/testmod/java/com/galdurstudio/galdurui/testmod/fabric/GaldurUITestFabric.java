package com.galdurstudio.galdurui.testmod.fabric;

import com.galdurstudio.galdurui.testmod.DemoWindow;
import com.galdurstudio.galdurui.testmod.TestScreen;
import com.mojang.brigadier.Command;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.Minecraft;

/**
 * Fabric test mod: the demo screens of Galdur UI, to look at the widgets by eye.
 * {@code /galdurui test} opens every widget, {@code /galdurui window} the declarative window demo.
 * Development only, never in the library jar.
 */
public final class GaldurUITestFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal("galdurui")
                        .then(ClientCommandManager.literal("test").executes(context -> {
                            // The chat screen is still open while the command runs: open on the next loop,
                            // otherwise closing the chat would replace our screen.
                            Minecraft minecraft = Minecraft.getInstance();
                            minecraft.tell(() -> minecraft.setScreen(new TestScreen()));
                            return Command.SINGLE_SUCCESS;
                        }))
                        .then(ClientCommandManager.literal("window").executes(context -> {
                            DemoWindow.open();
                            return Command.SINGLE_SUCCESS;
                        }))));
    }
}
