package com.galdurstudio.galdurui.fabric;

import com.galdurstudio.galdurui.internal.config.GaldurSettingsScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/**
 * Mod Menu integration: the settings screen of Galdur UI. Declared as a {@code modmenu}
 * entrypoint, so it is only loaded when Mod Menu is installed.
 */
public final class GaldurModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return GaldurSettingsScreen::new;
    }
}
