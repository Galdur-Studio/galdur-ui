package com.galdurstudio.galdurui.internal.bridge;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class ScreenFilterTest {
    @Test
    void matchesAClassItsPackageAndSubPackages() {
        List<String> entries = List.of("net.minecraft.client.gui.screens.TitleScreen", "com.example.mymod");
        assertTrue(ScreenFilter.excludes("net.minecraft.client.gui.screens.TitleScreen", entries));
        assertTrue(ScreenFilter.excludes("com.example.mymod.MachineScreen", entries));
        assertTrue(ScreenFilter.excludes("com.example.mymod.client.gui.CrateScreen", entries));
    }

    @Test
    void doesNotMatchANameThatOnlyStartsTheSame() {
        List<String> entries = List.of("net.minecraft.client.gui.screens.TitleScreen", "com.example.mymod");
        assertFalse(ScreenFilter.excludes("net.minecraft.client.gui.screens.TitleScreenExtra", entries));
        assertFalse(ScreenFilter.excludes("com.example.mymodextra.MachineScreen", entries));
    }

    @Test
    void anExcludedClassAlsoExcludesItsSubclasses() {
        assertTrue(ScreenFilter.excludes(java.util.ArrayList.class, List.of("java.util.AbstractList")));
        assertFalse(ScreenFilter.excludes(java.util.ArrayList.class, List.of("java.util.LinkedList")));
    }

    @Test
    void ignoresBlankEntriesAndSpaces() {
        assertFalse(ScreenFilter.excludes("com.example.A", List.of("", "  ")));
        assertTrue(ScreenFilter.excludes("com.example.A", List.of(" com.example ")));
    }
}
