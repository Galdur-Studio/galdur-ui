package com.galdurstudio.galdurui.api.screen;

import com.galdurstudio.galdurui.api.theme.GaldurTheme;
import com.galdurstudio.galdurui.internal.render.GaldurRender;
import com.galdurstudio.galdurui.internal.render.GaldurSprites;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

/**
 * Écran d'un menu à slots aux couleurs de Galdur UI : panneau du thème, cadre de chaque slot,
 * titres en couleurs du thème. Le glisser-déposer, le Maj+clic, les touches 1 à 9 et les
 * infobulles d'objets sont ceux de Minecraft ({@link AbstractContainerScreen}).
 *
 * <p>Utilisable tel quel pour n'importe quel menu, y compris un menu vanilla, grâce à
 * {@link #factory()} :</p>
 *
 * <pre>{@code
 * // côté client, sur le bus du mod
 * event.register(MES_MENUS.COFFRE.get(), GaldurContainerScreen.factory());
 * }</pre>
 *
 * <p>La taille du panneau est déduite des slots : la marge à gauche du premier slot est reprise
 * à droite et en bas. Un menu sans slot prend la taille d'un coffre vanilla (176 × 166). Une
 * sous-classe peut imposer sa taille avec
 * {@link #GaldurContainerScreen(AbstractContainerMenu, Inventory, Component, int, int)}, et
 * redéfinir {@link #renderBg} ou {@link #renderLabels} pour ajouter son contenu.</p>
 *
 * <p>Le titre « Inventaire » est placé juste au-dessus de l'inventaire du joueur, s'il y en a
 * un dans le menu, et masqué sinon.</p>
 *
 * <p>Aux couleurs du thème :</p>
 * <ul>
 *   <li>le slot survolé reçoit un voile {@code accent} par-dessus l'objet (au lieu du blanc
 *       vanilla) et un contour {@code accent_hover} ;</li>
 *   <li>pendant un glisser pour répartir (clic maintenu sur plusieurs slots), chaque slot visé
 *       reçoit le même contour. Le léger voile blanc posé sous l'objet reste celui de
 *       Minecraft.</li>
 * </ul>
 *
 * @param <M> le type du menu affiché
 */
public class GaldurContainerScreen<M extends AbstractContainerMenu> extends AbstractContainerScreen<M> {
    /** Taille d'un panneau sans slot, celle d'un coffre vanilla. */
    private static final int DEFAULT_WIDTH = 176;
    private static final int DEFAULT_HEIGHT = 166;
    /** Côté du cadre d'un slot : l'objet (16) et un pixel de chaque côté. */
    private static final int SLOT_FRAME = 18;
    /** Côté de l'objet dans un slot. */
    private static final int ITEM_SIZE = 16;
    /** Écart entre le haut du titre « Inventaire » et la première rangée de l'inventaire. */
    private static final int INVENTORY_LABEL_GAP = 12;
    /** Ordonnée du titre de l'écran dans le panneau. */
    private static final int TITLE_Y = 6;
    /** Opacité du voile posé sur le slot survolé (0 à 255), comme une case d'objet Galdur. */
    private static final int HOVER_ALPHA = 0x60;

    /** Vrai si le menu contient l'inventaire du joueur : le titre « Inventaire » est affiché. */
    private final boolean showInventoryLabel;

    /**
     * Crée l'écran, avec un panneau dimensionné d'après les slots du menu.
     *
     * @param menu      le menu affiché
     * @param inventory l'inventaire du joueur
     * @param title     le titre de l'écran
     */
    public GaldurContainerScreen(M menu, Inventory inventory, Component title) {
        this(menu, inventory, title, -1, -1);
    }

    /**
     * Crée l'écran avec une taille de panneau imposée.
     *
     * @param menu      le menu affiché
     * @param inventory l'inventaire du joueur
     * @param title     le titre de l'écran
     * @param width     largeur du panneau, en pixels ; négative pour la déduire des slots
     * @param height    hauteur du panneau, en pixels ; négative pour la déduire des slots
     */
    public GaldurContainerScreen(M menu, Inventory inventory, Component title, int width, int height) {
        super(menu, inventory, title);
        int margin = Integer.MAX_VALUE;
        int right = 0;
        int bottom = 0;
        Slot firstInventoryRow = null;
        for (Slot slot : menu.slots) {
            margin = Math.min(margin, slot.x);
            right = Math.max(right, slot.x + ITEM_SIZE);
            bottom = Math.max(bottom, slot.y + ITEM_SIZE);
            if (slot.container instanceof Inventory && slot.getContainerSlot() == 9 && firstInventoryRow == null) {
                firstInventoryRow = slot;
            }
        }
        boolean hasSlots = margin != Integer.MAX_VALUE;
        this.imageWidth = width >= 0 ? width : hasSlots ? right + margin : DEFAULT_WIDTH;
        this.imageHeight = height >= 0 ? height : hasSlots ? bottom + margin : DEFAULT_HEIGHT;
        this.titleLabelX = hasSlots ? margin : 8;
        this.titleLabelY = TITLE_Y;
        this.showInventoryLabel = firstInventoryRow != null;
        if (firstInventoryRow != null) {
            this.inventoryLabelX = firstInventoryRow.x;
            this.inventoryLabelY = firstInventoryRow.y - INVENTORY_LABEL_GAP;
        }
    }

    /**
     * {@return de quoi enregistrer cet écran pour un type de menu, sans sous-classe}
     *
     * <p>À passer à {@code RegisterMenuScreensEvent.register} : une référence de constructeur
     * ({@code GaldurContainerScreen::new}) ne suffit pas, Java ne sachant pas en déduire le type
     * de l'écran.</p>
     *
     * @param <M> le type du menu
     */
    public static <M extends AbstractContainerMenu> MenuScreens.ScreenConstructor<M, GaldurContainerScreen<M>> factory() {
        return GaldurContainerScreen::new;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    /**
     * Dessine le panneau (sprite {@code panel} teinté {@code background}) puis le cadre de chaque
     * slot actif (sprite {@code slot}), entouré en {@code accent_hover} s'il est survolé ou visé par
     * un glisser. Les objets sont dessinés ensuite par Minecraft.
     */
    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        GaldurRender.drawNineSlice(guiGraphics, GaldurSprites.PANEL, leftPos, topPos, imageWidth, imageHeight,
                GaldurTheme.background());
        boolean spreading = isQuickCrafting && quickCraftSlots.size() > 1;
        for (Slot slot : menu.slots) {
            if (!slot.isActive()) {
                continue;
            }
            int frameX = leftPos + slot.x - 1;
            int frameY = topPos + slot.y - 1;
            GaldurRender.drawNineSlice(guiGraphics, GaldurSprites.SLOT, frameX, frameY, SLOT_FRAME, SLOT_FRAME,
                    GaldurTheme.background());
            boolean hovered = slot.isHighlightable() && isHovering(slot.x, slot.y, ITEM_SIZE, ITEM_SIZE, mouseX, mouseY);
            if (hovered || (spreading && quickCraftSlots.contains(slot))) {
                GaldurRender.drawOutline(guiGraphics, frameX, frameY, SLOT_FRAME, SLOT_FRAME, GaldurTheme.accentHover());
            }
        }
    }

    /**
     * {@return la couleur du voile posé sur le slot survolé : {@code accent} translucide}
     *
     * <p>Minecraft la dessine par-dessus l'objet. Relue à chaque image, elle suit le thème.</p>
     */
    @Override
    public int getSlotColor(int index) {
        return (HOVER_ALPHA << 24) | (GaldurTheme.accent() & 0xFFFFFF);
    }

    /** Titre de l'écran en {@code text}, titre « Inventaire » en {@code text_muted}. */
    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        GaldurRender.text(guiGraphics, title, titleLabelX, titleLabelY, GaldurTheme.text(), false);
        if (showInventoryLabel) {
            GaldurRender.text(guiGraphics, playerInventoryTitle, inventoryLabelX, inventoryLabelY,
                    GaldurTheme.textMuted(), false);
        }
    }
}
