package com.galdurstudio.galdurui.internal.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

//? if >=26.1 {
/*import net.minecraft.util.Util;
*///?} else {
import net.minecraft.Util;
//?}

/**
 * Calls to the Minecraft client that changed between the versions Galdur UI supports: the rest of
 * the library goes through here instead of calling {@link Minecraft} directly.
 */
public final class Client {
    private Client() {
    }

    /** {@return the screen currently shown, {@code null} in game} */
    @Nullable
    public static Screen screen() {
        // Very early in startup (mod loading), the game does not exist yet.
        if (Minecraft.getInstance() == null) {
            return null;
        }
        //? if >=26.1 {
        /*return Minecraft.getInstance().gui.screen();
        *///?} else {
        return Minecraft.getInstance().screen;
        //?}
    }

    /**
     * Shows a screen, or closes the current one.
     *
     * @param screen the screen to show, {@code null} to go back to the game
     */
    public static void setScreen(@Nullable Screen screen) {
        //? if >=26.1 {
        /*Minecraft.getInstance().gui.setScreen(screen);
        *///?} else {
        Minecraft.getInstance().setScreen(screen);
        //?}
    }

    /**
     * Runs {@code task} on the client thread at the next loop, never right away: from a click or a
     * key press, for instance, while the screen is still going through its widgets.
     *
     * @param task what to run
     */
    public static void later(Runnable task) {
        //? if >=26.1 {
        /*Minecraft.getInstance().schedule(task);
        *///?} else {
        Minecraft.getInstance().tell(task);
        //?}
    }

    /** {@return {@code true} while a Shift key is held} */
    public static boolean shiftDown() {
        //? if >=26.1 {
        /*return Minecraft.getInstance().hasShiftDown();
        *///?} else {
        return Screen.hasShiftDown();
        //?}
    }

    /** {@return the time in milliseconds, for animations (monotonic, not the date)} */
    public static long millis() {
        return Util.getMillis();
    }

    /** {@return the text of the system clipboard, empty if none} */
    public static String clipboard() {
        String text = Minecraft.getInstance().keyboardHandler.getClipboard();
        return text == null ? "" : text;
    }

    /** Puts {@code text} in the system clipboard. */
    public static void setClipboard(String text) {
        Minecraft.getInstance().keyboardHandler.setClipboard(text);
    }

    /** Opens a folder in the system file explorer. */
    public static void openFolder(java.nio.file.Path folder) {
        //? if >=26.1 {
        /*com.mojang.blaze3d.Blaze3D.openPath(folder);
        *///?} else {
        Util.getPlatform().openFile(folder.toFile());
        //?}
    }
}
