package com.galdurstudio.galdurui.api.window;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Unit tests run without the Minecraft classes, so tooltips ({@code Component}) are never touched
 * here; they are checked in game with {@code /galdurui window}.
 */
class ElementHandleTest {
    private int changes;

    private ElementHandle handle() {
        ElementHandle handle = new ElementHandle();
        handle.setChangeListener(() -> changes++);
        return handle;
    }

    @Test
    void enabledAndVisibleByDefault() {
        ElementHandle handle = new ElementHandle();
        assertTrue(handle.isEnabled());
        assertTrue(handle.isVisible());
    }

    @Test
    void disablingAndHidingNotifyTheWindow() {
        ElementHandle handle = handle();
        handle.setEnabled(false);
        handle.setVisible(false);
        assertFalse(handle.isEnabled());
        assertFalse(handle.isVisible());
        assertEquals(2, changes);
    }

    @Test
    void noNotificationWhenStateDoesNotChange() {
        ElementHandle handle = handle();
        handle.setEnabled(true);
        handle.setVisible(true);
        assertEquals(0, changes);
    }

    @Test
    void checkboxHasCommonStatesToo() {
        CheckboxHandle box = new CheckboxHandle(true);
        box.setChangeListener(() -> changes++);
        box.setEnabled(false);
        // Checking does not rebuild the screen: the checkbox reads its state every frame.
        box.setChecked(false);
        assertFalse(box.isEnabled());
        assertFalse(box.isChecked());
        assertEquals(1, changes);
    }
}
