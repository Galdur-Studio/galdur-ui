package com.galdurstudio.galdurui.internal.dev;

import com.galdurstudio.galdurui.GaldurUI;
import com.galdurstudio.galdurui.api.GaldurLib;
import com.galdurstudio.galdurui.api.window.CheckboxHandle;
import com.galdurstudio.galdurui.api.window.ElementHandle;
import com.galdurstudio.galdurui.api.window.GaldurWindow;
import com.galdurstudio.galdurui.api.window.ListHandle;
import com.galdurstudio.galdurui.api.window.RowHandle;
import com.galdurstudio.galdurui.api.window.SearchHandle;
import com.mojang.brigadier.Command;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
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
                    openDemoWindow();
                    return Command.SINGLE_SUCCESS;
                }))
        );
    }

    /**
     * Ouvre une fenêtre de démonstration construite uniquement avec l'API publique.
     *
     * <p>Elle montre les états communs : un texte caché qui libère sa place (case « Afficher le
     * détail »), un bouton désactivé avec infobulle que la case « Mode avancé » active, et une
     * infobulle sur une case, un champ de recherche qui filtre une liste défilante de minerais, dont la
     * sélection active le bouton
     * « Prendre », et une ligne centrée de boutons. Ouverte depuis
     * l'écran de test, Échap et « Quitter » y reviennent.</p>
     */
    static void openDemoWindow() {
        GaldurWindow window = GaldurLib.createWindow();
        window.addTitle(Component.literal("Inventaire"), true);

        ElementHandle detail = window.addText(Component.literal("12 objets, 3 types différents"), true);
        detail.setVisible(false);
        CheckboxHandle showDetail = window.addCheckbox(Component.literal("Afficher le détail"), false);
        showDetail.onChange(detail::setVisible);

        CheckboxHandle advanced = window.addCheckbox(Component.literal("Mode avancé"), false);
        advanced.setTooltip(Component.literal("Débloque les options avancées"));

        List<String> allOres = List.of("Charbon", "Fer", "Cuivre", "Or", "Redstone", "Lapis-lazuli",
                "Diamant", "Émeraude", "Quartz du Nether", "Débris antiques");
        SearchHandle search = window.addSearch(Component.literal("Rechercher un minerai..."), true);
        ListHandle ores = window.addList(5, true);
        ores.setLines(filterOres(allOres, ""));
        search.onChange(query -> ores.setLines(filterOres(allOres, query)));

        RowHandle actions = window.addRow(true);
        ElementHandle take = actions.addButton(Component.literal("Prendre"),
                () -> GaldurUI.LOGGER.info("Prendre : {}", ores.getLines().get(ores.getSelected()).getString()));
        take.setEnabled(false);
        take.setTooltip(Component.literal("Choisis d'abord un minerai dans la liste"));
        ores.onSelect(index -> take.setEnabled(index >= 0));
        ElementHandle options = actions.addButton(Component.literal("Options avancées"),
                () -> GaldurUI.LOGGER.info("Options avancées"));
        options.setEnabled(false);
        options.setTooltip(Component.literal("Coche « Mode avancé » pour y accéder"));
        advanced.onChange(options::setEnabled);
        actions.addButton(Component.literal("Quitter"), window::close);
        window.open();
    }

    /**
     * {@return les minerais dont le nom contient {@code query}, sans tenir compte des majuscules
     * ni des accents (« emer » trouve « Émeraude »)}
     */
    private static List<Component> filterOres(List<String> ores, String query) {
        String wanted = simplify(query.trim());
        return ores.stream()
                .filter(ore -> simplify(ore).contains(wanted))
                .map(ore -> (Component) Component.literal(ore))
                .toList();
    }

    /** {@return {@code text} en minuscules et sans accents} */
    private static String simplify(String text) {
        return Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
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
