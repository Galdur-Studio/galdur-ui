package com.galdurstudio.galdurui.internal.input;

import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

/**
 * Lecture de l'état des périphériques d'entrée, pour les widgets de Galdur UI.
 *
 * <p>Implémentation visant Minecraft 1.21.1.</p>
 */
public final class GaldurInput {
    /**
     * {@return {@code true} si le bouton gauche de la souris est enfoncé en ce moment}
     *
     * <p>Interroge GLFW directement : en 1.21.1, {@code MouseHandler.isLeftPressed()} n'est mis
     * à jour que quand aucun écran n'est ouvert, il est donc inutilisable dans une interface.</p>
     */
    public static boolean isLeftMouseDown() {
        long window = Minecraft.getInstance().getWindow().getWindow();
        return GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
    }

    private GaldurInput() {
    }
}
