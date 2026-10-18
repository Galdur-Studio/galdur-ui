// Generates the Galdur UI 9-slice sprites.
//
// Sprites are grayscale: the colour comes from the theme and is applied at render time
// (GaldurRender.drawNineSlice multiplies each pixel by the tint). A white pixel takes exactly
// the theme colour, a gray pixel a darker version. Editing theme.json is therefore enough to
// recolour the interface, without regenerating the images.
//
//     .\gradlew.bat generateSprites
//
// Each sprite comes with a nine_slice .png.mcmeta (4 px border, 2 px for the scrollbar): corners
// stay sharp whatever the widget size. Edges and centre are NOT stretched in 1.21.1: they are
// repeated in tiles of (size - 2 * border), one quad per tile (GaldurRender batches them into a
// single draw call). Hence large sprites (panel 256, buttons 64) to keep the quad count low;
// edges and centre must stay uniform so the repetition does not show.
//
// This file lives outside src/main (Gradle "tools" source set): it does not end up in the jar.

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.imageio.ImageIO;

public final class GenerateSprites {
    private static final int SIZE_BUTTON = 64;     // sprite side, in pixels
    private static final int SIZE_PANEL = 256;
    private static final int SIZE_SCROLLBAR = 32;
    private static final int RADIUS = 4;    // corner radius, equal to the 9-slice border
    private static final int BORDER = 4;
    private static final int RADIUS_WELL = 3;   // sunken (field, slot): slightly sharper corners
    private static final int BORDER_SCROLLBAR = 2;  // the scrollbar is only 4 px wide
    private static final int RADIUS_SCROLLBAR = 2;

    // Gray levels, as a fraction of white. Since the tint can only darken, the lit edge is
    // white and the fill a bit below it, to keep the relief.
    private static final double LIGHT = 1.0;          // lit edge
    private static final double FILL = 0.9;           // fill
    private static final double DARK = FILL * 0.75;   // shadowed edge
    private static final double OUTLINE = FILL * 0.45; // dark outline: separates the widget from the background

    private static final Path OUT = Paths.get("src", "main", "resources", "assets", "galdurui",
            "textures", "gui", "sprites");

    private static String mcmeta(int size, int border) {
        return """
            {
              "gui": {
                "scaling": {
                  "type": "nine_slice",
                  "width": %d,
                  "height": %d,
                  "border": %d
                }
              }
            }
            """.formatted(size, size, border);
    }

    /**
     * Shape of a sprite.
     *
     * <ul>
     *   <li>{@code ROUNDED}: rectangle with four rounded corners (panel, button, field...).</li>
     *   <li>{@code TAB}: rounded corners at the top only, and no outline or shadow at the bottom:
     *       the tab opens onto the line drawn below it.</li>
     * </ul>
     */
    private enum Shape { ROUNDED, TAB }

    /**
     * Sprite name, relief, size, 9-slice border, corner radius and shape. Each sprite's colour is
     * chosen at render time (see the widgets of api.widget). "inset" inverts the relief: light
     * comes from the bottom and the widget looks pressed in (text field, item slot, scroll track).
     */
    private record Sprite(String name, boolean inset, int size, int border, int radius, Shape shape) {
        Sprite(String name, boolean inset, int size) {
            this(name, inset, size, BORDER, RADIUS, Shape.ROUNDED);
        }
    }

    private static final Sprite[] SPRITES = {
            new Sprite("panel", false, SIZE_PANEL),
            new Sprite("button", false, SIZE_BUTTON),
            new Sprite("button_hover", false, SIZE_BUTTON),
            new Sprite("button_pressed", true,  SIZE_BUTTON),
            new Sprite("button_disabled", false,  SIZE_BUTTON),
            // Sunken: search field, list frame, progress bar track.
            new Sprite("field", true, SIZE_BUTTON, BORDER, RADIUS_WELL, Shape.ROUNDED),
            // Item slot: same pixels as field, separate so a pack can redraw it.
            new Sprite("slot", true, SIZE_BUTTON, BORDER, RADIUS_WELL, Shape.ROUNDED),
            new Sprite("tab", false, SIZE_BUTTON, BORDER, RADIUS, Shape.TAB),
            new Sprite("tab_selected", false, SIZE_BUTTON, BORDER, RADIUS, Shape.TAB),
            new Sprite("scrollbar_track", true, SIZE_SCROLLBAR, BORDER_SCROLLBAR, RADIUS_SCROLLBAR, Shape.ROUNDED),
            new Sprite("scrollbar_thumb", false, SIZE_SCROLLBAR, BORDER_SCROLLBAR, RADIUS_SCROLLBAR, Shape.ROUNDED),
    };

    public static void main(String[] args) throws IOException {
        Files.createDirectories(OUT);
        for (Sprite sprite : SPRITES) {
            Path png = OUT.resolve(sprite.name() + ".png");
            ImageIO.write(build(sprite), "PNG", png.toFile());
            Files.writeString(png.resolveSibling(sprite.name() + ".png.mcmeta"),
                    mcmeta(sprite.size(), sprite.border()), StandardCharsets.UTF_8);
            System.out.println("wrote " + png);
        }
    }

    private static BufferedImage build(Sprite sprite) {
        boolean inset = sprite.inset();
        int size = sprite.size();
        int radius = sprite.radius();
        boolean tab = sprite.shape() == Shape.TAB;
        int fill = gray(FILL);
        int outline = gray(OUTLINE);
        int light = gray(LIGHT);
        int dark = gray(DARK);
        int top = inset ? dark : light;
        int bottom = inset ? light : dark;

        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                double d = distanceToRoundedRect(x, y, size, radius, tab);
                int argb;
                if (d > radius) {
                    argb = 0;                       // cut corner: transparent
                } else if (d > radius - 1.0) {
                    argb = 0xFF000000 | outline;
                } else if (y == 1) {
                    argb = 0xFF000000 | top;
                } else if (y == size - 2 && !tab) {
                    argb = 0xFF000000 | bottom;
                } else {
                    argb = 0xFF000000 | fill;
                }
                image.setRGB(x, y, argb);
            }
        }
        return image;
    }

    /**
     * Distance from the pixel centre to the inner rectangle, to cut the corners. The point is
     * clamped onto the central rectangle: on straight edges the distance is the gap to the edge,
     * and in the corners it is the distance to the circle centre. For a tab ({@code openBottom}),
     * the central rectangle reaches the bottom of the image: no rounded corner or outline at the
     * bottom.
     */
    private static double distanceToRoundedRect(int x, int y, int size, int radius, boolean openBottom) {
        double px = x + 0.5;
        double py = y + 0.5;
        double cx = Math.min(Math.max(px, radius), size - radius);
        double cy = Math.min(Math.max(py, radius), openBottom ? size : size - radius);
        return Math.hypot(px - cx, py - cy);
    }

    /** {@return the RGB gray of intensity {@code level} (0 = black, 1 = white)} */
    private static int gray(double level) {
        int v = Math.max(0, Math.min(255, (int) Math.round(level * 255)));
        return (v << 16) | (v << 8) | v;
    }

    private GenerateSprites() {
    }
}
