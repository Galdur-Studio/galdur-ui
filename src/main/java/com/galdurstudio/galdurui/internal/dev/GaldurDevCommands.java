package com.galdurstudio.galdurui.internal.dev;

import com.galdurstudio.galdurui.GaldurUI;
import com.galdurstudio.galdurui.api.GaldurLib;
import com.galdurstudio.galdurui.api.window.GaldurWindow;
import com.mojang.brigadier.Command;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Commandes client réservées au développement de Galdur UI.
 *
 * <ul>
 *   <li>{@code /galdurui test} ouvre {@link TestScreen} ;</li>
 *   <li>{@code /galdurui debug} active ou désactive le contour des layouts ({@link LayoutDebug}) ;</li>
 *   <li>{@code /galdurui window} ouvre une fenêtre construite avec l'API publique
 *       ({@link com.galdurstudio.galdurui.api.GaldurLib}), pour la tester en jeu.</li>
 * </ul>
 *
 * <p>L'enregistrement est conditionné à l'environnement de développement par l'appelant
 * ({@code GaldurUIClient}) : ces commandes n'existent pas dans une version publiée.</p>
 */
public final class GaldurDevCommands {
    private GaldurDevCommands() {
    }

    /**
     * Branche les commandes de développement sur le bus de jeu.
     */
    public static void register() {
        NeoForge.EVENT_BUS.addListener(RegisterClientCommandsEvent.class, GaldurDevCommands::onRegisterClientCommands);
    }

    private static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal(GaldurUI.MODID)
                .then(Commands.literal("test").executes(context -> {
                    openTestScreen();
                    return Command.SINGLE_SUCCESS;
                }))
                .then(Commands.literal("debug").executes(context -> {
                    boolean isDebug = LayoutDebug.toggle();
                    context.getSource().sendSuccess(() -> Component.literal("Galdur UI: Le mode debug des layouts est " + (isDebug ? "activé." : "désactivé.")), false);
                    return Command.SINGLE_SUCCESS;
                }))
                .then(Commands.literal("window").executes(context -> {
                    GaldurWindow window = GaldurLib.createWindow();
                    window.addTitle(Component.literal("Inventaire"), true);
                    window.addText(Component.literal("Salut"), true);
                    window.addButton(Component.literal("Quitter"), window::close, true);
                    window.open();
                    return Command.SINGLE_SUCCESS;
                }))
        );
    }

    /**
     * Ouvre l'écran de test au tick suivant.
     *
     * <p>La commande s'exécute pendant que l'écran de chat est encore à l'écran : ouvrir
     * directement reviendrait à se faire recouvrir par la fermeture du chat. On diffère donc
     * l'ouverture d'un tour de boucle principale.</p>
     */
    private static void openTestScreen() {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.tell(() -> minecraft.setScreen(new TestScreen()));
    }
}
