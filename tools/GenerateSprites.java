// Génère les sprites 9-slice de Galdur UI.
//
// Les sprites sont en niveaux de gris : la couleur vient du thème et est appliquée au rendu
// (GaldurRender.drawNineSlice multiplie chaque pixel par la teinte). Un pixel blanc prend
// exactement la couleur du thème, un pixel gris une version assombrie. Modifier theme.json
// suffit donc à recolorer l'interface, sans régénérer les images.
//
//     .\gradlew.bat generateSprites
//
// Chaque sprite est accompagné d'un .png.mcmeta en nine_slice (bordure de 4 px) : les
// coins restent nets quelle que soit la taille du widget. Les bords et le centre ne sont
// PAS étirés en 1.21.1 : ils sont répétés par tuiles de (taille - 2 * bordure), à raison d'un
// quad (donc d'un appel de dessin) par tuile. D'où des sprites grands (panel 256, boutons 64)
// pour limiter les quads ; bords et centre doivent rester uniformes pour que la répétition
// ne se voie pas.
//
// Ce fichier vit hors de src/main (source set Gradle « tools ») : il ne finit pas dans le jar.

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.imageio.ImageIO;

public final class GenerateSprites {
    private static final int SIZE_BUTTON = 64;     // côté du sprite, en pixels
    private static final int SIZE_PANEL = 256;
    private static final int RADIUS = 4;    // rayon des coins arrondis, égal à la bordure 9-slice
    private static final int BORDER = 4;

    // Niveaux de gris, en fraction du blanc. La teinte ne pouvant qu'assombrir, l'arête
    // éclairée est blanche et le remplissage un peu en dessous, pour garder le relief.
    private static final double LIGHT = 1.0;          // arête éclairée
    private static final double FILL = 0.9;           // remplissage
    private static final double DARK = FILL * 0.75;   // arête dans l'ombre
    private static final double OUTLINE = FILL * 0.45; // liseré sombre : détache le widget du fond

    private static final Path OUT = Paths.get("src", "main", "resources", "assets", "galdurui",
            "textures", "gui", "sprites");

    private static String mcmeta(int size) {
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
            """.formatted(size, size, BORDER);
    }

    /**
     * Nom du sprite et relief inversé. La couleur de chaque sprite est choisie au rendu
     * (voir GaldurPanel et GaldurButton).
     * « inset » inverse le relief : la lumière passe en bas, le widget paraît enfoncé.
     */
    private record Sprite(String name, boolean inset, int size) {}

    private static final Sprite[] SPRITES = {
            new Sprite("panel", false, SIZE_PANEL),
            new Sprite("button", false, SIZE_BUTTON),
            new Sprite("button_hover", false, SIZE_BUTTON),
            new Sprite("button_pressed", true,  SIZE_BUTTON),
            new Sprite("button_disabled", false,  SIZE_BUTTON),
    };

    public static void main(String[] args) throws IOException {
        Files.createDirectories(OUT);
        for (Sprite sprite : SPRITES) {
            Path png = OUT.resolve(sprite.name() + ".png");
            ImageIO.write(build(sprite.inset(), sprite.size()), "PNG", png.toFile());
            Files.writeString(png.resolveSibling(sprite.name() + ".png.mcmeta"), mcmeta(sprite.size()), StandardCharsets.UTF_8);
            System.out.println("écrit " + png);
        }
    }

    private static BufferedImage build(boolean inset, int size) {
        int fill = gray(FILL);
        int outline = gray(OUTLINE);
        int light = gray(LIGHT);
        int dark = gray(DARK);
        int top = inset ? dark : light;
        int bottom = inset ? light : dark;

        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                double d = distanceToRoundedRect(x, y, size);
                int argb;
                if (d > RADIUS) {
                    argb = 0;                       // coin taillé : transparent
                } else if (d > RADIUS - 1.0) {
                    argb = 0xFF000000 | outline;
                } else if (y == 1) {
                    argb = 0xFF000000 | top;
                } else if (y == size - 2) {
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
    private static double distanceToRoundedRect(int x, int y, int size) {
        double px = x + 0.5;
        double py = y + 0.5;
        double cx = Math.min(Math.max(px, RADIUS), size - RADIUS);
        double cy = Math.min(Math.max(py, RADIUS), size - RADIUS);
        return Math.hypot(px - cx, py - cy);
    }

    /** {@return le gris RGB d'intensité {@code level} (0 = noir, 1 = blanc)} */
    private static int gray(double level) {
        int v = Math.max(0, Math.min(255, (int) Math.round(level * 255)));
        return (v << 16) | (v << 8) | v;
    }

    private GenerateSprites() {
    }
}
