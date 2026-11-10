package com.galdurstudio.galdurui.internal.theme;

import com.galdurstudio.galdurui.api.layout.GaldurPanelLayout;
import com.galdurstudio.galdurui.api.layout.GaldurStack;
import com.galdurstudio.galdurui.api.theme.GaldurTheme;
import com.galdurstudio.galdurui.api.widget.GaldurButton;
import com.galdurstudio.galdurui.api.widget.GaldurLabel;
import com.galdurstudio.galdurui.api.widget.GaldurScrollList;
import com.galdurstudio.galdurui.api.widget.GaldurSearchField;
import com.galdurstudio.galdurui.internal.client.Client;
import java.nio.file.Path;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

/**
 * Sharing themes, from the theme editor: name the theme and copy it (to paste in a message; it is
 * also saved in {@code config/galdurui-themes/}), paste a theme copied from a message, or load a
 * saved one. What is imported goes back to the editor, applied but saved only by its "Done".
 */
final class ThemeShareScreen extends Screen {
    private static final int FIELD_WIDTH = 150;
    private static final int LIST_ROWS = 5;

    private final ThemeEditorScreen editor;
    private List<Path> saved = List.of();
    private int selectedSaved = -1;
    @Nullable
    private Component status;

    ThemeShareScreen(ThemeEditorScreen editor) {
        super(Component.translatable("galdurui.theme.share.title"));
        this.editor = editor;
    }

    @Override
    protected void init() {
        saved = PlayerTheme.savedThemes();
        int gap = GaldurTheme.gap();
        GaldurStack column = GaldurStack.vertical(gap);
        column.defaultCellSetting().alignHorizontallyCenter();
        column.add(GaldurLabel.of(title));

        GaldurStack body = column.add(GaldurStack.horizontal(2 * gap));
        GaldurStack mine = body.add(GaldurStack.vertical(gap));
        mine.add(GaldurLabel.muted(Component.translatable("galdurui.theme.share.mine")));
        mine.add(GaldurSearchField.of(FIELD_WIDTH, Component.translatable("galdurui.theme.share.name"),
                () -> editor.name, text -> editor.name = text));
        mine.add(GaldurSearchField.of(FIELD_WIDTH, Component.translatable("galdurui.theme.share.author"),
                () -> editor.author, text -> editor.author = text));
        mine.add(GaldurButton.of(Component.translatable("galdurui.theme.share.copy"), button -> copy()));
        mine.add(GaldurButton.of(Component.translatable("galdurui.theme.share.paste"), button -> paste()));
        if (PlayerTheme.hasBackup()) {
            mine.add(GaldurButton.of(Component.translatable("galdurui.theme.share.backup"), button -> backup()));
        }

        GaldurStack others = body.add(GaldurStack.vertical(gap));
        others.add(GaldurLabel.muted(Component.translatable("galdurui.theme.share.saved")));
        others.add(GaldurScrollList.of(FIELD_WIDTH, LIST_ROWS, () -> saved.stream()
                        .map(path -> (Component) Component.literal(path.getFileName().toString().replaceFirst("\\.json$", "")))
                        .toList(),
                () -> selectedSaved, this::loadSaved));
        others.add(GaldurButton.of(Component.translatable("galdurui.theme.share.folder"),
                button -> Client.openFolder(PlayerTheme.folder())));

        if (status != null) {
            column.add(GaldurLabel.muted(status));
        }
        column.add(GaldurButton.of(CommonComponents.GUI_BACK, button -> onClose()));

        GaldurPanelLayout panel = new GaldurPanelLayout(column);
        addRenderableOnly(panel);
        panel.visitWidgets(this::addRenderableWidget);
        panel.arrangeElements();
        FrameLayout.centerInRectangle(panel, getRectangle());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        //? if <1.20.2
        //renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onFilesDrop(List<Path> files) {
        editor.onFilesDrop(files);
        Client.setScreen(editor);
    }

    @Override
    public void onClose() {
        Client.setScreen(editor);
    }

    private void copy() {
        PlayerTheme.Theme theme = editor.editedTheme();
        Path file = PlayerTheme.export(theme);
        show(file == null ? Component.translatable("galdurui.theme.share.copied")
                : Component.translatable("galdurui.theme.share.copied_file", file.getFileName().toString()));
    }

    private void paste() {
        PlayerTheme.Theme theme = PlayerTheme.parse(Client.clipboard());
        if (theme == null) {
            show(Component.translatable("galdurui.theme.import_failed"));
            return;
        }
        editor.load(theme);
        editor.showStatus(Component.translatable("galdurui.theme.imported", ThemeEditorScreen.displayName(theme)));
        Client.setScreen(editor);
    }

    private void loadSaved(int index) {
        selectedSaved = index;
        PlayerTheme.Theme theme = index < 0 || index >= saved.size() ? null : PlayerTheme.readFile(saved.get(index));
        if (theme == null) {
            show(Component.translatable("galdurui.theme.import_failed"));
            return;
        }
        editor.load(theme);
        editor.showStatus(Component.translatable("galdurui.theme.imported", ThemeEditorScreen.displayName(theme)));
        Client.setScreen(editor);
    }

    private void backup() {
        PlayerTheme.restoreBackup();
        editor.load(PlayerTheme.current());
        editor.showStatus(Component.translatable("galdurui.theme.share.backup.done"));
        Client.setScreen(editor);
    }

    private void show(Component message) {
        status = message;
        Client.later(() -> {
            if (Client.screen() == this) {
                rebuildWidgets();
            }
        });
    }
}
