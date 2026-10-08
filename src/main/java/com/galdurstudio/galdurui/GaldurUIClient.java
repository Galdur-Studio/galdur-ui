package com.galdurstudio.galdurui;

import com.galdurstudio.galdurui.internal.theme.ThemeLoader;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

/**
 * Point d'entrée client de Galdur UI.
 *
 * <p>Toute la bibliothèque est du rendu : c'est ici que l'on branche le chargeur de thème.
 * Rien dans cette classe ne doit être atteignable depuis un serveur dédié.</p>
 */
@Mod(value = GaldurUI.MODID, dist = Dist.CLIENT)
public class GaldurUIClient {
    public GaldurUIClient(IEventBus modEventBus) {
        ThemeLoader.register(modEventBus);
    }
}
