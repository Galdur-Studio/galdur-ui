package com.galdurstudio.galdurui;

import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Point d'entrée commun de Galdur UI.
 *
 * <p>Cette classe ne contient que ce qui est valable sur les deux côtés (client et serveur) :
 * l'identifiant du mod et le journal partagé. Tout ce qui touche au rendu vit côté client,
 * dans {@link GaldurUIClient}.</p>
 */
@Mod(GaldurUI.MODID)
public class GaldurUI {
    /** Identifiant du mod, tel que déclaré dans {@code neoforge.mods.toml}. */
    public static final String MODID = "galdurui";

    /** Journal partagé par toute la bibliothèque. */
    public static final Logger LOGGER = LoggerFactory.getLogger("GaldurUI");

    public GaldurUI() {
        LOGGER.info("Galdur UI chargé.");
    }
}
