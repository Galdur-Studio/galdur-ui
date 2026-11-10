package com.galdurstudio.galdurui.internal.render;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

//? if >=26.1 {
//?} elif >=1.20.2 {
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiSpriteManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling;
//?} else {
/*import com.galdurstudio.galdurui.GaldurUI;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.GsonHelper;
*///?}

/**
 * Finds a GUI sprite ({@code galdurui:panel}, a mod's own sprite...) for {@link GaldurRender}:
 * the texture to bind, the sprite's area in it, and its 9-slice data.
 *
 * <p>The only class that knows where GUI sprites come from, which depends on the Minecraft
 * version. Since 1.20.2 they are in Minecraft's GUI atlas, with the 9-slice data read from their
 * {@code .png.mcmeta}. Minecraft 1.20.1 has no GUI atlas: the sprite is then its own texture,
 * {@code textures/gui/sprites/<name>.png}, and the {@code .png.mcmeta} is read here, with the
 * same format. Both are the files resource packs already override.</p>
 *
 * <p>Since 26.1 {@link GaldurRender} gives the sprite ids to Minecraft directly, and this class
 * is no longer used.</p>
 */
public final class GuiSprites {
    private GuiSprites() {
    }

    /**
     * A sprite ready to draw.
     *
     * @param texture   texture to bind
     * @param u0        left edge of the sprite in the texture (0 to 1)
     * @param v0        top edge
     * @param u1        right edge
     * @param v1        bottom edge
     * @param nineSlice 9-slice data, {@code null} if the sprite is not 9-slice
     */
    public record Sprite(ResourceLocation texture, float u0, float v0, float u1, float v1, @Nullable NineSlice nineSlice) {
        /** {@return the texture u at a fraction (0 to 1) of the sprite width} */
        public float u(float fraction) {
            return u0 + (u1 - u0) * fraction;
        }

        /** {@return the texture v at a fraction (0 to 1) of the sprite height} */
        public float v(float fraction) {
            return v0 + (v1 - v0) * fraction;
        }
    }

    /**
     * 9-slice data of a sprite, as in its {@code .png.mcmeta}.
     *
     * @param width  sprite width, in pixels
     * @param height sprite height, in pixels
     */
    public record NineSlice(int width, int height, int left, int top, int right, int bottom) {
    }

    //? if >=26.1 {
    /*/^* Nothing to do: since 26.1 {@link GaldurRender} hands sprites to Minecraft, which loads them. ^/
    public static void clearCache() {
    }
    *///?} elif >=1.20.2 {
    /**
     * @param id sprite id, {@code galdurui:panel} for {@code assets/galdurui/textures/gui/sprites/panel.png}
     * @return the sprite (Minecraft's "missing" sprite if there is none)
     */
    public static Sprite get(ResourceLocation id) {
        GuiSpriteManager sprites = Minecraft.getInstance().getGuiSprites();
        TextureAtlasSprite sprite = sprites.getSprite(id);
        NineSlice nineSlice = null;
        if (sprites.getSpriteScaling(sprite) instanceof GuiSpriteScaling.NineSlice scaling) {
            GuiSpriteScaling.NineSlice.Border border = scaling.border();
            nineSlice = new NineSlice(scaling.width(), scaling.height(),
                    border.left(), border.top(), border.right(), border.bottom());
        }
        return new Sprite(sprite.atlasLocation(), sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1(), nineSlice);
    }

    /** Nothing to do: Minecraft reloads its GUI atlas itself. */
    public static void clearCache() {
    }
    //?} else {
    /*private static final Map<ResourceLocation, Sprite> CACHE = new HashMap<>();

    /^*
     * @param id sprite id, {@code galdurui:panel} for {@code assets/galdurui/textures/gui/sprites/panel.png}
     * @return the sprite; without a valid {@code .png.mcmeta}, a sprite stretched over the area
     ^/
    public static Sprite get(ResourceLocation id) {
        return CACHE.computeIfAbsent(id, GuiSprites::load);
    }

    /^* Forgets the sprites read so far, so a resource reload (pack change, F3+T) takes effect. ^/
    public static void clearCache() {
        CACHE.clear();
    }

    private static Sprite load(ResourceLocation id) {
        ResourceLocation texture = id.withPath(path -> "textures/gui/sprites/" + path + ".png");
        return new Sprite(texture, 0, 0, 1, 1, readNineSlice(texture.withSuffix(".mcmeta")));
    }

    @Nullable
    private static NineSlice readNineSlice(ResourceLocation metadata) {
        Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(metadata);
        if (resource.isEmpty()) {
            return null;
        }
        try (Reader reader = resource.get().openAsReader()) {
            JsonObject scaling = GsonHelper.getAsJsonObject(GsonHelper.getAsJsonObject(GsonHelper.parse(reader), "gui"), "scaling");
            if (!"nine_slice".equals(GsonHelper.getAsString(scaling, "type"))) {
                return null;
            }
            int width = GsonHelper.getAsInt(scaling, "width");
            int height = GsonHelper.getAsInt(scaling, "height");
            JsonElement border = scaling.get("border");
            if (border != null && border.isJsonObject()) {
                JsonObject sides = border.getAsJsonObject();
                return new NineSlice(width, height, GsonHelper.getAsInt(sides, "left"), GsonHelper.getAsInt(sides, "top"),
                        GsonHelper.getAsInt(sides, "right"), GsonHelper.getAsInt(sides, "bottom"));
            }
            int all = GsonHelper.getAsInt(scaling, "border");
            return new NineSlice(width, height, all, all, all, all);
        } catch (Exception exception) {
            GaldurUI.LOGGER.warn("Invalid GUI sprite metadata {}: the sprite is stretched.", metadata, exception);
            return null;
        }
    }
    *///?}
}
