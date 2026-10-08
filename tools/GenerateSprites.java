// Équivalent Java de tools/generate_sprites.py, pour les postes sans Python ni Pillow.
// Les deux scripts produisent exactement les mêmes fichiers ; toute retouche de la charte
// doit donc être reportée dans les deux.
//
//     java tools/GenerateSprites.java
//
// (Java 11+ suffit : exécution directe d'un fichier source, sans compilation préalable.)

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.imageio.ImageIO;

public final class GenerateSprites {
    private static final int SIZE = 16;     // côté du sprite, en pixels
    private static final int RADIUS = 4;    // rayon des coins arrondis, égal à la bordure 9-slice
    private static final int BORDER = 4;    // bordure 9-slice

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

    /** Nom du sprite, couleur de remplissage (voir theme.json), relief inversé. */
    private record Sprite(String name, int fill, boolean inset) {}

    private static final Sprite[] SPRITES = {
            new Sprite("panel", 0x2A2420, false),           // background / charbon
            new Sprite("button", 0xB8341F, false),          // accent / redstone
            new Sprite("button_hover", 0xD9573D, false),    // accent_hover
            new Sprite("button_pressed", 0x952A18, true),   // accent_pressed
            new Sprite("button_disabled", 0x352E29, false), // surface
    };

    public static void main(String[] args) throws IOException {
        Path out = Paths.get("src", "main", "resources", "assets", "galdurui", "textures", "gui", "sprites");
        Files.createDirectories(out);
        for (Sprite sprite : SPRITES) {
            Path png = out.resolve(sprite.name() + ".png");
            ImageIO.write(build(sprite), "PNG", png.toFile());
            Files.writeString(png.resolveSibling(sprite.name() + ".png.mcmeta"), MCMETA, StandardCharsets.UTF_8);
            System.out.println("écrit " + png);
        }
    }

    private static BufferedImage build(Sprite sprite) {
        int fill = sprite.fill();
        int outline = scale(fill, 0.45);  // liseré sombre : détache le widget du fond
        int light = scale(fill, 1.22);    // arête éclairée
        int dark = scale(fill, 0.75);     // arête dans l'ombre
        int top = sprite.inset() ? dark : light;
        int bottom = sprite.inset() ? light : dark;

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
