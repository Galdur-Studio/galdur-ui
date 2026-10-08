// Génère les sprites 9-slice de Galdur UI.
//
// Les PNG du dépôt sont volontairement minuscules (16x16) et entièrement décrits par ce
// programme : pour retoucher la charte, on modifie assets/galdurui/theme.json et on
// régénère, plutôt que d'éditer les images à la main.
//
//     .\gradlew.bat generateSprites
//
// Chaque sprite est accompagné d'un .png.mcmeta en nine_slice (bordure de 4 px) : les
// coins restent nets quelle que soit la taille du widget, seuls les bords et le centre
// sont étirés.
//
// Ce fichier vit hors de src/main (source set Gradle « tools ») : il ne finit pas dans le jar.

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

public final class GenerateSprites {
    private static final int SIZE = 16;     // côté du sprite, en pixels
    private static final int RADIUS = 4;    // rayon des coins arrondis, égal à la bordure 9-slice
    private static final int BORDER = 4;    // bordure 9-slice

    private static final Path ASSETS = Paths.get("src", "main", "resources", "assets", "galdurui");

    private static final String MCMETA = """
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
            """.formatted(SIZE, SIZE, BORDER);

    /**
     * Nom du sprite, clé de sa couleur de remplissage dans theme.json, relief inversé.
     * « inset » inverse le relief : la lumière passe en bas, le widget paraît enfoncé.
     */
    private record Sprite(String name, String themeKey, boolean inset) {}

    private static final Sprite[] SPRITES = {
            new Sprite("panel", "background", false),          // charbon
            new Sprite("button", "accent", false),             // redstone
            new Sprite("button_hover", "accent_hover", false),
            new Sprite("button_pressed", "accent_pressed", true),
            new Sprite("button_disabled", "surface", false),
    };

    public static void main(String[] args) throws IOException {
        String theme = Files.readString(ASSETS.resolve("theme.json"), StandardCharsets.UTF_8);
        Path out = ASSETS.resolve(Paths.get("textures", "gui", "sprites"));
        Files.createDirectories(out);
        for (Sprite sprite : SPRITES) {
            Path png = out.resolve(sprite.name() + ".png");
            ImageIO.write(build(themeColor(theme, sprite.themeKey()), sprite.inset()), "PNG", png.toFile());
            Files.writeString(png.resolveSibling(sprite.name() + ".png.mcmeta"), MCMETA, StandardCharsets.UTF_8);
            System.out.println("écrit " + png);
        }
    }

    /** Lit la couleur {@code "key": "#RRGGBB"} de theme.json (format plat, pas besoin d'un parseur JSON). */
    private static int themeColor(String theme, String key) {
        Matcher matcher = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"#([0-9A-Fa-f]{6})\"").matcher(theme);
        if (!matcher.find()) {
            throw new IllegalStateException("couleur \"" + key + "\" introuvable dans theme.json");
        }
        return Integer.parseInt(matcher.group(1), 16);
    }

    private static BufferedImage build(int fill, boolean inset) {
        int outline = scale(fill, 0.45);  // liseré sombre : détache le widget du fond
        int light = scale(fill, 1.22);    // arête éclairée
        int dark = scale(fill, 0.75);     // arête dans l'ombre
        int top = inset ? dark : light;
        int bottom = inset ? light : dark;

        BufferedImage image = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                double d = distanceToRoundedRect(x, y);
                int argb;
                if (d > RADIUS) {
                    argb = 0;                       // coin taillé : transparent
                } else if (d > RADIUS - 1.0) {
                    argb = 0xFF000000 | outline;
                } else if (y == 1) {
                    argb = 0xFF000000 | top;
                } else if (y == SIZE - 2) {
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
     * Distance du centre du pixel au rectangle intérieur, pour tailler les coins. On ramène le
     * point sur le rectangle central : sur les bords droits la distance vaut l'écart à l'arête,
     * et dans les coins elle vaut la distance au centre du cercle.
     */
    private static double distanceToRoundedRect(int x, int y) {
        double px = x + 0.5;
        double py = y + 0.5;
        double cx = Math.min(Math.max(px, RADIUS), SIZE - RADIUS);
        double cy = Math.min(Math.max(py, RADIUS), SIZE - RADIUS);
        return Math.hypot(px - cx, py - cy);
    }

    /** Éclaircit (facteur > 1) ou assombrit (facteur < 1) une couleur RGB, en restant dans [0, 255]. */
    private static int scale(int rgb, double factor) {
        int r = clamp((int) Math.round(((rgb >> 16) & 0xFF) * factor));
        int g = clamp((int) Math.round(((rgb >> 8) & 0xFF) * factor));
        int b = clamp((int) Math.round((rgb & 0xFF) * factor));
        return (r << 16) | (g << 8) | b;
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }

    private GenerateSprites() {
    }
}
