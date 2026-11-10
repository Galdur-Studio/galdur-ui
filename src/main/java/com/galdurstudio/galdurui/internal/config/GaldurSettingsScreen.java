package com.galdurstudio.galdurui.internal.config;

import com.galdurstudio.galdurui.api.bridge.GaldurBridge;
import com.galdurstudio.galdurui.api.layout.GaldurPanelLayout;
import com.galdurstudio.galdurui.api.layout.GaldurStack;
import com.galdurstudio.galdurui.api.theme.GaldurTheme;
import com.galdurstudio.galdurui.api.widget.GaldurButton;
import com.galdurstudio.galdurui.api.widget.GaldurCheckbox;
import com.galdurstudio.galdurui.api.widget.GaldurLabel;
import com.galdurstudio.galdurui.api.widget.GaldurSearchField;
import com.galdurstudio.galdurui.internal.client.Client;
import com.galdurstudio.galdurui.internal.theme.PlayerTheme;
import com.galdurstudio.galdurui.internal.theme.ThemeEditorScreen;
import java.util.Arrays;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

/**
 * Settings screen of Galdur UI on every loader (Mods menu; Mod Menu on Fabric): animations, the
 * Galdur look on every menu and its exclusions, where the style applies and the theme editor.
 * Drawn with the default Galdur theme ({@link DefaultThemeScreen}), whatever the player's colours.
 */
public final class GaldurSettingsScreen extends Screen implements DefaultThemeScreen {
    static final int WIDTH = 260;

    @Nullable
    private final Screen parent;
    private String excludedText = "";
    @Nullable
    private Component galdurThemeLabel;

    public GaldurSettingsScreen(@Nullable Screen parent) {
        super(Component.translatable("galdurui.configuration.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        excludedText = String.join(", ", GaldurConfig.excludedScreens());
        int gap = GaldurTheme.gap();
        GaldurStack column = GaldurStack.vertical(gap);
        column.add(GaldurLabel.of(title), settings -> settings.alignHorizontallyCenter());

        GaldurCheckbox animations = column.add(GaldurCheckbox.of(Component.translatable("galdurui.configuration.animations"),
                GaldurConfig::animations, GaldurConfig::setAnimations));
        animations.setTooltip(Tooltip.create(Component.translatable("galdurui.configuration.animations.tooltip")));
        GaldurCheckbox restyleAll = column.add(GaldurCheckbox.of(Component.translatable("galdurui.configuration.restyleAll"),
                GaldurConfig::restyleAll, GaldurConfig::setRestyleAll));
        restyleAll.setTooltip(Tooltip.create(Component.translatable("galdurui.configuration.restyleAll.tooltip")));

        column.add(GaldurLabel.muted(Component.translatable("galdurui.configuration.excludedScreens")));
        GaldurSearchField excluded = column.add(GaldurSearchField.of(WIDTH,
                Component.translatable("galdurui.configuration.excludedScreens.hint"),
                () -> excludedText, this::setExcluded).maxLength(2048));
        excluded.setTooltip(Tooltip.create(Component.translatable("galdurui.configuration.excludedScreens.tooltip")));

        GaldurButton rules = column.add(button(Component.translatable("galdurui.configuration.style_rules"),
                button -> Client.setScreen(new StyleRulesScreen(this))));
        rules.setTooltip(Tooltip.create(Component.translatable("galdurui.configuration.style_rules.tooltip")));
        GaldurButton editTheme = column.add(button(Component.translatable("galdurui.configuration.edit_theme"),
                button -> Client.setScreen(new ThemeEditorScreen(this))));
        // A modpack may keep its theme for everyone.
        if (GaldurBridge.playerThemeLockedEverywhere()) {
            editTheme.active = false;
            editTheme.setTooltip(Tooltip.create(Component.translatable("galdurui.configuration.edit_theme.locked")));
        }
        GaldurButton galdurTheme = column.add(button(galdurThemeLabel != null ? galdurThemeLabel
                : Component.translatable("galdurui.theme.galdur"), button -> {
                    PlayerTheme.save(PlayerTheme.Theme.EMPTY);
                    galdurThemeLabel = Component.translatable("galdurui.theme.galdur.done");
                    button.setMessage(galdurThemeLabel);
                }));
        galdurTheme.setTooltip(Tooltip.create(Component.translatable("galdurui.theme.galdur.tooltip")));
        column.add(button(CommonComponents.GUI_DONE, button -> onClose()));

        GaldurPanelLayout panel = new GaldurPanelLayout(column);
        addRenderableOnly(panel);
        panel.visitWidgets(this::addRenderableWidget);
        panel.arrangeElements();
        FrameLayout.centerInRectangle(panel, getRectangle());
    }

    /** {@return a button as wide as the panel} */
    static GaldurButton button(Component label, GaldurButton.OnPress onPress) {
        return new GaldurButton(0, 0, WIDTH, GaldurTheme.buttonHeight(), label, onPress);
    }

    private void setExcluded(String text) {
        excludedText = text;
        GaldurConfig.setExcludedScreens(Arrays.stream(text.split(","))
                .map(String::trim).filter(entry -> !entry.isEmpty()).toList());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        //? if <1.20.2
        //renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        if (minecraft != null) {
            Client.setScreen(parent);
        }
    }
}
