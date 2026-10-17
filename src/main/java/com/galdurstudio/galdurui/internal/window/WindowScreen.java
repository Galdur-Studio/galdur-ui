package com.galdurstudio.galdurui.internal.window;

import java.util.Set;
import java.util.Map;
import java.util.IdentityHashMap;
import java.util.Collections;
import net.minecraft.client.gui.GuiGraphics;
import com.galdurstudio.galdurui.internal.render.GaldurRender;
import com.galdurstudio.galdurui.api.anim.GaldurEasing;
import com.galdurstudio.galdurui.api.anim.GaldurAnimations;
import com.galdurstudio.galdurui.internal.narration.GaldurNarration;
import com.galdurstudio.galdurui.api.layout.GaldurPanelLayout;
import com.galdurstudio.galdurui.api.theme.GaldurTheme;
import com.galdurstudio.galdurui.api.widget.GaldurTabs;
import com.galdurstudio.galdurui.api.window.ElementHandle;
import com.galdurstudio.galdurui.api.window.TabsHandle;
import java.util.List;
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

    /** Distance parcourue par la fenêtre en s'ouvrant (elle monte), en pixels. */
    private static final int OPEN_RISE = 8;
    /** Distance parcourue par le contenu d'un onglet en arrivant, en pixels. */
    private static final int PAGE_SLIDE = 12;

    /** Début de l'animation d'ouverture ; {@code -1} tant que la fenêtre n'a jamais été montrée. */
    private long openStart = -1;
    /** Onglet affiché par chaque rangée d'onglets à la dernière construction. */
    private final Map<TabsHandle, Integer> builtTabs = new IdentityHashMap<>();
    /** Contenu de l'onglet qui vient d'être choisi, trouvé pendant la construction ; sinon {@code null}. */
    @Nullable
    private LinearLayout arrivingPage;
    /** Widgets de l'onglet qui arrive, dessinés décalés pendant {@link GaldurAnimations#PAGE_MS}. */
    private final Set<AbstractWidget> slidingWidgets = Collections.newSetFromMap(new IdentityHashMap<>());
    /** Sens d'arrivée : {@code 1} depuis la droite (onglet suivant), {@code -1} depuis la gauche. */
    private int pageDirection;
    /** Début du glissement de l'onglet qui arrive. */
    private long pageStart;

    /**
     * @param model    le contenu à afficher
     * @param onShown  appelé chaque fois que l'écran devient l'écran courant (à l'ouverture, ou au
     *                 retour depuis un écran ouvert par-dessus)
     * @param onHidden appelé chaque fois que l'écran cesse d'être l'écran courant (Échap, bouton de
     *                 fermeture, autre écran qui le remplace). Ces deux rappels permettent de
     *                 prévenir la fenêtre sans que ce package dépende de {@code api}.
     */
    public WindowScreen(WindowModel model, Consumer<WindowScreen> onShown, Consumer<WindowScreen> onHidden) {
        super(GaldurNarration.ours("window"));
        this.model = model;
        this.onShown = onShown;
        this.onHidden = onHidden;
    }

    /**
     * {@return ce que le narrateur lit à l'ouverture : le premier titre de la fenêtre
     * ({@code addTitle}), ou « Fenêtre Galdur » s'il n'y en a pas}
     */
    @Override
    public Component getNarrationMessage() {
        for (WindowElement element : model.elements()) {
            if (element instanceof TitleElement title && title.handle().isVisible()) {
                return title.title();
            }
        }
        return getTitle();
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
     *
     * <p>Le focus est rendu au widget de même rang : un joueur qui navigue au clavier dans une
     * liste ne le perd pas quand sa sélection change l'état d'un autre élément. Si un élément
     * apparaît ou disparaît avant lui, le focus peut glisser d'un widget.</p>
     */
    public void rebuild() {
        int focusedIndex = children().indexOf(getFocused());
        rebuildWidgets();
        if (focusedIndex >= 0 && focusedIndex < children().size()) {
            setFocused(children().get(focusedIndex));
        }
    }

    /** Appelée par Minecraft quand l'écran devient l'écran courant. */
    @Override
    public void added() {
        super.added();
        if (openStart < 0) {
            openStart = GaldurAnimations.now();
        }
        onShown.accept(this);
    }

    /** {@return le décalage vertical de la fenêtre pendant son ouverture, en pixels} */
    private float openOffset() {
        if (openStart < 0) {
            return 0;
        }
        double t = GaldurAnimations.progress(openStart, GaldurAnimations.OPEN_MS);
        return (float) (OPEN_RISE * (1 - GaldurEasing.OUT_CUBIC.applyAsDouble(t)));
    }

    /** {@return le décalage horizontal du contenu de l'onglet qui arrive, en pixels} */
    private float pageOffset() {
        double t = GaldurAnimations.progress(pageStart, GaldurAnimations.PAGE_MS);
        return (float) (pageDirection * PAGE_SLIDE * (1 - GaldurEasing.OUT_CUBIC.applyAsDouble(t)));
    }

    /**
     * Dessine l'écran : la fenêtre monte légèrement à l'ouverture. Les zones de clic ne bougent
     * pas ; l'animation est trop courte pour que ça se remarque.
     */
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        float offset = openOffset();
        GaldurRender.drawOffset(guiGraphics, 0, offset,
                () -> super.render(guiGraphics, mouseX, mouseY - Math.round(offset), partialTick));
    }

    /** Le fond (flou, voile) ne suit pas la montée de la fenêtre : il couvre tout l'écran. */
    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        float offset = openOffset();
        GaldurRender.drawOffset(guiGraphics, 0, -offset,
                () -> super.renderBackground(guiGraphics, mouseX, mouseY, partialTick));
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
    private LayoutElement build(WindowElement element, boolean parentEnabled, @Nullable Component parentTooltip) {
        ElementHandle handle = element.handle();
        if (!handle.isVisible()) {
            return null;
        }
        boolean enabled = parentEnabled && handle.isEnabled();
        Component tooltip = handle.getTooltip() != null ? handle.getTooltip() : parentTooltip;
        if (element instanceof RowElement row) {
            return buildRow(row, enabled, tooltip);
        }
        if (element instanceof TabsElement tabs) {
            return buildTabs(tabs, enabled, tooltip);
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
    private LayoutElement buildRow(RowElement row, boolean enabled, @Nullable Component tooltip) {
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
     * Construit une rangée d'onglets : les onglets, puis dessous le contenu de l'onglet actif.
     *
     * <p>Le contenu de chaque onglet est construit puis mesuré, et la zone de contenu prend la
     * taille du plus grand : la fenêtre ne change pas de taille quand on change d'onglet. Seuls
     * les widgets de l'onglet actif sont placés dans la mise en page, donc ajoutés à l'écran ; les
     * autres ne servent qu'à mesurer et sont abandonnés.</p>
     *
     * <p>L'infobulle de la rangée ne s'applique qu'aux onglets, pas au contenu ; l'état
     * désactivé, lui, s'applique aux deux.</p>
     *
     * @return la rangée et son contenu, ou {@code null} s'il n'y a encore aucun onglet
     */
    @Nullable
    private LayoutElement buildTabs(TabsElement element, boolean enabled, @Nullable Component tooltip) {
        List<Component> titles = element.tabs().titles();
        List<WindowModel> contents = element.tabs().contents();
        if (titles.isEmpty()) {
            return null;
        }
        TabsHandle handle = element.handle();
        int selected = handle.getSelected();
        Integer before = builtTabs.put(handle, selected);
        LinearLayout shown = null;
        int contentWidth = 0;
        int contentHeight = 0;
        for (int index = 0; index < contents.size(); index++) {
            LinearLayout content = buildColumn(contents.get(index), enabled);
            content.arrangeElements();
            contentWidth = Math.max(contentWidth, content.getWidth());
            contentHeight = Math.max(contentHeight, content.getHeight());
            if (index == selected) {
                shown = content;
            }
        }

        GaldurTabs bar = GaldurTabs.of(contentWidth, titles, handle::getSelected, handle::setSelected);
        applyState(bar, enabled, tooltip);
        FrameLayout area = new FrameLayout(bar.getWidth(), contentHeight);
        area.defaultChildLayoutSetting().alignHorizontallyLeft().alignVerticallyTop();
        if (shown != null) {
            area.addChild(shown);
            if (before != null && before != selected) {
                // Le joueur vient de changer d'onglet : le nouveau contenu glisse depuis son côté.
                arrivingPage = shown;
                pageDirection = selected > before ? 1 : -1;
                pageStart = GaldurAnimations.now();
            }
        }

        LinearLayout column = LinearLayout.vertical().spacing(GaldurTheme.gap());
        column.addChild(bar);
        column.addChild(area);
        return column;
    }

    /**
     * Construit des éléments empilés de haut en bas, comme le contenu de la fenêtre.
     *
     * @param model   les éléments, dans l'ordre
     * @param enabled {@code false} si ce qui les contient est désactivé
     * @return la colonne, vide si aucun élément n'est visible
     */
    private LinearLayout buildColumn(WindowModel model, boolean enabled) {
        LinearLayout column = LinearLayout.vertical().spacing(GaldurTheme.gap());
        for (WindowElement element : model.elements()) {
            LayoutElement widget = build(element, enabled, null);
            if (widget != null) {
                column.addChild(widget, element::adjustSettings);
            }
        }
        return column;
    }

    /**
     * Applique au widget l'état désactivé et l'infobulle.
     *
     * <p>Les libellés sont toujours inactifs ({@code active = false}, comme {@code StringWidget}) :
     * leur état désactivé se voit à la couleur, choisie par l'élément lui-même. L'infobulle
     * s'affiche au survol, que le widget soit actif ou non. Sans infobulle donnée par le mod, le
     * widget garde la sienne (un bouton icône affiche son libellé).</p>
     */
    private static void applyState(AbstractWidget widget, boolean enabled, @Nullable Component tooltip) {
        if (!enabled) {
            widget.active = false;
        }
        if (tooltip != null) {
            widget.setTooltip(Tooltip.create(tooltip));
        }
    }

    @Override
    protected void init() {
        arrivingPage = null;
        LinearLayout layout = buildColumn(model, true);
        GaldurPanelLayout panel = new GaldurPanelLayout(layout);
        addRenderableOnly(panel);
        panel.arrangeElements();
        FrameLayout.centerInRectangle(panel, getRectangle());

        slidingWidgets.clear();
        if (arrivingPage != null) {
            arrivingPage.visitWidgets(slidingWidgets::add);
        }
        layout.visitWidgets(widget -> {
            if (!slidingWidgets.contains(widget)) {
                addRenderableWidget(widget);
                return;
            }
            // Widget de l'onglet qui arrive : il reçoit clics et clavier normalement, mais il
            // est dessiné décalé tant que le glissement dure.
            addWidget(widget);
            addRenderableOnly((guiGraphics, mouseX, mouseY, partialTick) -> {
                float offset = pageOffset();
                GaldurRender.drawOffset(guiGraphics, offset, 0,
                        () -> widget.render(guiGraphics, mouseX - Math.round(offset), mouseY, partialTick));
            });
        });
    }
}
