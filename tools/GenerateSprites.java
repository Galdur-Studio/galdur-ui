// Génère les sprites 9-slice de Galdur UI.
//
// Les sprites sont en niveaux de gris : la couleur vient du thème et est appliquée au rendu
// (GaldurRender.drawNineSlice multiplie chaque pixel par la teinte). Un pixel blanc prend
// exactement la couleur du thème, un pixel gris une version assombrie. Modifier theme.json
// suffit donc à recolorer l'interface, sans régénérer les images.
//
//     .\gradlew.bat generateSprites
//
// Chaque sprite est accompagné d'un .png.mcmeta en nine_slice (bordure de 4 px, 2 px pour
// la barre de défilement) : les coins restent nets quelle que soit la taille du widget. Les bords et le centre ne sont
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
    private static final int SIZE_SCROLLBAR = 32;
    private static final int RADIUS = 4;    // rayon des coins arrondis, égal à la bordure 9-slice
    private static final int BORDER = 4;
    private static final int RADIUS_WELL = 3;   // creux (champ, case) : coins un peu plus vifs
    private static final int BORDER_SCROLLBAR = 2;  // la barre ne fait que 4 px de large
    private static final int RADIUS_SCROLLBAR = 2;

    // Niveaux de gris, en fraction du blanc. La teinte ne pouvant qu'assombrir, l'arête
    // éclairée est blanche et le remplissage un peu en dessous, pour garder le relief.
    private static final double LIGHT = 1.0;          // arête éclairée
    private static final double FILL = 0.9;           // remplissage
    private static final double DARK = FILL * 0.75;   // arête dans l'ombre
    private static final double OUTLINE = FILL * 0.45; // liseré sombre : détache le widget du fond

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
     * Forme d'un sprite.
     *
     * <ul>
     *   <li>{@code ROUNDED} : rectangle aux quatre coins arrondis (panneau, bouton, champ...).</li>
     *   <li>{@code TAB} : coins arrondis en haut seulement, et ni liseré ni ombre en bas : l'onglet
     *       s'ouvre sur le trait posé dessous.</li>
     * </ul>
     */
    private enum Shape { ROUNDED, TAB }

    /**
     * Nom du sprite, relief, taille, bordure 9-slice, rayon des coins et forme. La couleur de
     * chaque sprite est choisie au rendu (voir les widgets de api.widget).
     * « inset » inverse le relief : la lumière passe en bas, le widget paraît enfoncé (champ de
     * saisie, case d'objet, rail de défilement).
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
            // Creux : champ de recherche, cadre de liste, rail de la barre de progression.
            new Sprite("field", true, SIZE_BUTTON, BORDER, RADIUS_WELL, Shape.ROUNDED),
            // Case d'objet : mêmes pixels que field, séparée pour qu'un pack puisse la redessiner.
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
            System.out.println("écrit " + png);
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
                    argb = 0;                       // coin taillé : transparent
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
     * Distance du centre du pixel au rectangle intérieur, pour tailler les coins. On ramène le
     * point sur le rectangle central : sur les bords droits la distance vaut l'écart à l'arête,
     * et dans les coins elle vaut la distance au centre du cercle. Pour un onglet
     * ({@code openBottom}), le rectangle central descend jusqu'en bas de l'image : ni coin
     * arrondi ni liseré en bas.
     */
    private static double distanceToRoundedRect(int x, int y, int size, int radius, boolean openBottom) {
        double px = x + 0.5;
        double py = y + 0.5;
        double cx = Math.min(Math.max(px, radius), size - radius);
        double cy = Math.min(Math.max(py, radius), openBottom ? size : size - radius);
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
