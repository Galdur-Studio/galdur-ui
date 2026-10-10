package com.galdurstudio.galdurui.internal.dev;

import com.galdurstudio.galdurui.GaldurUI;
import com.galdurstudio.galdurui.api.layout.GaldurPanelLayout;
import com.galdurstudio.galdurui.api.theme.GaldurTheme;
import com.galdurstudio.galdurui.api.widget.GaldurButton;
import com.galdurstudio.galdurui.api.widget.GaldurItemSlot;
import com.galdurstudio.galdurui.api.widget.GaldurLabel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Écran de démonstration des widgets de Galdur UI.
 *
 * <p>Outil de développement, ouvert par {@code /galdurui test} : il sert de banc d'essai
 * visuel pour le thème, le découpage 9-slice et les états des boutons. Il ne fait pas partie
 * de l'API et n'est pas enregistré dans une version publiée.</p>
 *
 * <p>Aucune coordonnée n'est calculée ici : les layouts vanilla placent les widgets, le
 * {@link GaldurPanelLayout} les entoure, et l'ensemble est centré sur le rectangle de l'écran.
 * Un redimensionnement de la fenêtre repasse par {@link #init()} via
 * {@code repositionElements()}, qui relit le thème ; le nombre de clics est conservé.</p>
 *
 * <p>Minecraft ignore F3+T tant qu'un écran est ouvert. Le bouton « Recharger le thème »
 * appelle donc {@code Minecraft.reloadResourcePacks()} lui-même : à la fin du rechargement,
 * l'écran de chargement rappelle {@code init(...)} sur ce même écran, qui se reconstruit avec
 * le nouveau thème. Changer l'échelle d'interface se fait depuis les options, donc écran de
 * test fermé : le rouvrir crée un nouvel écran, compteur remis à zéro.</p>
 */
public class TestScreen extends Screen {
    /** Nombre de clics sur le bouton actif ; conservé d'un {@link #init()} à l'autre. */
    private int clicks;

    private GaldurPanelLayout panel;
    private GaldurLabel counter;

    public TestScreen() {
        super(Component.literal("Galdur UI — écran de test"));
    }

    @Override
    protected void init() {
        LinearLayout column = LinearLayout.vertical().spacing(GaldurTheme.gap());
        column.defaultCellSetting().alignHorizontallyCenter();

        column.addChild(GaldurLabel.of(getTitle()));
        counter = column.addChild(GaldurLabel.muted(counterText()));

        LinearLayout actions = column.addChild(LinearLayout.horizontal().spacing(GaldurTheme.gap()));
        actions.addChild(GaldurButton.of(Component.literal("Cliquer"), button -> onClick()));
        GaldurButton disabled = actions.addChild(GaldurButton.of(Component.literal("Indisponible"), button -> {
        }));
        disabled.active = false;

        LinearLayout demo = column.addChild(LinearLayout.horizontal().spacing(GaldurTheme.gap()));
        demo.addChild(GaldurButton.of(Component.literal("OK"), button -> {
        }));
        demo.addChild(GaldurButton.of(Component.literal("Annuler"), button -> {
        }));
        demo.addChild(GaldurButton.of(Component.literal("Un libellé nettement plus long"), button -> {
        }));

        column.addChild(new ClipDemo());

        LinearLayout slots = column.addChild(LinearLayout.horizontal().spacing(2));
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

        column.addChild(GaldurButton.of(Component.literal("Ouvrir une fenêtre Galdur"),
                button -> GaldurDevCommands.openDemoWindow()));

        column.addChild(GaldurButton.of(Component.literal("Recharger le thème"),
                button -> Minecraft.getInstance().reloadResourcePacks()));

        panel = new GaldurPanelLayout(column);
        addRenderableOnly(panel);
        panel.visitWidgets(this::addRenderableWidget);
        layoutPanel();
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
