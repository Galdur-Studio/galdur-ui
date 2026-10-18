package com.galdurstudio.galdurui.internal.input;

import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

/**
 * Reads the state of input devices, for Galdur UI widgets.
 *
 * <p>Implementation for Minecraft 1.21.1.</p>
 */
public final class GaldurInput {
    /**
     * {@return {@code true} if the left mouse button is held right now}
     *
     * <p>Queries GLFW directly: in 1.21.1, {@code MouseHandler.isLeftPressed()} is only updated
     * when no screen is open, so it is useless in a GUI.</p>
     */
    public static boolean isLeftMouseDown() {
        long window = Minecraft.getInstance().getWindow().getWindow();
        return GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
    }

    private GaldurInput() {
    }
}
