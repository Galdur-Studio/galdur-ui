package com.galdurstudio.galdurui.testmod;

import com.mojang.brigadier.Command;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Test menus and the server command {@code /galduruidev}: {@code chest} gives the test chest,
 * {@code terminal} opens the test terminal.
 *
 * <p>The command has its own root because {@code /galdurui} is a client command, which the server
 * does not know about.</p>
 */
public final class TestMenus {
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, GaldurUITest.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<TestChestMenu>> TEST_CHEST =
            MENUS.register("test_chest", () -> new MenuType<>(TestChestMenu::new, FeatureFlags.DEFAULT_FLAGS));
    public static final DeferredHolder<MenuType<?>, MenuType<TestTerminalMenu>> TEST_TERMINAL =
            MENUS.register("test_terminal", () -> new MenuType<>(TestTerminalMenu::new, FeatureFlags.DEFAULT_FLAGS));

    private TestMenus() {
    }

    public static void register(IEventBus modEventBus) {
        MENUS.register(modEventBus);
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, TestMenus::onRegisterCommands);
    }

    private static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("galduruidev")
                .then(Commands.literal("chest").executes(context -> {
                    giveTestChest(context.getSource().getPlayerOrException());
                    context.getSource().sendSuccess(() -> Component.literal("Gave a Galdur test chest."), false);
                    return Command.SINGLE_SUCCESS;
                }))
                .then(Commands.literal("terminal").executes(context -> {
                    context.getSource().getPlayerOrException().openMenu(new SimpleMenuProvider(
                            (containerId, inventory, player) -> new TestTerminalMenu(containerId, inventory),
                            Component.literal("Test Terminal")));
                    return Command.SINGLE_SUCCESS;
                })));
    }

    /** Gives the player a test chest, or drops it at their feet if the inventory is full. */
    private static void giveTestChest(ServerPlayer player) {
        ItemStack chest = new ItemStack(TestBlocks.TEST_CHEST_ITEM.get());
        if (!player.getInventory().add(chest)) {
            player.drop(chest, false);
        }
    }
}
