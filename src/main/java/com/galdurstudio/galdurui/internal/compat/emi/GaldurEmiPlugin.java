package com.galdurstudio.galdurui.internal.compat.emi;

import com.galdurstudio.galdurui.api.screen.GaldurContainerScreen;
import com.galdurstudio.galdurui.internal.compat.GridLookup;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiStackInteraction;
import dev.emi.emi.api.widget.Bounds;
import net.minecraft.client.renderer.Rect2i;

/**
 * Intégration EMI de Galdur UI, découverte par EMI grâce à {@link EmiEntrypoint} et chargée
 * seulement s'il est installé :
 * <ul>
 *   <li>la liste d'objets d'EMI évite les zones de
 *       {@link GaldurContainerScreen#getExclusionZones()} ;</li>
 *   <li>survoler un objet d'une {@code GaldurItemGrid} puis appuyer sur R ou U montre ses
 *       recettes, et EMI peut l'afficher dans ses favoris.</li>
 * </ul>
 *
 * <p>Aucune autre classe de Galdur UI ne référence celle-ci : sans EMI, elle n'est jamais
 * chargée.</p>
 */
@EmiEntrypoint
public class GaldurEmiPlugin implements EmiPlugin {
    @Override
    @SuppressWarnings({"unchecked", "rawtypes"}) // GaldurContainerScreen est générique : Class<GaldurContainerScreen<?>> n'existe pas
    public void register(EmiRegistry registry) {
        registry.addExclusionArea((Class) GaldurContainerScreen.class, (screen, consumer) -> {
            for (Rect2i zone : ((GaldurContainerScreen<?>) screen).getExclusionZones()) {
                consumer.accept(new Bounds(zone.getX(), zone.getY(), zone.getWidth(), zone.getHeight()));
            }
        });
        registry.addGenericStackProvider((screen, mouseX, mouseY) -> {
            GridLookup.Hit hit = GridLookup.find(screen, mouseX, mouseY);
            return hit == null ? EmiStackInteraction.EMPTY : new EmiStackInteraction(EmiStack.of(hit.stack()));
        });
    }
}
