package com.galdurstudio.galdurui.internal.theme;

import com.galdurstudio.galdurui.api.layout.GaldurPanelLayout;
import com.galdurstudio.galdurui.api.layout.GaldurStack;
import com.galdurstudio.galdurui.api.theme.GaldurPaint;
import com.galdurstudio.galdurui.api.theme.GaldurTheme;
import com.galdurstudio.galdurui.api.widget.GaldurButton;
import com.galdurstudio.galdurui.api.widget.GaldurCheckbox;
import com.galdurstudio.galdurui.api.widget.GaldurLabel;
import com.galdurstudio.galdurui.api.widget.GaldurProgressBar;
import com.galdurstudio.galdurui.api.widget.GaldurSearchField;
import com.galdurstudio.galdurui.internal.client.Client;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

/**
 * Theme editor. The player picks a colour of the theme, from the base palette or one element
 * (buttons, fields, slots...), and changes it with the picker or a hex code, as one colour or a
 * gradient. Every Galdur UI screen, this one included, updates right away. "Done" saves to
 * {@code config/galdurui-theme.json}; "Cancel" and Escape restore the theme from when the editor
 * opened. "Galdur theme" goes back to the studio's theme in one click, and "Share" exports and
 * imports themes. A theme file dropped on the window is imported too.
 */
public final class ThemeEditorScreen extends Screen {
    /** Groups of the colour list, each short enough for a small window. */
    private static final List<Group> GROUPS = List.of(
            new Group("base", GaldurTheme.BASE_COLORS),
            new Group("buttons", List.of(GaldurTheme.BUTTON, GaldurTheme.BUTTON_HOVER, GaldurTheme.BUTTON_PRESSED,
                    GaldurTheme.BUTTON_DISABLED, GaldurTheme.BUTTON_TEXT, GaldurTheme.OUTLINE)),
            new Group("fields", List.of(GaldurTheme.FIELD, GaldurTheme.FIELD_TEXT, GaldurTheme.CHECKBOX,
                    GaldurTheme.CHECKBOX_MARK, GaldurTheme.SLIDER, GaldurTheme.SLIDER_HANDLE)),
            new Group("panels", List.of(GaldurTheme.PANEL, GaldurTheme.TITLE, GaldurTheme.SLOT, GaldurTheme.SLOT_HOVER,
                    GaldurTheme.LIST, GaldurTheme.LIST_SELECTED, GaldurTheme.SCROLLBAR)),
            new Group("bars", List.of(GaldurTheme.TAB, GaldurTheme.TAB_SELECTED, GaldurTheme.PROGRESS,
                    GaldurTheme.PROGRESS_TRACK)));
    private static final int MAX_STOPS = 4;
    private static final int HEX_WIDTH = 72;
    private static final int SIDE_WIDTH = 96;

    private record Group(String id, List<String> keys) {
    }

    @Nullable
    private final Screen parent;
    /** The player's theme when the editor opened, put back by "Cancel". */
    private final PlayerTheme.Theme before;
    /** The player's paints being edited, applied live. */
    private final Map<String, GaldurPaint> edited;
    private Map<String, Integer> spacing;
    String name;
    String author;

    private int group;
    private String selected = GaldurTheme.ACCENT;
    /** Colour of the selected paint being changed, for a gradient. */
    private int stop;
    /** Text of the hex field, which may be incomplete while the player types. */
    private String hexText = "";
    private boolean previewChecked = true;
    @Nullable
    private Component status;

    private ColorPicker picker;

    public ThemeEditorScreen(@Nullable Screen parent) {
        super(Component.translatable("galdurui.theme.title"));
        this.parent = parent;
        this.before = PlayerTheme.current();
        this.edited = new LinkedHashMap<>(before.paints());
        this.spacing = before.spacing();
        this.name = before.name();
        this.author = before.author();
    }

    @Override
    protected void init() {
        int gap = GaldurTheme.gap();
        GaldurStack column = GaldurStack.vertical(gap);
        column.defaultCellSetting().alignHorizontallyCenter();
        column.add(GaldurLabel.of(title));

        GaldurStack body = column.add(GaldurStack.horizontal(2 * gap));

        // Left: the colours of a group.
        GaldurStack entries = body.add(GaldurStack.vertical(2));
        Group current = GROUPS.get(group);
        entries.add(GaldurButton.of(Component.translatable("galdurui.theme.group",
                Component.translatable("galdurui.theme.group." + current.id())), button -> {
            group = (group + 1) % GROUPS.size();
            select(GROUPS.get(group).keys().get(0));
            rebuildLater();
        }));
        for (String key : current.keys()) {
            entries.add(new ColorEntry(Component.translatable("galdurui.theme.color." + key),
                    () -> GaldurTheme.paint(key), () -> edited.containsKey(key), () -> key.equals(selected),
                    () -> {
                        select(key);
                        rebuildLater();
                    }));
        }

        // Middle: the picker, the hex code and the gradient.
        GaldurPaint paint = GaldurTheme.paint(selected);
        GaldurStack editor = body.add(GaldurStack.vertical(gap));
        picker = editor.add(new ColorPicker(this::pick));
        picker.setColor(paint.colors().get(Math.min(stop, paint.colors().size() - 1)));
        GaldurStack hexRow = editor.add(GaldurStack.horizontal(gap));
        hexRow.add(GaldurSearchField.of(HEX_WIDTH, Component.literal("#RRGGBB"), () -> hexText, this::typeHex));
        hexRow.add(GaldurButton.of(Component.translatable("galdurui.theme.reset"), button -> resetSelected()));
        GaldurStack gradientRow = editor.add(GaldurStack.horizontal(gap));
        gradientRow.add(GaldurButton.of(Component.translatable(paint.isSolid() ? "galdurui.theme.solid" : "galdurui.theme.gradient"),
                button -> toggleGradient()));
        if (!paint.isSolid()) {
            gradientRow.add(GaldurButton.of(Component.translatable(paint.direction() == GaldurPaint.Direction.VERTICAL
                    ? "galdurui.theme.vertical" : "galdurui.theme.horizontal"), button -> toggleDirection()));
            GaldurStack stops = editor.add(GaldurStack.horizontal(gap));
            stops.add(GaldurButton.of(Component.translatable("galdurui.theme.stop", stop + 1, paint.colors().size()),
                    button -> {
                        stop = (stop + 1) % GaldurTheme.paint(selected).colors().size();
                        select(selected);
                        rebuildLater();
                    }));
            stops.add(GaldurButton.of(Component.literal("+"), button -> addStop()));
            stops.add(GaldurButton.of(Component.literal("−"), button -> removeStop()));
        }

        // Right: preview and actions.
        GaldurStack side = body.add(GaldurStack.vertical(gap));
        side.add(GaldurLabel.muted(Component.translatable("galdurui.theme.preview")));
        side.add(GaldurButton.of(Component.translatable("galdurui.theme.preview.button"), button -> {
        }));
        side.add(GaldurCheckbox.of(Component.translatable("galdurui.theme.preview.checkbox"),
                () -> previewChecked, value -> previewChecked = value));
        side.add(GaldurProgressBar.of(SIDE_WIDTH, () -> 0.6));
        side.add(GaldurButton.of(CommonComponents.GUI_DONE, button -> done()));
        side.add(GaldurButton.of(CommonComponents.GUI_CANCEL, button -> onClose()));
        side.add(GaldurButton.of(Component.translatable("galdurui.theme.galdur"), button -> galdurTheme()));
        side.add(GaldurButton.of(Component.translatable("galdurui.theme.share"),
                button -> Client.setScreen(new ThemeShareScreen(this))));
        if (status != null) {
            column.add(GaldurLabel.muted(status));
        }

        GaldurPanelLayout panel = new GaldurPanelLayout(column);
        addRenderableOnly(panel);
        panel.visitWidgets(this::addRenderableWidget);
        panel.arrangeElements();
        FrameLayout.centerInRectangle(panel, getRectangle());
        hexText = PlayerTheme.hex(paint.colors().get(Math.min(stop, paint.colors().size() - 1)));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        //? if <1.20.2
        //renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    /** A theme file dropped on the window is loaded in the editor, to check before "Done". */
    @Override
    public void onFilesDrop(List<Path> files) {
        for (Path file : files) {
            PlayerTheme.Theme theme = PlayerTheme.readFile(file);
            if (theme != null) {
                load(theme);
                showStatus(Component.translatable("galdurui.theme.imported", displayName(theme)));
                return;
            }
        }
        showStatus(Component.translatable("galdurui.theme.import_failed"));
    }

    // --- used by the share screen

    /** {@return the share screen of this editor} */
    public Screen shareScreen() {
        return new ThemeShareScreen(this);
    }

    /** {@return the theme as it is in the editor} */
    PlayerTheme.Theme editedTheme() {
        return new PlayerTheme.Theme(edited, spacing, name, author);
    }

    /** Replaces the theme being edited (import), applied at once but saved only by "Done". */
    void load(PlayerTheme.Theme theme) {
        edited.clear();
        edited.putAll(theme.paints());
        spacing = theme.spacing();
        name = theme.name();
        author = theme.author();
        applyLive();
        stop = 0;
        select(selected);
    }

    void showStatus(Component message) {
        status = message;
        rebuildLater();
    }

    static Component displayName(PlayerTheme.Theme theme) {
        return theme.name().isEmpty() ? Component.translatable("galdurui.theme.unnamed") : Component.literal(theme.name());
    }

    // --- editing

    private void select(String key) {
        if (!key.equals(selected)) {
            stop = 0;
        }
        selected = key;
        GaldurPaint paint = GaldurTheme.paint(key);
        int color = paint.colors().get(Math.min(stop, paint.colors().size() - 1));
        if (picker != null) {
            picker.setColor(color);
        }
        hexText = PlayerTheme.hex(color);
    }

    /** The picker moved: the colour applies at once. */
    private void pick(int argb) {
        setStop(argb);
        hexText = PlayerTheme.hex(argb);
    }

    /** The hex field changed: a complete code applies at once, an incomplete one waits. */
    private void typeHex(String text) {
        // The field also reports the text the editor put in it: only the player's typing counts.
        if (text.equals(hexText)) {
            return;
        }
        hexText = text;
        Integer argb = PlayerTheme.parseHex(text);
        if (argb != null) {
            setStop(argb);
            picker.setColor(argb);
        }
    }

    private void setStop(int argb) {
        GaldurPaint paint = GaldurTheme.paint(selected);
        edited.put(selected, paint.withColor(Math.min(stop, paint.colors().size() - 1), argb));
        applyLive();
    }

    private void toggleGradient() {
        GaldurPaint paint = GaldurTheme.paint(selected);
        if (paint.isSolid()) {
            // A gradient from the colour to a slightly darker one, to start from.
            edited.put(selected, GaldurPaint.gradient(GaldurPaint.Direction.VERTICAL, paint.first(), darker(paint.first())));
        } else {
            edited.put(selected, GaldurPaint.solid(paint.first()));
        }
        stop = 0;
        applyLive();
        select(selected);
        rebuildLater();
    }

    private void toggleDirection() {
        GaldurPaint paint = GaldurTheme.paint(selected);
        GaldurPaint.Direction other = paint.direction() == GaldurPaint.Direction.VERTICAL
                ? GaldurPaint.Direction.HORIZONTAL : GaldurPaint.Direction.VERTICAL;
        edited.put(selected, new GaldurPaint(paint.colors(), other));
        applyLive();
        rebuildLater();
    }

    private void addStop() {
        GaldurPaint paint = GaldurTheme.paint(selected);
        if (paint.colors().size() >= MAX_STOPS) {
            return;
        }
        List<Integer> colors = new ArrayList<>(paint.colors());
        colors.add(stop + 1, colors.get(stop));
        edited.put(selected, new GaldurPaint(colors, paint.direction()));
        stop++;
        applyLive();
        select(selected);
        rebuildLater();
    }

    private void removeStop() {
        GaldurPaint paint = GaldurTheme.paint(selected);
        if (paint.colors().size() <= 2) {
            return;
        }
        List<Integer> colors = new ArrayList<>(paint.colors());
        colors.remove(stop);
        edited.put(selected, new GaldurPaint(colors, paint.direction()));
        stop = Math.max(0, stop - 1);
        applyLive();
        select(selected);
        rebuildLater();
    }

    private void resetSelected() {
        edited.remove(selected);
        stop = 0;
        applyLive();
        select(selected);
        rebuildLater();
    }

    /** Back to the studio's theme: nothing of the player's left. Undone by "Cancel" until "Done". */
    private void galdurTheme() {
        load(PlayerTheme.Theme.EMPTY);
        showStatus(Component.translatable("galdurui.theme.galdur.done"));
    }

    private void applyLive() {
        ThemeStore.setPlayerTheme(edited, spacing);
    }

    private void done() {
        PlayerTheme.save(editedTheme());
        Client.setScreen(parent);
    }

    /** Escape and "Cancel": the theme goes back to what it was when the editor opened. */
    @Override
    public void onClose() {
        ThemeStore.setPlayerTheme(before.paints(), before.spacing());
        Client.setScreen(parent);
    }

    /** Builds the screen again on the next loop: we may be inside a click on one of its widgets. */
    private void rebuildLater() {
        Client.later(() -> {
            if (Client.screen() == this) {
                rebuildWidgets();
            }
        });
    }

    private static int darker(int argb) {
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;
        return (argb & 0xFF000000) | (r * 2 / 3) << 16 | (g * 2 / 3) << 8 | (b * 2 / 3);
    }
}
