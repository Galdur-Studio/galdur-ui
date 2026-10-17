package com.galdurstudio.galdurui.api.widget;

import com.galdurstudio.galdurui.internal.narration.GaldurNarration;
import com.galdurstudio.galdurui.api.text.GaldurFormat;
import com.galdurstudio.galdurui.api.theme.GaldurTheme;
import com.galdurstudio.galdurui.internal.render.GaldurRender;
import com.galdurstudio.galdurui.internal.render.GaldurSprites;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

/**
 * Case d'objet aux couleurs de la charte : l'icône d'un objet et sa quantité abrégée
 * (« 64 », « 1,2k », « 64k », « 1,2M »), utilisable dans les layouts vanilla.
 *
 * <p>C'est une case <strong>d'affichage</strong>, pas un vrai slot de menu : elle ne déplace aucun
 * objet et ne parle pas au serveur. Elle sert à montrer des objets en grand nombre (le contenu
 * d'un réseau de stockage, une recette) et prévient le mod quand on clique dessus ; au mod de
 * décider quoi faire.</p>
 *
 * <p>Comme {@link GaldurCheckbox}, le widget relit l'objet et la quantité <strong>à chaque
 * image</strong> auprès de fournisseurs : la case suit le contenu sans être recréée. La quantité
 * est un {@code long} distinct de la taille de la pile, pour dépasser 64 et même
 * {@code Integer.MAX_VALUE}.</p>
 *
 * <pre>{@code
 * GaldurItemSlot fer = GaldurItemSlot.of(() -> new ItemStack(Items.IRON_INGOT), () -> reseau.quantite(fer))
 *         .onPress(bouton -> bouton == 0 ? prendrePile() : prendreMoitie());
 * }</pre>
 *
 * <ul>
 *   <li><strong>Quantité</strong> : masquée si elle vaut 1, comme en vanilla.</li>
 *   <li><strong>Infobulle</strong> : celle de l'objet, comme dans un inventaire, suivie de la
 *       quantité exacte quand elle est abrégée.</li>
 *   <li><strong>Clic</strong> : gauche ({@code 0}) ou droit ({@code 1}), transmis au mod ; Entrée
 *       ou Espace au clavier valent un clic gauche.</li>
 *   <li><strong>Désactivée</strong> ({@code active = false}) : quantité atténuée, plus de clic ;
 *       l'infobulle reste.</li>
 *   <li><strong>Vide</strong> : une pile vide affiche une case vide, sans infobulle.</li>
 * </ul>
 */
public class GaldurItemSlot extends AbstractWidget {
    /** Côté d'une case, cadre compris, en pixels : celui d'un slot vanilla. */
    public static final int SIZE = GaldurRender.ITEM_SIZE + 2;
    /** Opacité du surlignage de la case survolée (0 à 255). */
    private static final int HOVER_ALPHA = 0x60;

    private final Supplier<ItemStack> stack;
    private final LongSupplier amount;
    private IntConsumer onPress = button -> {};

    /**
     * Crée une case. Préférer {@link #of(Supplier)} ou {@link #of(Supplier, LongSupplier)}.
     *
     * @param stack  fournisseur de l'objet affiché, appelé à chaque image
     * @param amount fournisseur de la quantité affichée, appelé à chaque image
     */
    protected GaldurItemSlot(Supplier<ItemStack> stack, LongSupplier amount) {
        super(0, 0, SIZE, SIZE, Component.empty());
        this.stack = stack;
        this.amount = amount;
    }

    /**
     * {@return une case qui affiche la taille de la pile comme quantité}
     *
     * @param stack fournisseur de l'objet affiché, appelé à chaque image
     */
    public static GaldurItemSlot of(Supplier<ItemStack> stack) {
        return new GaldurItemSlot(stack, () -> stack.get().getCount());
    }

    /**
     * {@return une case dont la quantité affichée est fournie à part}
     *
     * @param stack  fournisseur de l'objet affiché, appelé à chaque image ; sa taille est ignorée
     * @param amount fournisseur de la quantité affichée, appelé à chaque image
     */
    public static GaldurItemSlot of(Supplier<ItemStack> stack, LongSupplier amount) {
        return new GaldurItemSlot(stack, amount);
    }

    /**
     * Définit l'action appelée quand on clique sur la case. Une seule action est conservée.
     *
     * @param onPress reçoit le bouton de la souris : {@code 0} gauche, {@code 1} droit
     * @return cette case, pour chaîner les appels
     */
    public GaldurItemSlot onPress(IntConsumer onPress) {
        this.onPress = onPress;
        return this;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        ItemStack item = stack.get();
        long count = amount.getAsLong();
        int itemX = getX() + 1;
        int itemY = getY() + 1;
        boolean keyboardFocused = isFocused() && Minecraft.getInstance().getLastInputType().isKeyboard();
        boolean highlighted = active && (isHovered() || keyboardFocused);

        GaldurRender.drawNineSlice(guiGraphics, GaldurSprites.SLOT, getX(), getY(), SIZE, SIZE, GaldurTheme.background());
        if (highlighted) {
            GaldurRender.drawOutline(guiGraphics, getX(), getY(), SIZE, SIZE, GaldurTheme.accentHover());
        }
        if (highlighted) {
            GaldurRender.drawFill(guiGraphics, itemX, itemY, GaldurRender.ITEM_SIZE, GaldurRender.ITEM_SIZE,
                    (HOVER_ALPHA << 24) | (GaldurTheme.accent() & 0xFFFFFF));
        }
        if (item.isEmpty()) {
            return;
        }
        GaldurRender.drawItem(guiGraphics, item, itemX, itemY);
        if (count != 1) {
            GaldurRender.drawItemCount(guiGraphics, GaldurFormat.amount(count), itemX, itemY,
                    active ? GaldurTheme.text() : GaldurTheme.textMuted());
        }
        if (isHovered()) {
            GaldurRender.showItemTooltip(item, count >= 1000
                    ? List.of(Component.literal(GaldurFormat.fullAmount(count)).withStyle(ChatFormatting.GRAY))
                    : List.of());
        }
    }

    /** Accepte le clic gauche et le clic droit. */
    @Override
    protected boolean isValidClickButton(int button) {
        return button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!active || !visible || !isValidClickButton(button) || !clicked(mouseX, mouseY)) {
            return false;
        }
        onPress.accept(button);
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!active || !visible || !CommonInputs.selected(keyCode)) {
            return false;
        }
        onPress.accept(GLFW.GLFW_MOUSE_BUTTON_LEFT);
        return true;
    }

    /** Une case d'objet ne fait pas le son d'un bouton. */
    @Override
    public void playDownSound(SoundManager handler) {
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        ItemStack item = stack.get();
        Component title = item.isEmpty()
                ? GaldurNarration.ours("slot.empty")
                : GaldurNarration.item(item, amount.getAsLong());
        // Pas de consigne : ce que fait un clic dépend du mod.
        narrationElementOutput.add(NarratedElementType.TITLE, GaldurNarration.withState(title, active));
    }
}
