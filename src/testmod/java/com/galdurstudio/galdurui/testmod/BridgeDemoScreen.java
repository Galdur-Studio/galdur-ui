package com.galdurstudio.galdurui.testmod;

import com.galdurstudio.galdurui.api.bridge.GaldurBridge;
import com.galdurstudio.galdurui.internal.render.GaldurRender;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * A screen made only of vanilla widgets, as a mod would have it, restyled by
 * {@link GaldurBridge}: shows what the bridge changes without touching the screen code.
 */
public class BridgeDemoScreen extends Screen {
    private static final int WIDTH = 200;
    private static final int HEIGHT = 20;
    private static final int GAP = 4;

    /** Restyles this screen; called once by the test mod of each loader. */
    public static void register() {
        GaldurBridge.restyle(BridgeDemoScreen.class);
    }

    public BridgeDemoScreen() {
        super(Component.literal("Vanilla widgets, restyled by GaldurBridge"));
    }

    @Override
    protected void init() {
        int x = (width - WIDTH) / 2;
        int y = height / 2 - 3 * (HEIGHT + GAP);
        addRenderableWidget(Button.builder(Component.literal("Vanilla button"), button -> TestLog.LOGGER.info("Bridge: button"))
                .bounds(x, y, WIDTH, HEIGHT).build());
        y += HEIGHT + GAP;
        Button disabled = addRenderableWidget(Button.builder(Component.literal("Disabled button"), button -> {
        }).bounds(x, y, WIDTH, HEIGHT).build());
        disabled.active = false;
        y += HEIGHT + GAP;
        addRenderableWidget(CycleButton.onOffBuilder(true).create(x, y, WIDTH, HEIGHT, Component.literal("Cycle button")));
        y += HEIGHT + GAP;
        EditBox field = addRenderableWidget(new EditBox(font, x, y, WIDTH, HEIGHT, Component.literal("Text field")));
        field.setValue("Vanilla text field");
        y += HEIGHT + GAP;
        //? if >=1.20.2 {
        addRenderableWidget(Checkbox.builder(Component.literal("Vanilla checkbox"), font).pos(x, y).selected(true).build());
        //?} else {
        /*addRenderableWidget(new Checkbox(x, y, WIDTH, HEIGHT, Component.literal("Vanilla checkbox"), true));
        *///?}
        y += HEIGHT + GAP;
        addRenderableWidget(new AbstractSliderButton(x, y, WIDTH, HEIGHT, Component.literal("Slider"), 0.4) {
            {
                updateMessage();
            }

            @Override
            protected void updateMessage() {
                setMessage(Component.literal("Slider: " + Math.round(value * 100) + " %"));
            }

            @Override
            protected void applyValue() {
            }
        });
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        //? if <1.20.2
        //renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        GaldurRender.drawCenteredText(graphics, title, width / 2,
                height / 2 - 3 * (HEIGHT + GAP) - 16, 0xFFFFFFFF);
    }
}
