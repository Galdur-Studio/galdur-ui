package com.galdurstudio.galdurui.internal.narration;

import com.galdurstudio.galdurui.GaldurUI;
import com.galdurstudio.galdurui.api.text.GaldurFormat;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * Textes lus par le narrateur de Minecraft (Ctrl+B) pour les widgets Galdur UI.
 *
 * <p>Tout passe par des clés de traduction : celles de Minecraft quand il en a une pour la même
 * chose (« Bouton : %s », « Onglet %s sur %s »...), sinon les nôtres, préfixées
 * {@code narration.galdurui.} et traduites dans {@code assets/galdurui/lang}. Le narrateur lit
 * ainsi dans la langue du joueur, comme pour les écrans vanilla.</p>
 *
 * <p>Rappel du fonctionnement : chaque widget remplit des entrées typées ({@code TITLE} : ce que
 * c'est, {@code POSITION} : où l'on est, {@code USAGE} : comment s'en servir, {@code HINT} :
 * l'infobulle). Minecraft ne relit que ce qui a changé depuis la dernière lecture : survoler une
 * autre case d'une grille ne relit que la case.</p>
 */
public final class GaldurNarration {
    private static final String PREFIX = "narration." + GaldurUI.MODID + ".";

    private GaldurNarration() {
    }

    /** {@return la traduction de {@code narration.galdurui.<key>}} */
    public static Component ours(String key, Object... args) {
        return Component.translatable(PREFIX + key, args);
    }

    /**
     * {@return {@code title} suivi de « désactivé » si le widget est inactif, tel quel sinon}
     *
     * @param title  ce que le widget lit déjà
     * @param active l'état du widget
     */
    public static Component withState(Component title, boolean active) {
        return active ? title : CommonComponents.joinForNarration(title, ours("disabled"));
    }

    /**
     * Ajoute la consigne d'utilisation, au clavier si le widget a le focus, à la souris sinon.
     * Rien pour un widget inactif : il ne réagit à rien.
     *
     * @param output  la sortie du widget
     * @param active  l'état du widget
     * @param focused {@code true} si le widget a le focus clavier
     * @param focusedKey clé de traduction de la consigne au clavier
     * @param hoveredKey clé de traduction de la consigne à la souris
     */
    public static void usage(NarrationElementOutput output, boolean active, boolean focused,
                             String focusedKey, String hoveredKey) {
        if (active) {
            output.add(NarratedElementType.USAGE, Component.translatable(focused ? focusedKey : hoveredKey));
        }
    }

    /**
     * {@return le nom d'un objet suivi de sa quantité complète (« Lingot de fer, 1 234 »), ou le
     * nom seul pour une quantité de 1 ou moins, comme à l'affichage}
     *
     * @param stack  l'objet
     * @param amount la quantité affichée
     */
    public static Component item(ItemStack stack, long amount) {
        if (amount <= 1) {
            return stack.getHoverName();
        }
        return ours("amount", stack.getHoverName(), GaldurFormat.fullAmount(amount));
    }
}
