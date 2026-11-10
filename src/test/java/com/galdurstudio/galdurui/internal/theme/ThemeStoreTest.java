package com.galdurstudio.galdurui.internal.theme;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.galdurstudio.galdurui.api.theme.GaldurPaint;
import com.galdurstudio.galdurui.api.theme.GaldurTheme;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ThemeStoreTest {
    @AfterEach
    void reset() {
        ThemeStore.setPlayerTheme(Map.of(), Map.of());
    }

    @Test
    void anElementColourFallsBackThenTakesThePlayersPaint() {
        assertEquals(ThemeStore.color(GaldurTheme.BACKGROUND), ThemeStore.color(GaldurTheme.PANEL));
        GaldurPaint gradient = GaldurPaint.gradient(GaldurPaint.Direction.VERTICAL, 0xFF1E2A38, 0xFF2A2420);
        ThemeStore.setPlayerTheme(Map.of(GaldurTheme.PANEL, gradient), Map.of());
        assertEquals(gradient, ThemeStore.paint(GaldurTheme.PANEL));
        assertEquals(gradient, ThemeStore.paint(GaldurTheme.SLOT).isSolid() ? gradient : null, "slot keeps the background");
    }
}
