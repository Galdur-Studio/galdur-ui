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
 * EMI integration of Galdur UI, found by EMI through {@link EmiEntrypoint} and loaded only when
 * EMI is installed:
 * <ul>
 *   <li>EMI's item list avoids the {@link GaldurContainerScreen#getExclusionZones()} areas;</li>
 *   <li>hovering an item of a {@code GaldurItemGrid} and pressing R or U shows its recipes, and
 *       EMI can add it to its favourites.</li>
 * </ul>
 *
 * <p>No other Galdur UI class references this one: without EMI it is never loaded.</p>
 */
@EmiEntrypoint
public class GaldurEmiPlugin implements EmiPlugin {
    @Override
    @SuppressWarnings({"unchecked", "rawtypes"}) // GaldurContainerScreen is generic: Class<GaldurContainerScreen<?>> does not exist
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
