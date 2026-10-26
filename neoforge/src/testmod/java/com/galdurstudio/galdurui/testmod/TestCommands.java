package com.galdurstudio.galdurui.testmod;

import com.mojang.brigadier.Command;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Client commands of the test mod:
 * <ul>
 *   <li>{@code /galdurui test} opens {@link TestScreen}, every widget used directly;</li>
 *   <li>{@code /galdurui window} opens a window built only with the public declarative API;</li>
 *   <li>{@code /galdurui debug} toggles widget outlines ({@link LayoutDebug});</li>
 *   <li>{@code /galdurui fps} toggles the FPS counter ({@link FpsOverlay}).</li>
 * </ul>
 */
public final class TestCommands {
    private TestCommands() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(RegisterClientCommandsEvent.class, TestCommands::onRegisterClientCommands);
    }

    private static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("galdurui")
                .then(Commands.literal("test").executes(context -> {
                    // The chat screen is still open while the command runs: open on the next loop,
                    // otherwise closing the chat would replace our screen.
                    Minecraft minecraft = Minecraft.getInstance();
                    minecraft.tell(() -> minecraft.setScreen(new TestScreen()));
                    return Command.SINGLE_SUCCESS;
                }))
                .then(Commands.literal("window").executes(context -> {
                    DemoWindow.open();
                    return Command.SINGLE_SUCCESS;
                }))
                .then(Commands.literal("debug").executes(context -> {
                    boolean on = LayoutDebug.toggle();
                    context.getSource().sendSuccess(() -> Component.literal("Galdur UI: layout debug " + (on ? "on." : "off.")), false);
                    return Command.SINGLE_SUCCESS;
                }))
                .then(Commands.literal("fps").executes(context -> {
                    boolean on = FpsOverlay.toggle();
                    context.getSource().sendSuccess(() -> Component.literal("Galdur UI: FPS counter "
                            + (on ? "shown on open screens." : "hidden.")), false);
                    return Command.SINGLE_SUCCESS;
                })));
    }
}
