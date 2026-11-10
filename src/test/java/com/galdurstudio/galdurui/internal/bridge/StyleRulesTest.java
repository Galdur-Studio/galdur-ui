package com.galdurstudio.galdurui.internal.bridge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.galdurstudio.galdurui.api.bridge.GaldurBridge.Element;
import java.io.StringReader;
import java.util.List;
import org.junit.jupiter.api.Test;

class StyleRulesTest {
    private static final StyleRules.Target OPTIONS =
            new StyleRules.Target("net.minecraft.client.gui.screens.options.OptionsScreen", List.of(), false, null);
    private static final StyleRules.Target CHEST =
            new StyleRules.Target("net.minecraft.client.gui.screens.inventory.ContainerScreen",
                    List.of("net.minecraft.client.gui.screens.inventory.AbstractContainerScreen"), true, "minecraft");
    private static final StyleRules.Target MACHINE =
            new StyleRules.Target("com.example.mymod.client.MachineScreen", List.of(), true, "mymod");

    @Test
    void theMostPreciseScopeWins() {
        StyleRules rules = new StyleRules();
        rules.setPlayer(StyleRules.ALL, Element.BUTTONS, true);
        rules.setPlayer(StyleRules.INVENTORIES, Element.BUTTONS, false);
        rules.setPlayer("screen:net.minecraft.client.gui.screens.inventory.ContainerScreen", Element.BUTTONS, true);
        assertTrue(rules.resolve(OPTIONS, Element.BUTTONS, false).value());
        assertTrue(rules.resolve(CHEST, Element.BUTTONS, false).value());
        rules.setPlayer("screen:net.minecraft.client.gui.screens.inventory.ContainerScreen", Element.BUTTONS, null);
        assertFalse(rules.resolve(CHEST, Element.BUTTONS, false).value());
    }

    @Test
    void withoutRulesTheDefaultApplies() {
        StyleRules rules = new StyleRules();
        StyleRules.Answer answer = rules.resolve(MACHINE, Element.SLIDERS, true);
        assertTrue(answer.value());
        assertEquals(StyleRules.Source.DEFAULT, answer.source());
    }

    @Test
    void locksBeatThePlayer() {
        StyleRules rules = new StyleRules();
        rules.setPlayer("mod:com.example.mymod", Element.PLAYER_THEME, true);
        rules.lockForMod("modid:mymod", Element.PLAYER_THEME, false);
        StyleRules.Answer answer = rules.resolve(MACHINE, Element.PLAYER_THEME, true);
        assertFalse(answer.value());
        assertEquals(StyleRules.Source.LOCKED, answer.source());
    }

    @Test
    void aModpackLocksFromTheFile() {
        StyleRules rules = new StyleRules();
        rules.read(new StringReader("{\"rules\": {\"*\": {\"buttons\": true}},"
                + " \"locked\": {\"*\": {\"player_theme\": false}, \"group:menus\": {\"buttons\": false}}}"));
        assertFalse(rules.resolve(OPTIONS, Element.BUTTONS, true).value());
        assertTrue(rules.resolve(CHEST, Element.BUTTONS, false).value());
        assertFalse(rules.resolve(MACHINE, Element.PLAYER_THEME, true).value());
        assertEquals(Boolean.FALSE, rules.lockedValue(StyleRules.ALL, Element.PLAYER_THEME));
    }

    @Test
    void theFileKeepsBothSections() {
        StyleRules rules = new StyleRules();
        rules.read(new StringReader("{\"locked\": {\"group:menus\": {\"sliders\": false}}}"));
        rules.setPlayer("mod:com.example.mymod", Element.CONTAINER_BACKGROUND, true);
        StyleRules copy = new StyleRules();
        copy.read(new StringReader(rules.write()));
        assertEquals(Boolean.TRUE, copy.playerValue("mod:com.example.mymod", Element.CONTAINER_BACKGROUND));
        assertEquals(Boolean.FALSE, copy.lockedValue("group:menus", Element.SLIDERS));
    }

    @Test
    void modsAreGroupedByTheirFirstPackages() {
        assertEquals("mod:com.example.mymod", StyleRules.modScope("com.example.mymod.client.gui.MachineScreen"));
        assertEquals("mod:net.minecraft.client", StyleRules.modScope("net.minecraft.client.gui.screens.TitleScreen"));
    }
}
