package com.galdurstudio.galdurui.internal.dev;

import com.galdurstudio.galdurui.api.theme.GaldurTheme;
import com.galdurstudio.galdurui.api.widget.GaldurButton;
import com.galdurstudio.galdurui.api.widget.GaldurPanel;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

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
    private static final int BUTTON_WIDTH = 120;
    private static final int BUTTON_HEIGHT = 20;
    private static final int PADDING = 16;

    /**
     * Nombre de clics sur le bouton actif ; conservé d'un {@link #init()} à l'autre.
     */
    private int clicks;

    public TestScreen() {
        super(Component.literal("Galdur UI — écran de test"));
    }

    @Override
    protected void init() {
        LinearLayout layout = LinearLayout.vertical().spacing(8);
        LayoutSettings layoutSettings = layout.defaultCellSetting();
        GaldurPanel panel = new GaldurPanel(0, 0, 0, 0);
        StringWidget title = new StringWidget(getTitle(), font).setColor(GaldurTheme.text());
        // Ajouté en premier : les éléments suivants se dessineront par-dessus.
        addRenderableOnly(panel);
        layoutSettings.alignHorizontallyCenter();
        layout.addChild(title, settings -> settings.padding(PADDING));
        layout.addChild(new GaldurButton(0, 0, BUTTON_WIDTH, BUTTON_HEIGHT,
                Component.literal("Cliquer"), button -> clicks++));

        layout.addChild(new GaldurButton(0, 0, BUTTON_WIDTH, BUTTON_HEIGHT, Component.literal("Bouton de test"), button -> {
        }));

        layout.addChild(new GaldurButton(0, 0, BUTTON_WIDTH, BUTTON_HEIGHT, Component.literal("Bouton de test2"), button -> {
        }));


        GaldurButton disabled = new GaldurButton(0, 0, BUTTON_WIDTH, BUTTON_HEIGHT,
                Component.literal("Indisponible"), button -> {
        });
        disabled.active = false;
        layout.addChild(disabled);
        layout.arrangeElements();
        int panelW = layout.getWidth() + 2 * PADDING;
        int panelH = layout.getHeight() + 2 * PADDING;
        int panelX = (width - panelW) / 2;
        int panelY = (height - panelH) / 2;
        panel.setBounds(panelX, panelY, panelW, panelH);
        layout.setX(panelX + PADDING);
        layout.setY(panelY + PADDING);
        layout.visitWidgets(this::addRenderableWidget);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Dessine le fond d'écran, le panneau puis les boutons.
        super.render(guiGraphics, mouseX, mouseY, partialTick);

/*        GaldurRender.drawCenteredText(guiGraphics, getTitle(), panel.getCenterX(), titleY, GaldurTheme.text());
        GaldurRender.drawCenteredText(guiGraphics, Component.literal("Clics : " + clicks),
                panel.getCenterX(), counterY, GaldurTheme.textMuted());*/
    }

    @Override
    public boolean isPauseScreen() {
        // Écran de test : inutile de mettre une partie solo en pause pour le regarder.
        return false;
    }
}
