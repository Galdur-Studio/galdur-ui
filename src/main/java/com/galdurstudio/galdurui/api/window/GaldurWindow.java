package com.galdurstudio.galdurui.api.window;

import com.galdurstudio.galdurui.internal.window.TextElement;
import com.galdurstudio.galdurui.internal.window.WindowModel;
import com.galdurstudio.galdurui.internal.window.WindowScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/**
 * Un écran décrit par le code d'un mod : titre, textes, boutons, cases à cocher.
 *
 * <p>On la crée avec {@link com.galdurstudio.galdurui.api.GaldurLib#createWindow()}. Elle ne
 * contient que la description de l'écran ; la mise en page, le thème et le rendu sont
 * assurés par Galdur UI, et aucune coordonnée n'est jamais à calculer.</p>
 *
 * <p><strong>Squelette</strong> : {@link #open()} affiche l'écran mais ne vérifie pas encore s'il
 * est déjà ouvert, et {@link #close()} ne fait encore rien. Les autres éléments (titre, boutons,
 * cases à cocher) arrivent aux étapes suivantes.</p>
 */
public class GaldurWindow {
    private final WindowModel model = new WindowModel();

    /** Crée une fenêtre vide. Préférer {@code GaldurLib.createWindow()}. */
    public GaldurWindow() {}

    /** Affiche la fenêtre à l'écran. Sans effet si elle est déjà ouverte. */
    public void open() {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.tell(() -> minecraft.setScreen(new WindowScreen(model)));
    }
    /** Ferme la fenêtre et revient à l'écran précédent. Sans effet si elle est fermée. */
    public void close() {}

    /**
     * Ajoute une ligne de texte à la fin de la fenêtre.
     *
     * <p>Les éléments sont affichés de haut en bas, dans l'ordre des appels. À appeler avant
     * {@link #open()}.</p>
     *
     * @param text le texte à afficher
     */
    public void addText(Component text) {
        TextElement element = new TextElement(text);
        model.add(element);
    }
}
