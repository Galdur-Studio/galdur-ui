package com.galdurstudio.galdurui.internal.config;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

/**
 * Settings screen of Galdur UI for loaders without a built-in configuration screen (Fabric with
 * Mod Menu, Forge). NeoForge uses its own {@code ConfigurationScreen} instead.
 */
public final class GaldurSettingsScreen extends Screen {
    private static final int WIDTH = 200;
    private static final int HEIGHT = 20;

    @Nullable
    private final Screen parent;

    public GaldurSettingsScreen(@Nullable Screen parent) {
        super(Component.translatable("galdurui.configuration.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int x = (width - WIDTH) / 2;
        addRenderableWidget(CycleButton.onOffBuilder(GaldurConfig.animations())
                .withTooltip(value -> Tooltip.create(Component.translatable("galdurui.configuration.animations.tooltip")))
                .create(x, height / 4 + 24, WIDTH, HEIGHT,
                        Component.translatable("galdurui.configuration.animations"),
                        (button, value) -> GaldurConfig.setAnimations(value)));
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onClose())
                .bounds(x, height - 40, WIDTH, HEIGHT)
                .build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, 20, 0xFFFFFF);
    }

    @Override
    public void onClose() {
        if (minecraft != null) {
            minecraft.setScreen(parent);
        }
    }
}
