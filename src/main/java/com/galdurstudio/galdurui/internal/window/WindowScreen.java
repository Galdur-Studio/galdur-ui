package com.galdurstudio.galdurui.internal.window;

import com.galdurstudio.galdurui.api.layout.GaldurPanelLayout;
import com.galdurstudio.galdurui.api.theme.GaldurTheme;
import com.galdurstudio.galdurui.api.window.ElementHandle;
import java.util.function.Consumer;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

/**
 * Écran Minecraft qui affiche le contenu d'un {@link WindowModel} dans un panneau centré.
 *
 * <p>Minecraft rappelle {@link #init()} à chaque redimensionnement : la mise en page et les widgets
 * sont donc recréés à chaque fois à partir des descriptions du modèle.</p>
 *
 * <p>L'écran retient celui qui était affiché avant lui : Échap comme le bouton de fermeture y
 * reviennent, au lieu de fermer toute l'interface.</p>
 */
public class WindowScreen extends Screen {
    private final WindowModel model;
    private final Consumer<WindowScreen> onShown;
    private final Consumer<WindowScreen> onHidden;

    /** Écran affiché avant celui-ci, rétabli à la fermeture ; {@code null} = retour au jeu. */
    @Nullable
    private Screen previous;

    /**
     * @param model    le contenu à afficher
     * @param onShown  appelé chaque fois que l'écran devient l'écran courant (à l'ouverture, ou au
     *                 retour depuis un écran ouvert par-dessus)
     * @param onHidden appelé chaque fois que l'écran cesse d'être l'écran courant (Échap, bouton de
     *                 fermeture, autre écran qui le remplace). Ces deux rappels permettent de
     *                 prévenir la fenêtre sans que ce package dépende de {@code api}.
     */
    public WindowScreen(WindowModel model, Consumer<WindowScreen> onShown, Consumer<WindowScreen> onHidden) {
        super(Component.literal("Galdur"));
        this.model = model;
        this.onShown = onShown;
        this.onHidden = onHidden;
    }

    /**
     * Retient l'écran auquel revenir à la fermeture. À appeler juste avant de l'afficher.
     *
     * @param previous l'écran affiché jusqu'ici, ou {@code null} si aucun (en jeu)
     */
    public void setPrevious(@Nullable Screen previous) {
        this.previous = previous;
    }

    /**
     * Reconstruit les widgets à partir du modèle, par exemple après l'ajout d'un élément alors que
     * l'écran est déjà affiché. À appeler sur le thread du client.
     */
    public void rebuild() {
        rebuildWidgets();
    }

    /** Appelée par Minecraft quand l'écran devient l'écran courant. */
    @Override
    public void added() {
        super.added();
        onShown.accept(this);
    }

    /**
     * Prévient la fenêtre que l'écran n'est plus affiché.
     *
     * <p>Minecraft appelle cette méthode à chaque changement d'écran, mais pas lors d'un simple
     * redimensionnement (qui ne fait que rappeler {@link #init()}).</p>
     */
    @Override
    public void removed() {
        super.removed();
        onHidden.accept(this);
    }

    /**
     * Ferme l'écran en revenant au précédent. Appelée par Échap et par
     * {@code GaldurWindow.close()} : les deux se comportent donc de la même façon.
     */
    @Override
    public void onClose() {
        if (minecraft != null) {
            minecraft.setScreen(previous);
        }
    }

    /**
     * Applique au widget l'état désactivé et l'infobulle de la poignée.
     *
     * <p>Les libellés sont toujours inactifs ({@code active = false}, comme {@code StringWidget}) :
     * leur état désactivé se voit à la couleur, choisie par l'élément lui-même. L'infobulle
     * s'affiche au survol, que le widget soit actif ou non.</p>
     */
    private static void applyState(AbstractWidget widget, ElementHandle handle) {
        if (!handle.isEnabled()) {
            widget.active = false;
        }
        Component tooltip = handle.getTooltip();
        widget.setTooltip(tooltip == null ? null : Tooltip.create(tooltip));
    }

    @Override
    protected void init() {
        LinearLayout layout = LinearLayout.vertical();
        layout.spacing(GaldurTheme.gap());
        GaldurPanelLayout panel = new GaldurPanelLayout(layout);
        addRenderableOnly(panel);
        for (WindowElement element : model.elements()) {
            ElementHandle handle = element.handle();
            if (!handle.isVisible()) {
                continue;
            }
            LayoutElement widget = element.createWidget();
            if (widget instanceof AbstractWidget abstractWidget) {
                applyState(abstractWidget, handle);
            }
            layout.addChild(widget, element::adjustSettings);
        }
        panel.arrangeElements();
        FrameLayout.centerInRectangle(panel, getRectangle());
        layout.visitWidgets(this::addRenderableWidget);
    }
}
