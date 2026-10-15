package com.galdurstudio.galdurui;

import com.galdurstudio.galdurui.internal.dev.DevScreens;
import com.galdurstudio.galdurui.internal.dev.GaldurDevCommands;
import com.galdurstudio.galdurui.internal.dev.LayoutDebug;
import com.galdurstudio.galdurui.internal.theme.ThemeLoader;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

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

        // Les outils de test ne doivent exister qu'en développement, jamais chez les joueurs.
        if (!FMLEnvironment.production) {
            GaldurUI.LOGGER.info("Environnement de développement : la commande /{} test est disponible.", GaldurUI.MODID);
            GaldurDevCommands.register();
            LayoutDebug.register();
            DevScreens.register(modEventBus);
        }
    }
}
