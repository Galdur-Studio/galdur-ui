package com.galdurstudio.galdurui.api.window;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.IntConsumer;
import net.minecraft.network.chat.Component;

/**
 * Poignée vers une liste défilante d'une {@link GaldurWindow} : ses lignes, la ligne sélectionnée
 * et la position de défilement.
 *
 * <p>On l'obtient avec {@code GaldurWindow.addList(...)}. Comme pour {@link CheckboxHandle},
 * l'état vit ici et non dans le widget affiché : il survit aux redimensionnements, et les lignes
 * peuvent changer à tout moment, même fenêtre ouverte, sans reconstruire l'écran.</p>
 *
 * <pre>{@code
 * ListHandle minerais = window.addList(5);
 * minerais.addLine(Component.literal("Fer"));
 * minerais.addLine(Component.literal("Or"));
 * minerais.onSelect(index -> System.out.println("Choisi : " + index));
 * }</pre>
 *
 * <p>La largeur de la liste est calculée à la construction de l'écran d'après la plus longue
 * ligne. Une ligne plus longue ajoutée ensuite est coupée au bord, jusqu'à la prochaine
 * reconstruction (redimensionnement, ou ajout d'un élément à la fenêtre). Comme toute
 * {@link ElementHandle}, la liste se désactive, se cache ou reçoit une infobulle.</p>
 *
 * <p>À appeler sur le thread du client.</p>
 */
public class ListHandle extends ElementHandle {
    private final List<Component> lines = new ArrayList<>();
    private final List<Component> linesView = Collections.unmodifiableList(lines);
    private int selected = -1;
    private double scroll;
    private IntConsumer action;

    /** Non publique : seule {@link GaldurWindow} crée des listes. */
    ListHandle() {
    }

    /**
     * Ajoute une ligne à la fin de la liste.
     *
     * @param line le texte de la ligne
     */
    public void addLine(Component line) {
        lines.add(line);
    }

    /**
     * Remplace toutes les lignes. Si la ligne sélectionnée n'existe plus, la sélection est
     * retirée (et {@link #onSelect(IntConsumer)} est prévenue avec {@code -1}).
     *
     * @param newLines les nouvelles lignes, dans l'ordre d'affichage ; la liste est copiée
     */
    public void setLines(List<Component> newLines) {
        lines.clear();
        lines.addAll(newLines);
        if (selected >= lines.size()) {
            setSelected(-1);
        }
    }

    /** Retire toutes les lignes, et la sélection avec elles. */
    public void clearLines() {
        setLines(List.of());
    }

    /** {@return les lignes, en lecture seule ; la vue suit les changements de la liste} */
    public List<Component> getLines() {
        return linesView;
    }

    /** {@return l'index de la ligne sélectionnée, ou {@code -1} s'il n'y en a pas} */
    public int getSelected() {
        return selected;
    }

    /**
     * Sélectionne une ligne. Si la valeur change réellement, l'action donnée à
     * {@link #onSelect(IntConsumer)} est appelée, que le changement vienne du joueur ou de ce code.
     *
     * @param index l'index de la ligne, ou {@code -1} pour ne rien sélectionner ; un index hors
     *              de la liste vaut {@code -1}
     */
    public void setSelected(int index) {
        int valid = index >= 0 && index < lines.size() ? index : -1;
        if (valid == selected) {
            return;
        }
        selected = valid;
        if (action != null) {
            action.accept(valid);
        }
    }

    /**
     * Définit l'action appelée quand la sélection change. Une seule action est conservée : un
     * nouvel appel remplace la précédente. Elle reçoit l'index de la ligne, ou {@code -1}.
     *
     * @param action appelée à chaque changement de sélection
     */
    public void onSelect(IntConsumer action) {
        this.action = action;
    }

    /** {@return le défilement, en pixels depuis le haut du contenu} */
    public double getScroll() {
        return scroll;
    }

    /**
     * Retient le défilement, pour le rétablir quand l'écran est reconstruit. Appelée par le widget
     * affiché ; un mod n'a en général pas à s'en servir.
     *
     * @param scroll défilement, en pixels depuis le haut du contenu
     */
    public void setScroll(double scroll) {
        this.scroll = scroll;
    }
}
