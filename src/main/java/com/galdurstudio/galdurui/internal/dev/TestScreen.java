package com.galdurstudio.galdurui.internal.dev;

import com.galdurstudio.galdurui.api.theme.GaldurTheme;
import com.galdurstudio.galdurui.api.widget.GaldurButton;
import com.galdurstudio.galdurui.api.widget.GaldurPanel;
import com.galdurstudio.galdurui.internal.render.GaldurRender;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Écran de démonstration des widgets de Galdur UI.
 *
 * <p>Outil de développement, ouvert par {@code /galdurui test} : il sert de banc d'essai
 * visuel pour le thème, le découpage 9-slice et les états des boutons. Il ne fait pas partie
 * de l'API et n'est pas enregistré dans une version publiée.</p>
 *
 * <p>Toutes les positions sont dérivées de {@code width} et {@code height}, recalculées par
 * {@link #init()} à chaque redimensionnement : l'écran reste centré quelle que soit l'échelle
 * d'interface choisie.</p>
 */
public class TestScreen extends Screen {
    private static final int PANEL_WIDTH = 220;
    private static final int PANEL_HEIGHT = 140;
    private static final int BUTTON_WIDTH = 120;
    private static final int BUTTON_HEIGHT = 20;

    /** Nombre de clics sur le bouton actif ; conservé d'un {@link #init()} à l'autre. */
    private int clicks;

    private GaldurPanel panel;
    private int titleY;
    private int counterY;

    public TestScreen() {
        super(Component.literal("Galdur UI — écran de test"));
    }

    @Override
    protected void init() {
        int panelX = (width - PANEL_WIDTH) / 2;
        int panelY = (height - PANEL_HEIGHT) / 2;
        panel = new GaldurPanel(panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT);
        // Ajouté en premier : les éléments suivants se dessineront par-dessus.
        addRenderableOnly(panel);

        int buttonX = panel.getCenterX() - BUTTON_WIDTH / 2;
        titleY = panelY + 16;

        addRenderableWidget(new GaldurButton(buttonX, panelY + 44, BUTTON_WIDTH, BUTTON_HEIGHT,
                Component.literal("Cliquer"), button -> clicks++));

        counterY = panelY + 76;

        GaldurButton disabled = new GaldurButton(buttonX, panelY + 98, BUTTON_WIDTH, BUTTON_HEIGHT,
                Component.literal("Indisponible"), button -> {});
        disabled.active = false;
        addRenderableWidget(disabled);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Dessine le fond d'écran, le panneau puis les boutons.
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        GaldurRender.drawCenteredText(guiGraphics, getTitle(), panel.getCenterX(), titleY, GaldurTheme.text());
        GaldurRender.drawCenteredText(guiGraphics, Component.literal("Clics : " + clicks),
                panel.getCenterX(), counterY, GaldurTheme.textMuted());
    }

    @Override
    public boolean isPauseScreen() {
        // Écran de test : inutile de mettre une partie solo en pause pour le regarder.
        return false;
    }
}
