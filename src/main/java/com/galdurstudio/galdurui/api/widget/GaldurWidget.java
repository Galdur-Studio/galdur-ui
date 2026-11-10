package com.galdurstudio.galdurui.api.widget;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
//? if >=1.21.9 {
/*import com.galdurstudio.galdurui.internal.input.GaldurInput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
*///?}

/**
 * Base class of the Galdur UI widgets: an {@link AbstractWidget} with the same mouse and keyboard
 * methods on every Minecraft version Galdur UI supports.
 *
 * <p>Since 1.21.9, Minecraft passes events ({@code MouseButtonEvent}, {@code KeyEvent},
 * {@code CharacterEvent}) instead of coordinates and codes. This class turns them back into the
 * methods of the older versions, which a widget overrides as usual:
 * {@code mouseClicked(double, double, int)}, {@code onClick(double, double)},
 * {@code onRelease(double, double)}, {@code onDrag(double, double, double, double)},
 * {@code mouseReleased}, {@code mouseDragged}, {@code keyPressed(int, int, int)},
 * {@code charTyped(char, int)}, {@code clicked(double, double)} and
 * {@code isValidClickButton(int)}. On older versions they are Minecraft's own methods.
 * Never write key or button codes as numbers: compare them with {@code InputConstants}, whose
 * values changed in 26.1.</p>
 */
public abstract class GaldurWidget extends AbstractWidget {
    protected GaldurWidget(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

    //? if >=1.21.9 {
    /*@Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        return mouseClicked(event.x(), event.y(), event.button());
    }

    /^*
     * Same behaviour as Minecraft before 1.21.9: a valid button pressed over the active widget
     * plays the click sound and calls {@link #onClick(double, double)}.
     *
     * @return {@code true} if the click was used
     ^/
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isActive() || !isValidClickButton(button) || !clicked(mouseX, mouseY)) {
            return false;
        }
        playDownSound(Minecraft.getInstance().getSoundManager());
        onClick(mouseX, mouseY);
        return true;
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        onClick(event.x(), event.y());
    }

    /^* Called by {@link #mouseClicked(double, double, int)} for a click on the widget. ^/
    public void onClick(double mouseX, double mouseY) {
    }

    /^* {@return {@code true} if a click at this point is on the widget} ^/
    protected boolean clicked(double mouseX, double mouseY) {
        return isActive() && isMouseOver(mouseX, mouseY);
    }

    /^* {@return {@code true} for the buttons the widget reacts to; the left one only by default} ^/
    protected boolean isValidClickButton(int button) {
        return button == GaldurInput.MOUSE_LEFT;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        return mouseReleased(event.x(), event.y(), event.button());
    }

    /^* @return {@code true} if the release was used ^/
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (!isValidClickButton(button)) {
            return false;
        }
        onRelease(mouseX, mouseY);
        return true;
    }

    @Override
    public void onRelease(MouseButtonEvent event) {
        onRelease(event.x(), event.y());
    }

    /^* Called when the button is released after a click on the widget. ^/
    public void onRelease(double mouseX, double mouseY) {
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        return mouseDragged(event.x(), event.y(), event.button(), dragX, dragY);
    }

    /^* @return {@code true} if the drag was used ^/
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (!isValidClickButton(button)) {
            return false;
        }
        onDrag(mouseX, mouseY, dragX, dragY);
        return true;
    }

    @Override
    protected void onDrag(MouseButtonEvent event, double dragX, double dragY) {
        onDrag(event.x(), event.y(), dragX, dragY);
    }

    /^* Called while the mouse moves with the button held, after a click on the widget. ^/
    protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        return keyPressed(event.key(), event.keycode(), event.modifiers());
    }

    /^*
     * @param keyCode   key, to compare with {@code InputConstants.KEY_*}
     * @param scanCode  physical key code
     * @param modifiers held modifier keys
     * @return {@code true} if the key was used
     ^/
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        return charTyped((char) event.codepoint(), 0);
    }

    /^*
     * @param codePoint typed character
     * @param modifiers held modifier keys (always 0 since 1.21.9)
     * @return {@code true} if the character was used
     ^/
    public boolean charTyped(char codePoint, int modifiers) {
        return false;
    }
    *///?}
}
