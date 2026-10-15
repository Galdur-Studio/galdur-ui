package com.galdurstudio.galdurui.internal.dev;

import com.galdurstudio.galdurui.GaldurUI;
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
 * Menus de développement (client <strong>et</strong> serveur) : le menu du coffre Galdur de test
 * ({@link DevBlocks}) et du terminal de test, et la commande serveur {@code /galduruidev} :
 * {@code chest} donne le coffre au joueur, {@code terminal} ouvre le terminal de test.
 *
 * <p>Enregistré seulement hors production, par {@code GaldurUI}. La commande a sa propre racine :
 * {@code /galdurui} est une commande <em>client</em>, que le serveur ne voit pas.</p>
 */
public final class DevMenus {
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, GaldurUI.MODID);

    /** Le coffre de test : 27 cases et l'inventaire du joueur. */
    public static final DeferredHolder<MenuType<?>, MenuType<TestChestMenu>> TEST_CHEST =
            MENUS.register("test_chest", () -> new MenuType<>(TestChestMenu::new, FeatureFlags.DEFAULT_FLAGS));

    /** Le terminal de test : une grille d'objets (côté client) et l'inventaire du joueur. */
    public static final DeferredHolder<MenuType<?>, MenuType<TestTerminalMenu>> TEST_TERMINAL =
            MENUS.register("test_terminal", () -> new MenuType<>(TestTerminalMenu::new, FeatureFlags.DEFAULT_FLAGS));

    private DevMenus() {
    }

    /**
     * Enregistre les menus sur le bus du mod et la commande sur le bus du jeu.
     *
     * @param modEventBus le bus du mod
     */
    public static void register(IEventBus modEventBus) {
        MENUS.register(modEventBus);
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, DevMenus::onRegisterCommands);
    }

    private static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("galduruidev")
                .then(Commands.literal("chest").executes(context -> {
                    giveTestChest(context.getSource().getPlayerOrException());
                    context.getSource().sendSuccess(() -> Component.literal("Coffre Galdur de test donné."), false);
                    return Command.SINGLE_SUCCESS;
                }))
                .then(Commands.literal("terminal").executes(context -> {
                    context.getSource().getPlayerOrException().openMenu(new SimpleMenuProvider(
                            (containerId, inventory, player) -> new TestTerminalMenu(containerId, inventory),
                            Component.literal("Terminal de test")));
                    return Command.SINGLE_SUCCESS;
                })));
    }

    /** Donne un coffre Galdur de test au joueur, ou le pose à ses pieds si l'inventaire est plein. */
    private static void giveTestChest(ServerPlayer player) {
        ItemStack chest = new ItemStack(DevBlocks.TEST_CHEST_ITEM.get());
        if (!player.getInventory().add(chest)) {
            player.drop(chest, false);
        }
    }
}
