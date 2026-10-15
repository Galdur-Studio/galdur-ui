package com.galdurstudio.galdurui;

import com.galdurstudio.galdurui.internal.dev.DevBlocks;
import com.galdurstudio.galdurui.internal.dev.DevMenus;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Point d'entrée commun de Galdur UI.
 *
 * <p>Cette classe ne contient que ce qui est valable sur les deux côtés (client et serveur) :
 * l'identifiant du mod, le journal partagé et, en développement, les menus de test. Tout ce qui
 * touche au rendu vit côté client, dans {@link GaldurUIClient}.</p>
 */
@Mod(GaldurUI.MODID)
public class GaldurUI {
    /** Identifiant du mod, tel que déclaré dans {@code neoforge.mods.toml}. */
    public static final String MODID = "galdurui";

    /** Journal partagé par toute la bibliothèque. */
    public static final Logger LOGGER = LoggerFactory.getLogger("GaldurUI");

    public GaldurUI(IEventBus modEventBus) {
        LOGGER.info("Galdur UI chargé.");
        // Coffre Galdur de test (bloc, menu, /galduruidev chest) : développement uniquement, sur
        // les deux côtés pour que client et serveur aient le même registre.
        if (!FMLEnvironment.production) {
            DevBlocks.register(modEventBus);
            DevMenus.register(modEventBus);
        }
    }
}
