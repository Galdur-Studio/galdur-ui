package com.galdurstudio.galdurui.internal.dev;

import com.galdurstudio.galdurui.api.layout.GaldurPanelLayout;
import com.galdurstudio.galdurui.api.theme.GaldurTheme;
import com.galdurstudio.galdurui.api.widget.GaldurButton;
import com.galdurstudio.galdurui.api.widget.GaldurLabel;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Écran de démonstration des widgets de Galdur UI.
 *
 * <p>Outil de développement, ouvert par {@code /galdurui test} : il sert de banc d'essai
 * visuel pour le thème, le découpage 9-slice et les états des boutons. Il ne fait pas partie
 * de l'API et n'est pas enregistré dans une version publiée.</p>
 *
 * <p>Aucune coordonnée n'est calculée ici : les layouts vanilla placent les widgets, le
 * {@link GaldurPanelLayout} les entoure, et l'ensemble est centré sur le rectangle de l'écran.
 * Un redimensionnement, un changement d'échelle d'interface ou un rechargement des ressources
 * (F3+T) repasse par {@link #init()} via {@code repositionElements()}, qui relit le thème.</p>
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

        // Libellés de longueurs différentes : la largeur de chaque bouton suit son texte.
        LinearLayout demo = column.addChild(LinearLayout.horizontal().spacing(GaldurTheme.gap()));
        demo.addChild(GaldurButton.of(Component.literal("OK"), button -> {
        }));
        demo.addChild(GaldurButton.of(Component.literal("Annuler"), button -> {
        }));
        demo.addChild(GaldurButton.of(Component.literal("Un libellé nettement plus long"), button -> {
        }));

        panel = new GaldurPanelLayout(column);
        // Ajouté en premier : les widgets, ajoutés ensuite, se dessinent par-dessus le fond.
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

    private Component counterText() {
        return Component.literal("Clics : " + clicks);
    }

    @Override
    public boolean isPauseScreen() {
        // Écran de test : inutile de mettre une partie solo en pause pour le regarder.
        return false;
    }
}
