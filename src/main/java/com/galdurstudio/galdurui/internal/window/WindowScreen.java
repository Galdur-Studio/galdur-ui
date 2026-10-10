package com.galdurstudio.galdurui.internal.window;

import com.galdurstudio.galdurui.api.layout.GaldurPanelLayout;
import com.galdurstudio.galdurui.api.theme.GaldurTheme;
import com.galdurstudio.galdurui.api.window.ElementHandle;
import java.util.function.Consumer;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LayoutSettings;
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
     * Construit le widget d'un élément avec l'état hérité de ce qui le contient.
     *
     * <p>Un élément est actif seulement si lui et sa ligne le sont ; sans infobulle à lui, il
     * prend celle de sa ligne.</p>
     *
     * @param element       l'élément à construire
     * @param parentEnabled {@code false} si ce qui contient l'élément est désactivé
     * @param parentTooltip l'infobulle de ce qui contient l'élément, ou {@code null}
     * @return le widget, ou {@code null} si l'élément est caché et ne prend donc aucune place
     */
    @Nullable
    private static LayoutElement build(WindowElement element, boolean parentEnabled, @Nullable Component parentTooltip) {
        ElementHandle handle = element.handle();
        if (!handle.isVisible()) {
            return null;
        }
        boolean enabled = parentEnabled && handle.isEnabled();
        Component tooltip = handle.getTooltip() != null ? handle.getTooltip() : parentTooltip;
        if (element instanceof RowElement row) {
            return buildRow(row, enabled, tooltip);
        }
        LayoutElement widget = element.createWidget(enabled);
        if (widget instanceof AbstractWidget abstractWidget) {
            applyState(abstractWidget, enabled, tooltip);
        }
        return widget;
    }

    /**
     * Construit une ligne : ses éléments visibles côte à côte, centrés verticalement entre eux.
     *
     * @return la ligne, ou {@code null} si aucun de ses éléments n'est visible
     */
    @Nullable
    private static LayoutElement buildRow(RowElement row, boolean enabled, @Nullable Component tooltip) {
        LinearLayout line = LinearLayout.horizontal().spacing(GaldurTheme.gap());
        boolean empty = true;
        for (WindowElement child : row.children().elements()) {
            LayoutElement widget = build(child, enabled, tooltip);
            if (widget != null) {
                line.addChild(widget, LayoutSettings::alignVerticallyMiddle);
                empty = false;
            }
        }
        return empty ? null : line;
    }

    /**
     * Applique au widget l'état désactivé et l'infobulle.
     *
     * <p>Les libellés sont toujours inactifs ({@code active = false}, comme {@code StringWidget}) :
     * leur état désactivé se voit à la couleur, choisie par l'élément lui-même. L'infobulle
     * s'affiche au survol, que le widget soit actif ou non.</p>
     */
    private static void applyState(AbstractWidget widget, boolean enabled, @Nullable Component tooltip) {
        if (!enabled) {
            widget.active = false;
        }
        widget.setTooltip(tooltip == null ? null : Tooltip.create(tooltip));
    }

    @Override
    protected void init() {
        LinearLayout layout = LinearLayout.vertical();
        layout.spacing(GaldurTheme.gap());
        GaldurPanelLayout panel = new GaldurPanelLayout(layout);
        addRenderableOnly(panel);
        for (WindowElement element : model.elements()) {
            LayoutElement widget = build(element, true, null);
            if (widget != null) {
                layout.addChild(widget, element::adjustSettings);
            }
        }
        panel.arrangeElements();
        FrameLayout.centerInRectangle(panel, getRectangle());
        layout.visitWidgets(this::addRenderableWidget);
    }
}
