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
 * <p>Quatre états, chacun avec son sprite 9-slice en niveaux de gris, teinté au rendu par une
 * couleur du thème : au repos ({@code accent}), survolé ou ciblé au clavier
 * ({@code accent_hover}), appuyé ({@code accent_pressed}) et désactivé ({@code surface}). Le
 * libellé passe en {@code text_muted} quand le bouton est désactivé.</p>
 *
 * <pre>{@code
 * layout.addChild(GaldurButton.of(Component.literal("Valider"), bouton -> valider()));
 * }</pre>
 *
 * <p>{@link #of(Component, OnPress)} dimensionne le bouton d'après son libellé et le thème ;
 * le constructeur complet reste disponible pour une position et une taille imposées.</p>
 *
 * <p>Désactiver un bouton se fait par le champ {@code active} hérité d'{@link AbstractWidget} :
 * {@code bouton.active = false;}</p>
 *
 * <p>Les couleurs (teintes et libellé) sont relues dans le thème à chaque image : elles suivent
 * immédiatement un rechargement des ressources. Attention, Minecraft ignore F3+T tant qu'un
 * écran est ouvert ; le rechargement doit alors venir d'ailleurs, par exemple de
 * {@code Minecraft.reloadResourcePacks()}. La taille, elle, est fixée à la construction
 * ({@link #of(Component, OnPress)}) et ne suit le thème qu'à la reconstruction de l'écran.</p>
 */
public class GaldurButton extends AbstractWidget {
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

    /**
     * Vrai entre l'enfoncement et le relâchement du bouton de souris sur ce widget. Repasse à
     * faux uniquement dans {@link #onRelease}, que l'action soit déclenchée ou non, et par
     * {@link #clearHeld()} quand l'écran retire le bouton.
     *
     * <p>Surtout pas dans {@code setFocused(false)} : à chaque clic, Minecraft retire puis
     * redonne le focus au bouton déjà ciblé, entre l'appui et le relâchement
     * ({@code AbstractContainerEventHandler.setFocused}), ce qui effacerait l'appui.</p>
     */
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
     * Crée un bouton dimensionné d'après son libellé, à placer dans un layout.
     *
     * <p>Largeur : celle du libellé plus {@code spacing.button_padding_x} de chaque côté, sans
     * descendre sous {@code spacing.button_min_width}. Hauteur : {@code spacing.button_height}.
     * La position est laissée à {@code (0, 0)} : c'est le layout qui la fixe.</p>
     *
     * <pre>{@code
     * ligne.addChild(GaldurButton.of(Component.literal("Valider"), bouton -> valider()));
     * }</pre>
     *
     * @param label   libellé affiché au centre
     * @param onPress action déclenchée à l'activation
     * @return le bouton créé
     */
    public static GaldurButton of(Component label, OnPress onPress) {
        int width = Math.max(GaldurTheme.buttonMinWidth(), preferredWidth(label));
        return new GaldurButton(0, 0, width, GaldurTheme.buttonHeight(), label, onPress);
    }

    /** {@return la largeur du libellé augmentée de la marge intérieure du thème, des deux côtés} */
    private static int preferredWidth(Component label) {
        return GaldurRender.textWidth(label) + 2 * GaldurTheme.buttonPaddingX();
    }

    /**
     * Remplace le jeu de sprites du bouton, par exemple pour qu'un mod utilise le sien.
     *
     * <p>Ces sprites sont teintés comme ceux d'origine (couleur du thème selon l'état) : les
     * fournir en niveaux de gris, le blanc prenant exactement la couleur du thème.</p>
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
        return preferredWidth(getMessage());
    }

    /** États visuels du bouton, chacun avec son sprite et sa teinte. */
    private enum State { NORMAL, HOVERED, PRESSED, DISABLED }

    /** {@return l'état visuel courant du bouton} */
    private State currentState() {
        if (!active) {
            return State.DISABLED;
        }
        // Un appui en cours a toujours le focus : Minecraft le donne au bouton juste après
        // onClick. Si le focus est parti pendant l'appui (Tab bouton maintenu), le relâchement
        // ira à un autre widget : on n'affiche plus l'état enfoncé, sans toucher à held.
        if (held && isHovered() && isFocused()) {
            return State.PRESSED;
        }
        return isHovered() || isKeyboardFocused() ? State.HOVERED : State.NORMAL;
    }

    /**
     * {@return {@code true} si le bouton a le focus et que la dernière entrée vient du clavier}
     *
     * <p>Un clic souris donne aussi le focus au bouton. Sans ce filtre, il garderait
     * l'apparence du survol une fois le curseur parti. Minecraft mémorise le type de la dernière
     * entrée : Tab ou les flèches le passent en clavier, tout appui ou relâchement de bouton de
     * souris le repasse en souris.</p>
     */
    private boolean isKeyboardFocused() {
        return isFocused() && Minecraft.getInstance().getLastInputType().isKeyboard();
    }

    /** {@return le sprite de l'état {@code state}} */
    private ResourceLocation sprite(State state) {
        return switch (state) {
            case NORMAL -> spriteNormal;
            case HOVERED -> spriteHovered;
            case PRESSED -> spritePressed;
            case DISABLED -> spriteDisabled;
        };
    }

    /** {@return la teinte ARGB de l'état {@code state}, relue dans le thème} */
    private static int tint(State state) {
        return switch (state) {
            case NORMAL -> GaldurTheme.accent();
            case HOVERED -> GaldurTheme.accentHover();
            case PRESSED -> GaldurTheme.accentPressed();
            case DISABLED -> GaldurTheme.surface();
        };
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        State state = currentState();
        GaldurRender.drawNineSlice(guiGraphics, sprite(state), getX(), getY(), getWidth(), getHeight(), tint(state));

        int labelColor = active ? GaldurTheme.text() : GaldurTheme.textMuted();
        // Centrage vertical de la ligne de texte dans la hauteur du bouton.
        int labelY = getY() + (getHeight() - GaldurRender.lineHeight()) / 2;
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
     * <p>Minecraft ne prévient pas un widget quand il est retiré de l'écran : un écran qui garde
     * ses boutons d'un {@link Screen#init()} à l'autre (en surchargeant
     * {@code repositionElements()}), ou qui les réaffiche après avoir été fermé, doit appeler
     * cette méthode pour oublier un appui dont il n'a pas vu le relâchement. Les écrans qui
     * recréent leurs boutons dans {@code init()}, comme le fait {@code repositionElements()}
     * par défaut, n'en ont pas besoin.</p>
     */
    public void clearHeld() {
        held = false;
    }
}
