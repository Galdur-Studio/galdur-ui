package com.galdurstudio.galdurui.testmod;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common entry point of the Galdur UI test mod.
 *
 * <p>The test mod lives in its own source set ({@code src/testmod}) and is only loaded by the
 * development runs: it is never part of the Galdur UI jar. It registers on both sides what the
 * client and the server must agree on (test blocks and menus); everything else is in
 * {@link GaldurUITestClient}.</p>
 */
@Mod(GaldurUITest.MODID)
public class GaldurUITest {
    public static final String MODID = "galduruitest";

    public static final Logger LOGGER = LoggerFactory.getLogger("GaldurUITest");

    public GaldurUITest(IEventBus modEventBus) {
        TestBlocks.register(modEventBus);
        TestMenus.register(modEventBus);
    }
}
