package com.galdurstudio.galdurui.api.widget;

import com.galdurstudio.galdurui.api.theme.GaldurTheme;
import com.galdurstudio.galdurui.internal.render.GaldurRender;
import com.galdurstudio.galdurui.internal.render.GaldurSprites;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Bouton aux couleurs de la charte, avec libellé centré.
 *
 * <p>Quatre états, chacun avec son sprite 9-slice : au repos, survolé (ou ciblé au clavier),
 * appuyé, et désactivé. Le libellé passe en {@code text_muted} quand le bouton est
 * désactivé.</p>
 *
 * <pre>{@code
 * addRenderableWidget(new GaldurButton(x, y, 120, 20, Component.literal("Valider"), bouton -> valider()));
 * }</pre>
 *
 * <p>Désactiver un bouton se fait par le champ {@code active} hérité d'{@link AbstractWidget} :
 * {@code bouton.active = false;}</p>
 *
 * <p>Comme le bouton lit le thème à chaque image, un rechargement des ressources (F3+T) met
 * immédiatement ses couleurs à jour.</p>
 */
public class GaldurButton extends AbstractWidget {
    /** Marge horizontale minimale entre le bord du bouton et son libellé, en pixels. */
    private static final int LABEL_PADDING = 6;

    /** Action déclenchée quand le bouton est activé. */
    @FunctionalInterface
    public interface OnPress {
        /**
         * Appelé lorsque l'utilisateur active le bouton, à la souris ou au clavier.
         *
         * @param button le bouton activé
         */
        void onPress(GaldurButton button);
    }

    private final OnPress onPress;

    private ResourceLocation spriteNormal = GaldurSprites.BUTTON;
    private ResourceLocation spriteHovered = GaldurSprites.BUTTON_HOVER;
    private ResourceLocation spritePressed = GaldurSprites.BUTTON_PRESSED;
    private ResourceLocation spriteDisabled = GaldurSprites.BUTTON_DISABLED;

    /** Vrai entre l'enfoncement et le relâchement du bouton de souris sur ce widget. */
    private boolean held;

    /**
     * Crée un bouton.
     *
     * @param x       abscisse du coin supérieur gauche
     * @param y       ordonnée du coin supérieur gauche
     * @param width   largeur, en pixels d'interface
     * @param height  hauteur, en pixels d'interface
     * @param label   libellé affiché au centre
     * @param onPress action déclenchée à l'activation
     */
    public GaldurButton(int x, int y, int width, int height, Component label, OnPress onPress) {
        super(x, y, width, height, label);
        this.onPress = onPress;
    }

    /**
     * Remplace le jeu de sprites du bouton, par exemple pour qu'un mod utilise le sien.
     *
     * @param normal   sprite au repos
     * @param hovered  sprite au survol ou au focus clavier
     * @param pressed  sprite pendant l'appui
     * @param disabled sprite à l'état désactivé
     * @return ce bouton, pour chaîner les appels
     */
    public GaldurButton withSprites(ResourceLocation normal, ResourceLocation hovered,
                                    ResourceLocation pressed, ResourceLocation disabled) {
        this.spriteNormal = normal;
        this.spriteHovered = hovered;
        this.spritePressed = pressed;
        this.spriteDisabled = disabled;
        return this;
    }

    /**
     * {@return la largeur minimale permettant d'afficher le libellé en entier}
     *
     * <p>Pratique pour dimensionner un bouton d'après son texte plutôt qu'en dur.</p>
     */
    public int getPreferredWidth() {
        return GaldurRender.textWidth(getMessage()) + 2 * LABEL_PADDING;
    }

    /** {@return le sprite correspondant à l'état courant du bouton} */
    private ResourceLocation currentSprite() {
        if (!active) {
            return spriteDisabled;
        }
        if (held && isHovered()) {
            return spritePressed;
        }
        // Le focus clavier reprend l'apparence du survol : une seule cible visuelle à suivre.
        return isHovered() || isFocused() ? spriteHovered : spriteNormal;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        GaldurRender.drawNineSlice(guiGraphics, currentSprite(), getX(), getY(), getWidth(), getHeight());

        int labelColor = active ? GaldurTheme.text() : GaldurTheme.textMuted();
        // Centrage vertical de la ligne de texte dans la hauteur du bouton.
        int labelY = getY() + (getHeight() - GaldurRender.TEXT_HEIGHT) / 2;
        GaldurRender.drawCenteredText(guiGraphics, getMessage(), getX() + getWidth() / 2, labelY, labelColor);
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        held = true;
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        // On n'agit que si l'appui a commencé sur ce bouton et s'y termine : glisser le curseur
        // en dehors avant de relâcher annule le clic, comme partout ailleurs dans le jeu.
        boolean wasHeld = held;
        held = false;
        if (wasHeld && clicked(mouseX, mouseY)) {
            onPress.onPress(this);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!active || !visible) {
            return false;
        }
        if (!CommonInputs.selected(keyCode)) {
            return false;
        }
        playDownSound(Minecraft.getInstance().getSoundManager());
        onPress.onPress(this);
        return true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        defaultButtonNarrationText(narrationElementOutput);
    }

    /** {@return {@code true} si le bouton est actuellement maintenu enfoncé} */
    public boolean isHeld() {
        return held;
    }

    /**
     * Réinitialise l'état d'appui.
     *
     * <p>À appeler si un écran reprend la main sans que le relâchement ait été vu, par exemple
     * après un {@link Screen#init()} déclenché par un redimensionnement de la fenêtre.</p>
     */
    public void clearHeld() {
        held = false;
    }
}
