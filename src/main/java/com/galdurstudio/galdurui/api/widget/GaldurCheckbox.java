package com.galdurstudio.galdurui.api.widget;

import com.galdurstudio.galdurui.api.theme.GaldurTheme;
import com.galdurstudio.galdurui.internal.render.GaldurRender;
import com.galdurstudio.galdurui.internal.render.GaldurSprites;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.network.chat.Component;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * Case à cocher aux couleurs de la charte, utilisable dans les layouts vanilla.
 *
 * <p>Le widget ne garde pas l'état en mémoire : il le <strong>relit à chaque image</strong> auprès
 * d'un fournisseur, et demande un changement via une action. L'état vit ailleurs (dans la fenêtre
 * Galdur, par exemple) et survit ainsi à la recréation du widget, comme au redimensionnement.</p>
 *
 * <pre>{@code
 * boolean[] actif = {false};
 * colonne.addChild(GaldurCheckbox.of(Component.literal("Activer le son"),
 *         () -> actif[0], nouvelleValeur -> actif[0] = nouvelleValeur));
 * }</pre>
 *
 * <p>Le widget fait la taille du carré, d'un espace et du libellé.</p>
 */
public class GaldurCheckbox extends AbstractWidget {
    /** Côté du carré, en pixels. */
    private static final int BOX_SIZE = 9;
    /** Espace entre le carré et le libellé, en pixels. */
    private static final int GAP = 4;

    /** Donne l'état courant ; relu à chaque image, jamais copié. */
    private final BooleanSupplier checked;
    /** Reçoit la valeur voulue quand le joueur active la case. */
    private final Consumer<Boolean> onToggle;
    /** Vrai entre l'appui et le relâchement de la souris sur la case. */
    private boolean held;

    /**
     * Crée une case. Préférer {@link #of(Component, BooleanSupplier, Consumer)}.
     *
     * @param label    texte affiché à droite du carré
     * @param checked  fournisseur de l'état courant, appelé à chaque image
     * @param onToggle appelée avec la valeur voulue quand le joueur active la case
     */
    protected GaldurCheckbox(Component label, BooleanSupplier checked, Consumer<Boolean> onToggle) {
        super(0, 0, BOX_SIZE + GAP + GaldurRender.textWidth(label), Math.max(BOX_SIZE, GaldurRender.lineHeight()), label);
        this.checked = checked;
        this.onToggle = onToggle;
    }

    /**
     * {@return une case à cocher, positionnée en {@code (0, 0)} : c'est le layout qui la place}
     *
     * @param label    texte affiché à droite du carré
     * @param checked  fournisseur de l'état courant, appelé à chaque image
     * @param onToggle appelée avec la valeur voulue quand le joueur active la case
     */
    public static GaldurCheckbox of(Component label, BooleanSupplier checked, Consumer<Boolean> onToggle) {
        return new GaldurCheckbox(label, checked, onToggle);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int boxY = getY() + (getHeight() - BOX_SIZE) / 2 - 1;
        int labelY = getY() + (getHeight() - GaldurRender.lineHeight()) / 2;
        boolean highlighted = active
                && (isHovered() || (isFocused() && Minecraft.getInstance().getLastInputType().isKeyboard()));
        int mark = active ? GaldurTheme.accent() : GaldurTheme.textMuted();
        int label = active ? GaldurTheme.text() : GaldurTheme.textMuted();

        GaldurRender.drawNineSlice(guiGraphics, GaldurSprites.FIELD, getX(), boxY, BOX_SIZE, BOX_SIZE, GaldurTheme.surface());
        if (highlighted) {
            GaldurRender.drawOutline(guiGraphics, getX(), boxY, BOX_SIZE, BOX_SIZE, GaldurTheme.accentHover());
        }
        if (checked.getAsBoolean()) {
            GaldurRender.drawFill(guiGraphics, getX() + 2, boxY + 2, BOX_SIZE - 4, BOX_SIZE - 4, mark);
        }
        GaldurRender.text(guiGraphics, getMessage(), getX() + GAP + BOX_SIZE, labelY, label, true);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, getMessage());
    }

    /**
     * Appui de la souris sur la case : on retient seulement l'appui. La case ne change pas
     * encore, pour que le joueur puisse annuler en relâchant ailleurs.
     */
    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        held = true;
    }

    /**
     * Relâchement de la souris : la case bascule seulement si l'appui a commencé sur elle
     * et que le curseur est encore dessus.
     */
    @Override
    public void onRelease(double mouseX, double mouseY) {
        boolean wasHeld = held;
        held = false;
        if (wasHeld && clicked(mouseX, mouseY)) {
            this.toggle();
        }
    }

    /**
     * Bascule la case avec le clavier (Espace ou Entrée) quand elle a le focus.
     *
     * @return {@code true} si la touche a été consommée, {@code false} sinon
     */
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!active || !visible) {
            return false;
        }
        if (!CommonInputs.selected(keyCode)) {
            return false;
        }
        playDownSound(Minecraft.getInstance().getSoundManager());
        this.toggle();
        return true;
    }

    /**
     * Demande l'inverse de l'état courant. Le widget ne change rien lui-même : le nouvel
     * état s'affichera à l'image suivante, quand {@code checked} aura été relu.
     */
    private void toggle() {
        onToggle.accept(!checked.getAsBoolean());
    }
}