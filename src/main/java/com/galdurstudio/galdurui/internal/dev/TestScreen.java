package com.galdurstudio.galdurui.internal.dev;

import com.galdurstudio.galdurui.GaldurUI;
import com.galdurstudio.galdurui.api.layout.GaldurPanelLayout;
import com.galdurstudio.galdurui.api.theme.GaldurTheme;
import com.galdurstudio.galdurui.api.widget.GaldurButton;
import com.galdurstudio.galdurui.api.widget.GaldurCheckbox;
import com.galdurstudio.galdurui.api.widget.GaldurIconButton;
import com.galdurstudio.galdurui.api.widget.GaldurItemGrid;
import com.galdurstudio.galdurui.api.widget.GaldurItemSlot;
import com.galdurstudio.galdurui.api.widget.GaldurLabel;
import com.galdurstudio.galdurui.api.widget.GaldurProgressBar;
import com.galdurstudio.galdurui.api.widget.GaldurScrollList;
import com.galdurstudio.galdurui.api.widget.GaldurSearchField;
import com.galdurstudio.galdurui.api.widget.GaldurTabs;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Écran de démonstration des widgets de Galdur UI : chaque composant y est visible et
 * testable, avec ses états (survol, focus clavier, désactivé...).
 *
 * <p>Outil de développement, ouvert par {@code /galdurui test}. Il ne fait pas partie de l'API
 * et n'est pas enregistré dans une version publiée. Il utilise les widgets de
 * {@code api.widget} directement, dans des layouts vanilla ; {@code /galdurui window} montre,
 * lui, les mêmes composants à travers l'API déclarative {@code GaldurWindow}.</p>
 *
 * <p>Les composants sont rangés par onglets (une {@link GaldurTabs} utilisée seule) :</p>
 * <ul>
 *   <li><strong>Boutons</strong> : bouton actif avec compteur, bouton désactivé, libellés de
 *       longueurs variées, boutons icônes (objet, désactivé), cases à cocher (active,
 *       désactivée) ;</li>
 *   <li><strong>Texte</strong> : libellé normal, atténué, tronqué avec « … », et le découpage
 *       (scissor) de {@link ClipDemo} ;</li>
 *   <li><strong>Liste</strong> : champ de recherche qui filtre une liste défilante, champ et
 *       liste désactivés ;</li>
 *   <li><strong>Objets</strong> : cases d'objet (quantités abrégées, usure, case vide,
 *       désactivée) ;</li>
 *   <li><strong>Grille</strong> : une {@link GaldurItemGrid} avec tous les objets du jeu
 *       (plus d'un millier), filtrée par un champ de recherche et triable par nom ou par
 *       quantité ;</li>
 *   <li><strong>Progression</strong> : barre animée qu'on fait avancer, barre sans texte, barre
 *       désactivée.</li>
 * </ul>
 *
 * <p>Aucune coordonnée n'est calculée ici : les layouts vanilla placent les widgets, le
 * {@link GaldurPanelLayout} les entoure, et l'ensemble est centré sur le rectangle de l'écran.
 * Tout l'état (onglet, clics, recherche, sélection, défilement, avancement) vit dans des champs
 * de l'écran : il survit aux changements d'onglet et aux redimensionnements, qui repassent par
 * {@link #init()}.</p>
 *
 * <p>Minecraft ignore F3+T tant qu'un écran est ouvert. Le bouton « Recharger le thème »
 * appelle donc {@code Minecraft.reloadResourcePacks()} lui-même : à la fin du rechargement,
 * l'écran de chargement rappelle {@code init(...)} sur ce même écran, qui se reconstruit avec
 * le nouveau thème.</p>
 */
public class TestScreen extends Screen {
    /** Largeur de la rangée d'onglets et de la zone de contenu, en pixels. */
    private static final int CONTENT_WIDTH = 230;
    /** Titres des onglets, dans l'ordre. */
    private static final List<Component> TABS = List.of(
            Component.literal("Boutons"), Component.literal("Texte"), Component.literal("Liste"),
            Component.literal("Objets"), Component.literal("Grille"), Component.literal("Progression"));
    private static final List<String> ORES = List.of("Charbon", "Fer", "Cuivre", "Or", "Redstone",
            "Lapis-lazuli", "Diamant", "Émeraude", "Quartz du Nether", "Débris antiques");

    // État de la démo, conservé d'un init() à l'autre.
    private int tab;
    private int clicks;
    private boolean checked = true;
    private String query = "";
    private int selectedOre = -1;
    private double oreScroll;
    private double progress = 0.25;
    private String gridQuery = "";
    private boolean gridByAmount;
    private int gridScroll;
    /** Tous les objets du jeu avec une quantité de démonstration, calculés à la première ouverture. */
    private List<GaldurItemGrid.Entry> gridAll;
    /** Les objets affichés : filtrés par {@link #gridQuery} et triés. */
    private List<GaldurItemGrid.Entry> gridShown = List.of();

    private GaldurPanelLayout panel;
    private GaldurLabel counter;
    private GaldurTabs tabs;
    private GaldurLabel gridCount;

    public TestScreen() {
        super(Component.literal("Galdur UI — écran de démo"));
    }

    @Override
    protected void init() {
        LinearLayout column = LinearLayout.vertical().spacing(GaldurTheme.gap());
        column.defaultCellSetting().alignHorizontallyCenter();

        column.addChild(GaldurLabel.of(getTitle()));
        tabs = column.addChild(GaldurTabs.of(CONTENT_WIDTH, TABS, () -> tab, this::selectTab));

        FrameLayout area = column.addChild(new FrameLayout(CONTENT_WIDTH, 0));
        area.defaultChildLayoutSetting().alignHorizontallyCenter().alignVerticallyTop();
        area.addChild(switch (tab) {
            case 0 -> buttonsTab();
            case 1 -> textTab();
            case 2 -> listTab();
            case 3 -> itemsTab();
            case 4 -> gridTab();
            default -> progressTab();
        });

        LinearLayout footer = column.addChild(LinearLayout.horizontal().spacing(GaldurTheme.gap()));
        footer.addChild(GaldurButton.of(Component.literal("Ouvrir une fenêtre Galdur"),
                button -> GaldurDevCommands.openDemoWindow()));
        footer.addChild(GaldurButton.of(Component.literal("Recharger le thème"),
                button -> Minecraft.getInstance().reloadResourcePacks()));

        panel = new GaldurPanelLayout(column);
        addRenderableOnly(panel);
        panel.visitWidgets(this::addRenderableWidget);
        layoutPanel();
    }

    /**
     * Change d'onglet. La reconstruction est différée à la boucle suivante : on est appelé depuis
     * un clic ou une touche, pendant que l'écran parcourt ses widgets. Le focus est rendu aux
     * onglets pour qu'on puisse continuer aux flèches.
     */
    private void selectTab(int index) {
        tab = index;
        Minecraft.getInstance().tell(() -> {
            if (Minecraft.getInstance().screen == this) {
                rebuildWidgets();
                setFocused(tabs);
            }
        });
    }

    private LinearLayout buttonsTab() {
        LinearLayout content = LinearLayout.vertical().spacing(GaldurTheme.gap());
        content.defaultCellSetting().alignHorizontallyCenter();

        counter = content.addChild(GaldurLabel.muted(counterText()));
        LinearLayout actions = content.addChild(LinearLayout.horizontal().spacing(GaldurTheme.gap()));
        actions.addChild(GaldurButton.of(Component.literal("Cliquer"), button -> onClick()));
        GaldurButton disabled = actions.addChild(GaldurButton.of(Component.literal("Indisponible"), button -> {
        }));
        disabled.active = false;

        LinearLayout sizes = content.addChild(LinearLayout.horizontal().spacing(GaldurTheme.gap()));
        sizes.addChild(GaldurButton.of(Component.literal("OK"), button -> {
        }));
        sizes.addChild(GaldurButton.of(Component.literal("Un libellé nettement plus long"), button -> {
        }));

        LinearLayout icons = content.addChild(LinearLayout.horizontal().spacing(GaldurTheme.gap()));
        icons.addChild(GaldurIconButton.ofItem(new ItemStack(Items.HOPPER), Component.literal("Trier"),
                button -> GaldurUI.LOGGER.info("Icône : trier")));
        icons.addChild(GaldurIconButton.ofItem(new ItemStack(Items.COMPASS), Component.literal("Localiser"),
                button -> GaldurUI.LOGGER.info("Icône : localiser")));
        GaldurIconButton disabledIcon = icons.addChild(GaldurIconButton.ofItem(new ItemStack(Items.CRAFTING_TABLE),
                Component.literal("Commander (désactivé)"), button -> {
                }));
        disabledIcon.active = false;

        content.addChild(GaldurCheckbox.of(Component.literal("Case active"), () -> checked, value -> checked = value));
        GaldurCheckbox disabledBox = content.addChild(GaldurCheckbox.of(Component.literal("Case désactivée"),
                () -> true, value -> {
                }));
        disabledBox.active = false;
        return content;
    }

    private LinearLayout textTab() {
        LinearLayout content = LinearLayout.vertical().spacing(GaldurTheme.gap());
        content.defaultCellSetting().alignHorizontallyCenter();
        content.addChild(GaldurLabel.of(Component.literal("Libellé normal")));
        content.addChild(GaldurLabel.muted(Component.literal("Libellé atténué")));
        content.addChild(GaldurLabel.of(Component.literal("Un libellé bien trop long pour sa place, tronqué"), 120));
        content.addChild(new ClipDemo());
        return content;
    }

    private LinearLayout listTab() {
        LinearLayout content = LinearLayout.vertical().spacing(GaldurTheme.gap());
        content.defaultCellSetting().alignHorizontallyCenter();
        content.addChild(GaldurSearchField.of(140, Component.literal("Rechercher un minerai..."),
                () -> query, text -> {
                    query = text;
                    selectedOre = -1;
                }));
        GaldurScrollList list = GaldurScrollList.of(140, 5, this::filteredOres,
                () -> selectedOre, index -> {
                    selectedOre = index;
                    GaldurUI.LOGGER.info("Minerai : {}", filteredOres().get(index).getString());
                });
        list.setScroll(oreScroll);
        content.addChild(list.onScroll(scroll -> oreScroll = scroll));

        LinearLayout disabled = content.addChild(LinearLayout.horizontal().spacing(GaldurTheme.gap()));
        GaldurSearchField disabledField = disabled.addChild(GaldurSearchField.of(80,
                Component.literal("Désactivé"), () -> "", text -> {
                }));
        disabledField.active = false;
        GaldurScrollList disabledList = disabled.addChild(GaldurScrollList.of(80, 2,
                () -> List.of(Component.literal("Liste"), Component.literal("désactivée")), () -> 0, index -> {
                }));
        disabledList.active = false;
        return content;
    }

    private LinearLayout itemsTab() {
        LinearLayout content = LinearLayout.vertical().spacing(GaldurTheme.gap());
        content.defaultCellSetting().alignHorizontallyCenter();
        content.addChild(GaldurLabel.muted(Component.literal("Survol : infobulle ; clic gauche ou droit : journal")));
        LinearLayout slots = content.addChild(LinearLayout.horizontal().spacing(2));
        slots.addChild(GaldurItemSlot.of(() -> new ItemStack(Items.DIRT)));
        slots.addChild(GaldurItemSlot.of(() -> new ItemStack(Items.COBBLESTONE, 64)));
        slots.addChild(slotWithAmount(Items.IRON_INGOT, 1_234));
        slots.addChild(slotWithAmount(Items.DIAMOND, 64_000));
        slots.addChild(slotWithAmount(Items.REDSTONE, 1_250_000));
        slots.addChild(slotWithAmount(Items.EMERALD, 9_876_543_210L));
        slots.addChild(GaldurItemSlot.of(TestScreen::damagedSword));
        slots.addChild(GaldurItemSlot.of(() -> ItemStack.EMPTY));
        GaldurItemSlot disabledSlot = slots.addChild(slotWithAmount(Items.GOLD_INGOT, 500));
        disabledSlot.active = false;
        return content;
    }

    private LinearLayout gridTab() {
        if (gridAll == null) {
            gridAll = demoGridEntries();
            refreshGrid();
        }
        LinearLayout content = LinearLayout.vertical().spacing(GaldurTheme.gap());
        content.defaultCellSetting().alignHorizontallyCenter();

        LinearLayout tools = content.addChild(LinearLayout.horizontal().spacing(GaldurTheme.gap()));
        tools.addChild(GaldurSearchField.of(140, Component.literal("Rechercher un objet..."), () -> gridQuery, text -> {
            gridQuery = text;
            refreshGrid();
        }));
        tools.addChild(GaldurIconButton.ofItem(new ItemStack(Items.HOPPER),
                Component.literal("Trier par nom ou par quantité"), button -> {
                    gridByAmount = !gridByAmount;
                    refreshGrid();
                }));

        GaldurItemGrid grid = GaldurItemGrid.of(9, 6, () -> gridShown)
                .onClick((index, entry, button, shift) -> GaldurUI.LOGGER.info("Grille : {} × {}, clic {}{}",
                        entry.stack().getHoverName().getString(), entry.amount(),
                        button == 0 ? "gauche" : "droit", shift ? " + Maj" : ""));
        grid.setScrollRow(gridScroll);
        content.addChild(grid.onScroll(row -> gridScroll = row));
        gridCount = content.addChild(GaldurLabel.muted(gridCountText()));
        return content;
    }

    /** Refiltre et retrie la grille après un changement de recherche ou de tri. */
    private void refreshGrid() {
        String wanted = gridQuery.trim().toLowerCase(Locale.ROOT);
        List<GaldurItemGrid.Entry> shown = new ArrayList<>();
        for (GaldurItemGrid.Entry entry : gridAll) {
            if (entry.stack().getHoverName().getString().toLowerCase(Locale.ROOT).contains(wanted)) {
                shown.add(entry);
            }
        }
        shown.sort(gridByAmount
                ? Comparator.comparingLong(GaldurItemGrid.Entry::amount).reversed()
                : Comparator.comparing(entry -> entry.stack().getHoverName().getString()));
        gridShown = shown;
        if (gridCount != null) {
            int previousWidth = gridCount.getWidth();
            gridCount.setText(gridCountText());
            if (gridCount.getWidth() != previousWidth) {
                layoutPanel();
            }
        }
    }

    private Component gridCountText() {
        return Component.literal(gridShown.size() + " objets, triés par " + (gridByAmount ? "quantité" : "nom"));
    }

    /**
     * {@return tous les objets du jeu (sauf l'air), chacun avec une quantité de démonstration
     * variée : de 0 à plusieurs milliards, pour voir toutes les abréviations}
     */
    private static List<GaldurItemGrid.Entry> demoGridEntries() {
        List<GaldurItemGrid.Entry> all = new ArrayList<>();
        int index = 0;
        for (Item item : BuiltInRegistries.ITEM) {
            if (item == Items.AIR) {
                continue;
            }
            long amount = (index % 13 == 0) ? 0 : (long) Math.pow(10, index % 10) * (1 + index % 7);
            all.add(new GaldurItemGrid.Entry(new ItemStack(item), amount));
            index++;
        }
        return all;
    }

    private LinearLayout progressTab() {
        LinearLayout content = LinearLayout.vertical().spacing(GaldurTheme.gap());
        content.defaultCellSetting().alignHorizontallyCenter();
        content.addChild(GaldurProgressBar.of(160, () -> progress,
                () -> Component.literal(Math.round(progress * 100) + " %")));
        content.addChild(GaldurProgressBar.of(160, () -> progress));
        GaldurProgressBar disabled = content.addChild(GaldurProgressBar.of(160, () -> progress,
                () -> Component.literal("Désactivée")));
        disabled.active = false;

        LinearLayout actions = content.addChild(LinearLayout.horizontal().spacing(GaldurTheme.gap()));
        actions.addChild(GaldurButton.of(Component.literal("+10 %"),
                button -> progress = Math.min(1, progress + 0.1)));
        actions.addChild(GaldurButton.of(Component.literal("Pleine"), button -> progress = 1));
        actions.addChild(GaldurButton.of(Component.literal("Vider"), button -> progress = 0));
        return content;
    }

    /** {@return les minerais qui contiennent la recherche, sans tenir compte des majuscules} */
    private List<Component> filteredOres() {
        String wanted = query.trim().toLowerCase(Locale.ROOT);
        return ORES.stream()
                .filter(ore -> ore.toLowerCase(Locale.ROOT).contains(wanted))
                .map(ore -> (Component) Component.literal(ore))
                .toList();
    }

    /** Recalcule la taille du panneau et le recentre dans l'écran. */
    private void layoutPanel() {
        panel.arrangeElements();
        FrameLayout.centerInRectangle(panel, getRectangle());
    }

    private void onClick() {
        clicks++;
        int previousWidth = counter.getWidth();
        counter.setText(counterText());
        // « Clics : 9 » → « Clics : 10 » élargit le libellé : on réaligne tout le panneau.
        if (counter.getWidth() != previousWidth) {
            layoutPanel();
        }
    }

    /**
     * {@return une case de démonstration avec une quantité fournie à part, qui écrit dans le
     * journal le bouton de souris utilisé}
     */
    private static GaldurItemSlot slotWithAmount(Item item, long amount) {
        return GaldurItemSlot.of(() -> new ItemStack(item), () -> amount)
                .onPress(button -> GaldurUI.LOGGER.info("Case {} : clic {}", item, button == 0 ? "gauche" : "droit"));
    }

    /** {@return une épée usée, pour vérifier que la barre d'usure s'affiche} */
    private static ItemStack damagedSword() {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.setDamageValue(sword.getMaxDamage() * 2 / 3);
        return sword;
    }

    private Component counterText() {
        return Component.literal("Clics : " + clicks);
    }

    @Override
    public boolean isPauseScreen() {
        // Écran de test : inutile de mettre une partie solo en pause pour le regarder.
        return false;
    }
}
