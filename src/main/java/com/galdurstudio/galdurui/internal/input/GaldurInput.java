package com.galdurstudio.galdurui.internal.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
//? if >=1.21.9 {
/*import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
*///?}
//? if >=26.1 {
/*import org.lwjgl.sdl.SDLMouse;
*///?} else {
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
//?}

/**
 * Reads the state of input devices, for Galdur UI widgets.
 *
 * <p>Key and mouse button codes come from {@link InputConstants}, never from GLFW or SDL
 * directly: their values changed in 26.1, when Minecraft moved from GLFW to SDL.</p>
 */
public final class GaldurInput {
    /** Left mouse button, in the codes of the running version. */
    public static final int MOUSE_LEFT = InputConstants.MOUSE_BUTTON_LEFT;
    /** Right mouse button. */
    public static final int MOUSE_RIGHT = InputConstants.MOUSE_BUTTON_RIGHT;

    /**
     * {@return {@code true} if the left mouse button is held right now}
     *
     * <p>Asks the window system directly: {@code MouseHandler.isLeftPressed()} is only updated when
     * no screen is open, so it is useless in a GUI.</p>
     */
    public static boolean isLeftMouseDown() {
        //? if >=26.1 {
        /*return (SDLMouse.SDL_GetMouseState(null, null) & SDLMouse.SDL_BUTTON_LMASK) != 0;
        *///?} else {
        long window = Minecraft.getInstance().getWindow().getWindow();
        return GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        //?}
    }

    /**
     * {@return {@code true} for the keys that activate a focused widget: Enter, keypad Enter and Space}
     *
     * @param keyCode key code, as given to {@code keyPressed}
     */
    public static boolean isActivateKey(int keyCode) {
        return keyCode == InputConstants.KEY_RETURN || keyCode == InputConstants.KEY_SPACE
                || keyCode == InputConstants.KEY_NUMPADENTER;
    }

    /**
     * Passes a left click to a vanilla widget a Galdur widget wraps (the {@code EditBox} of a
     * search field, for instance).
     */
    public static void click(AbstractWidget target, double mouseX, double mouseY) {
        //? if >=1.21.9 {
        /*target.onClick(new MouseButtonEvent(mouseX, mouseY, new MouseButtonInfo(MOUSE_LEFT, 0)), false);
        *///?} else {
        target.onClick(mouseX, mouseY);
        //?}
    }

    /** Passes a key press to a wrapped vanilla element. @return {@code true} if it used the key */
    public static boolean keyPressed(GuiEventListener target, int keyCode, int scanCode, int modifiers) {
        //? if >=1.21.9 {
        /*return target.keyPressed(new KeyEvent(keyCode, scanCode, modifiers));
        *///?} else {
        return target.keyPressed(keyCode, scanCode, modifiers);
        //?}
    }

    /** Passes a typed character to a wrapped vanilla element. @return {@code true} if it used it */
    public static boolean charTyped(GuiEventListener target, char codePoint, int modifiers) {
        //? if >=1.21.9 {
        /*return target.charTyped(new CharacterEvent(codePoint));
        *///?} else {
        return target.charTyped(codePoint, modifiers);
        //?}
    }

    private GaldurInput() {
    }
}
