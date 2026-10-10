package com.galdurstudio.galdurui.api.widget;

import com.galdurstudio.galdurui.api.theme.GaldurTheme;
import com.galdurstudio.galdurui.internal.render.GaldurRender;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/**
 * Champ de recherche aux couleurs de la charte, utilisable dans les layouts vanilla.
 *
 * <p>La saisie elle-même (curseur, sélection, copier-coller, Ctrl+A, texte qui défile quand il
 * dépasse) est confiée à un {@link EditBox} vanilla placé à l'intérieur : Galdur UI dessine le
 * cadre, le texte d'indication et les couleurs, et transmet souris et clavier à la saisie.</p>
 *
 * <p>Comme {@link GaldurCheckbox}, le widget ne garde pas le texte en propre : il le
 * <strong>relit à chaque image</strong> auprès d'un fournisseur et signale chaque frappe. Le
 * texte survit ainsi à la recréation du widget, et le code peut le changer à tout moment.</p>
 *
 * <pre>{@code
 * String[] recherche = {""};
 * colonne.addChild(GaldurSearchField.of(120, Component.literal("Rechercher..."),
 *         () -> recherche[0], texte -> recherche[0] = texte));
 * }</pre>
 *
 * <ul>
 *   <li><strong>Texte d'indication</strong> : affiché en couleur atténuée tant que le champ est
 *       vide.</li>
 *   <li><strong>Désactivé</strong> ({@code active = false}) : texte atténué, plus de saisie.</li>
 *   <li><strong>Échap</strong> ferme l'écran, comme partout dans le jeu ; les flèches haut et
 *       bas passent au widget voisin.</li>
 * </ul>
 *
 * <p>Hauteur lue dans le thème : {@code spacing.field_height}. Le texte saisi est limité à
 * {@value #MAX_LENGTH} caractères.</p>
 */
public class GaldurSearchField extends AbstractWidget {
    /** Longueur maximale du texte saisi. */
    public static final int MAX_LENGTH = 256;
    /** Marge entre le bord du champ et le texte, en pixels. */
    private static final int PADDING_X = 4;

    private final EditBox input;
    private final Component placeholder;
    private final Supplier<String> text;

    /**
     * Crée un champ. Préférer {@link #of(int, Component, Supplier, Consumer)}.
     *
     * @param width       largeur totale, cadre compris, en pixels
     * @param placeholder texte d'indication, affiché tant que le champ est vide
     * @param text        fournisseur du texte courant, appelé à chaque image
     * @param onChange    appelée avec le nouveau texte à chaque modification par le joueur
     */
    protected GaldurSearchField(int width, Component placeholder, Supplier<String> text, Consumer<String> onChange) {
        super(0, 0, width, GaldurTheme.fieldHeight(), placeholder);
        this.placeholder = placeholder;
        this.text = text;
        this.input = new EditBox(Minecraft.getInstance().font, Math.max(1, width - 2 * PADDING_X),
                GaldurRender.lineHeight(), placeholder);
        input.setBordered(false);
        input.setMaxLength(MAX_LENGTH);
        input.setValue(text.get());
        input.setResponder(onChange);
    }

    /**
     * {@return un champ de recherche, positionné en {@code (0, 0)} : c'est le layout qui le place}
     *
     * @param width       largeur totale, cadre compris, en pixels
     * @param placeholder texte d'indication, affiché tant que le champ est vide
     * @param text        fournisseur du texte courant, appelé à chaque image
     * @param onChange    appelée avec le nouveau texte à chaque modification par le joueur
     */
    public static GaldurSearchField of(int width, Component placeholder, Supplier<String> text, Consumer<String> onChange) {
        return new GaldurSearchField(width, placeholder, text, onChange);
    }

    /**
     * {@return la largeur qui affiche entièrement le texte d'indication, ou {@code minWidth} si elle
     * est plus grande}
     *
     * @param placeholder le texte d'indication
     * @param minWidth    largeur minimale, en pixels
     */
    public static int preferredWidth(Component placeholder, int minWidth) {
        return Math.max(minWidth, GaldurRender.textWidth(placeholder) + 2 * PADDING_X + 2);
    }

    /**
     * Recopie le texte du fournisseur dans la saisie s'il a été changé ailleurs (par le code du
     * mod). Sans effet pendant que le joueur tape : les deux sont alors égaux.
     */
    private void syncFromSource() {
        String wanted = text.get();
        if (!input.getValue().equals(wanted)) {
            input.setValue(wanted);
        }
    }

    /** Place la saisie dans le cadre : marges horizontales, texte centré verticalement. */
    private void placeInput() {
        input.setX(getX() + PADDING_X);
        input.setY(getY() + (getHeight() - GaldurRender.lineHeight()) / 2 + 1);
        input.setWidth(Math.max(1, getWidth() - 2 * PADDING_X));
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        syncFromSource();
        placeInput();
        input.setEditable(active);
        input.setTextColor(GaldurTheme.text());
        input.setTextColorUneditable(GaldurTheme.textMuted());

        boolean highlighted = active && (isFocused() || isHovered());
        GaldurRender.drawFill(guiGraphics, getX(), getY(), getWidth(), getHeight(), GaldurTheme.surface());
        GaldurRender.drawOutline(guiGraphics, getX(), getY(), getWidth(), getHeight(),
                highlighted ? GaldurTheme.accentHover() : GaldurTheme.textMuted());

        if (input.getValue().isEmpty()) {
            GaldurRender.drawClipped(guiGraphics, input.getX(), getY(), input.getWidth(), getHeight(),
                    () -> GaldurRender.text(guiGraphics, placeholder, input.getX(), input.getY(), GaldurTheme.textMuted(), false));
        }
        input.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        input.setFocused(focused);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        placeInput();
        double insideX = Mth.clamp(mouseX, input.getX(), input.getX() + input.getWidth() - 1);
        input.onClick(insideX, input.getY());
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return active && visible && input.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return active && visible && input.charTyped(codePoint, modifiers);
    }

    /** Un champ de saisie ne fait pas le son d'un bouton quand on clique dedans. */
    @Override
    public void playDownSound(SoundManager handler) {
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        String value = input.getValue();
        narrationElementOutput.add(NarratedElementType.TITLE,
                value.isEmpty() ? placeholder : Component.literal(value));
    }
}
