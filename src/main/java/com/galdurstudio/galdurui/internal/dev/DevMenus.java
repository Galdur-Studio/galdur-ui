package com.galdurstudio.galdurui.internal.dev;

import com.galdurstudio.galdurui.GaldurUI;
import com.mojang.brigadier.Command;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Menus de développement (client <strong>et</strong> serveur) : le coffre de test et la commande
 * serveur {@code /galduruidev chest} qui l'ouvre.
 *
 * <p>Enregistré seulement hors production, par {@code GaldurUI}. Le contenu du coffre est gardé
 * en mémoire par joueur, le temps de la session : il est perdu au redémarrage du serveur. Le
 * coffre posable (bloc et entité de bloc) viendra avec UI-32.</p>
 *
 * <p>La commande a sa propre racine : {@code /galdurui} est une commande <em>client</em>, que le
 * serveur ne voit pas.</p>
 */
public final class DevMenus {
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, GaldurUI.MODID);

    /** Le coffre de test : 27 cases et l'inventaire du joueur. */
    public static final DeferredHolder<MenuType<?>, MenuType<TestChestMenu>> TEST_CHEST =
            MENUS.register("test_chest", () -> new MenuType<>(TestChestMenu::new, FeatureFlags.DEFAULT_FLAGS));

    /** Contenu du coffre de test de chaque joueur, côté serveur. */
    private static final Map<UUID, SimpleContainer> CHESTS = new HashMap<>();

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
                    openTestChest(context.getSource().getPlayerOrException());
                    return Command.SINGLE_SUCCESS;
                })));
    }

    /** Ouvre au joueur son coffre de test (un par joueur, créé au premier appel). */
    private static void openTestChest(ServerPlayer player) {
        SimpleContainer chest = CHESTS.computeIfAbsent(player.getUUID(), uuid -> new SimpleContainer(TestChestMenu.SIZE));
        player.openMenu(new SimpleMenuProvider(
                (containerId, inventory, opener) -> new TestChestMenu(containerId, inventory, chest),
                Component.literal("Coffre Galdur")));
    }
}
