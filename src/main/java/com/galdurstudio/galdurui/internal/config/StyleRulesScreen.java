package com.galdurstudio.galdurui.internal.config;

import com.galdurstudio.galdurui.api.bridge.GaldurBridge;
import com.galdurstudio.galdurui.api.bridge.GaldurBridge.Element;
import com.galdurstudio.galdurui.internal.bridge.StyleRules;
import com.galdurstudio.galdurui.internal.client.Client;
import com.galdurstudio.galdurui.api.layout.GaldurPanelLayout;
import com.galdurstudio.galdurui.api.layout.GaldurStack;
import com.galdurstudio.galdurui.api.theme.GaldurTheme;
import com.galdurstudio.galdurui.api.widget.GaldurButton;
import com.galdurstudio.galdurui.api.widget.GaldurLabel;
import com.galdurstudio.galdurui.internal.render.GaldurRender;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

/**
 * Where the Galdur look applies, element by element: for the whole game, the vanilla inventories,
 * the vanilla menus, each mod and each screen shown since the game started. Each switch inherits
 * from the wider scope, or says yes or no; what a mod or the modpack locked is greyed out and
 * cannot be changed. Drawn with the default Galdur theme, so it stays readable whatever the player's colours.
 */
public final class StyleRulesScreen extends Screen implements DefaultThemeScreen {
    private static final int WIDTH = 150;

    @Nullable
    private final Screen parent;
    private final List<String> scopes = new ArrayList<>();
    private int scope;

    public StyleRulesScreen(@Nullable Screen parent) {
        super(Component.translatable("galdurui.style.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        collectScopes();
        int gap = GaldurTheme.gap();
        int height = GaldurTheme.buttonHeight();
        String current = scopes.get(scope);
        Element[] elements = Element.values();
        // Wide enough for the longest label of this scope (French is longer than English), within the screen.
        Boolean[] locks = new Boolean[elements.length];
        int wanted = WIDTH;
        for (int index = 0; index < elements.length; index++) {
            locks[index] = GaldurBridge.rules().lockedValue(current, elements[index]);
            wanted = Math.max(wanted, GaldurRender.textWidth(label(current, elements[index], locks[index]))
                    + 2 * GaldurTheme.buttonPaddingX() + 8);
        }
        int width = Math.min(wanted, (this.width - 2 * gap - 24) / 2);
        int full = 2 * width + gap;
        GaldurStack column = GaldurStack.vertical(gap);
        column.add(GaldurLabel.of(title), settings -> settings.alignHorizontallyCenter());
        column.add(GaldurLabel.muted(Component.translatable("galdurui.style.hint"), 2 * width + gap),
                settings -> settings.alignHorizontallyCenter());

        GaldurStack scopeRow = column.add(GaldurStack.horizontal(gap));
        scopeRow.add(new GaldurButton(0, 0, height, height, Component.literal("<"), button -> move(-1)));
        GaldurButton scopeButton = scopeRow.add(new GaldurButton(0, 0, full - 2 * (height + gap), height,
                Component.translatable("galdurui.style.scope.label", scopeName(current)), button -> move(1)));
        scopeButton.setTooltip(Tooltip.create(Component.literal(current)));
        scopeRow.add(new GaldurButton(0, 0, height, height, Component.literal(">"), button -> move(1)));

        GaldurStack row = null;
        for (int index = 0; index < elements.length; index++) {
            if (index % 2 == 0) {
                row = column.add(GaldurStack.horizontal(gap));
            }
            Element element = elements[index];
            Boolean locked = locks[index];
            GaldurButton button = row.add(new GaldurButton(0, 0, width, height, label(current, element, locked),
                    pressed -> cycle(current, element)));
            button.setTooltip(Tooltip.create(Component.translatable(locked != null
                    ? "galdurui.style.locked.tooltip" : "galdurui.style.element." + element.id() + ".tooltip")));
            button.active = locked == null;
        }

        GaldurStack bottom = column.add(GaldurStack.horizontal(gap));
        bottom.add(new GaldurButton(0, 0, width, height, Component.translatable("galdurui.style.clear"), button -> {
            GaldurBridge.rules().clearPlayer();
            GaldurBridge.saveRules();
            rebuildWidgets();
        }));
        bottom.add(new GaldurButton(0, 0, width, height, CommonComponents.GUI_DONE, button -> onClose()));

        GaldurPanelLayout panel = new GaldurPanelLayout(column);
        addRenderableOnly(panel);
        panel.visitWidgets(this::addRenderableWidget);
        panel.arrangeElements();
        FrameLayout.centerInRectangle(panel, getRectangle());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        //? if <1.20.2
        //renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        Client.setScreen(parent);
    }

    /** The whole game, the vanilla groups, then each mod and each screen shown since the game started. */
    private void collectScopes() {
        Set<String> found = new LinkedHashSet<>(List.of(StyleRules.ALL, StyleRules.INVENTORIES, StyleRules.MENUS));
        List<String> seen = GaldurBridge.seenScreens();
        for (String screen : seen) {
            if (!screen.startsWith("net.minecraft.") && !screen.startsWith("com.galdurstudio.galdurui.")) {
                found.add(StyleRules.modScope(screen));
            }
        }
        for (String known : GaldurBridge.rules().knownScopes()) {
            if (!known.startsWith("screen:")) {
                found.add(known);
            }
        }
        for (String screen : seen) {
            if (!screen.startsWith("com.galdurstudio.galdurui.")) {
                found.add("screen:" + screen);
            }
        }
        scopes.clear();
        scopes.addAll(found);
        scope = Math.min(scope, scopes.size() - 1);
    }

    private void move(int step) {
        scope = Math.floorMod(scope + step, scopes.size());
        rebuildWidgets();
    }

    /** Inherit, then on, then off, then inherit again. */
    private void cycle(String current, Element element) {
        Boolean value = GaldurBridge.rules().playerValue(current, element);
        Boolean next = value == null ? Boolean.TRUE : value ? Boolean.FALSE : null;
        GaldurBridge.rules().setPlayer(current, element, next);
        GaldurBridge.saveRules();
        rebuildWidgets();
    }

    private static Component label(String current, Element element, @Nullable Boolean locked) {
        Component name = Component.translatable("galdurui.style.element." + element.id());
        Component state;
        if (locked != null) {
            state = Component.translatable("galdurui.style.locked", onOff(locked));
        } else {
            Boolean value = GaldurBridge.rules().playerValue(current, element);
            if (value != null) {
                state = onOff(value);
            } else {
                // What it inherits, when the scope is a screen we can ask about.
                StyleRules.Answer answer = current.startsWith("screen:")
                        ? GaldurBridge.answerFor(current.substring("screen:".length()), element) : null;
                state = answer == null ? Component.translatable("galdurui.style.inherit")
                        : Component.translatable("galdurui.style.inherit.value", onOff(answer.value()));
            }
        }
        return Component.translatable("galdurui.style.entry", name, state);
    }

    private static Component onOff(boolean value) {
        return value ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF;
    }

    private static Component scopeName(String scope) {
        if (scope.equals(StyleRules.ALL)) {
            return Component.translatable("galdurui.style.scope.all");
        }
        if (scope.equals(StyleRules.INVENTORIES)) {
            return Component.translatable("galdurui.style.scope.inventories");
        }
        if (scope.equals(StyleRules.MENUS)) {
            return Component.translatable("galdurui.style.scope.menus");
        }
        String value = scope.substring(scope.indexOf(':') + 1);
        if (scope.startsWith("mod:") || scope.startsWith("modid:")) {
            // com.example.mymod: the last part is what players know.
            value = value.substring(value.lastIndexOf('.') + 1);
        }
        if (scope.startsWith("screen:")) {
            return Component.translatable("galdurui.style.scope.screen", value.substring(value.lastIndexOf('.') + 1));
        }
        return Component.translatable("galdurui.style.scope.mod", value);
    }
}
