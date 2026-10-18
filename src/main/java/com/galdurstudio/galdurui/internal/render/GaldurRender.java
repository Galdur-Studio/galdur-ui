package com.galdurstudio.galdurui.internal.render;

import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.gui.GuiSpriteManager;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Couche de dessin de Galdur UI.
 *
 * <p>C'est la <strong>seule</strong> classe de la bibliothèque qui appelle {@link GuiGraphics}.
 * Les widgets de {@code api.widget} décrivent ce qu'ils veulent voir (un sprite étiré ici, une
 * ligne de texte là) et laissent cette classe s'occuper de l'API de rendu de la version du jeu.
 * Le jour où ce rendu change — il a changé en 1.21.6 — seul ce fichier sera à réécrire.</p>
 *
 * <p>Implémentation visant Minecraft 1.21.1.</p>
 */
public final class GaldurRender {
    /**
     * Côté d'une icône d'objet, en pixels d'interface.
     */
    public static final int ITEM_SIZE = 16;

    private GaldurRender() {
    }

    /**
     * Dessine un sprite 9-slice teinté.
     *
     * <p>Chaque pixel du sprite est multiplié par {@code argb} : un pixel blanc prend exactement
     * la couleur demandée, un pixel gris une version plus sombre. Les sprites de Galdur UI sont
     * donc en niveaux de gris, et leur couleur vient du thème à chaque image. L'alpha de
     * {@code argb} rend le sprite translucide.</p>
     *
     * <p>Le découpage vient du {@code .png.mcmeta} du sprite : les coins gardent leur taille,
     * les bords et le centre sont répétés (tuilés) puis rognés, jamais étirés, exactement comme
     * {@code GuiGraphics.blitNineSlicedSprite}. Si le rectangle est plus petit que deux fois la
     * bordure, la bordure est réduite à la moitié de la largeur (ou de la hauteur), comme en
     * vanilla.</p>
     *
     * <p>Implémentation 1.21.1, choisie après mesure avec Spark (UI-38) : Minecraft dessine un
     * 9-slice avec un appel de dessin par tuile, et changer la teinte par
     * {@link GuiGraphics#setColor} vide en plus le tampon de dessin à chaque sprite. Ici, toutes
     * les tuiles partent dans <strong>un seul</strong> tampon, la teinte étant portée par chaque
     * sommet (shader {@code position_tex_color}) : un seul appel de dessin par sprite, et plus de
     * vidage. Comme {@code blitSprite}, le dessin est immédiat ; blending et test de profondeur
     * restent activés. Un sprite qui n'est pas en 9-slice ({@code stretch}, {@code tile}) passe
     * par le chemin vanilla.</p>
     *
     * @param graphics contexte de rendu fourni par l'écran
     * @param sprite   identifiant du sprite dans l'atlas d'interface
     * @param x        abscisse du coin supérieur gauche
     * @param y        ordonnée du coin supérieur gauche
     * @param width    largeur voulue, en pixels d'interface
     * @param height   hauteur voulue, en pixels d'interface
     * @param argb     teinte, en ARGB empaqueté ({@code 0xFFFFFFFF} = aucune teinte)
     */
    public static void drawNineSlice(GuiGraphics graphics, ResourceLocation sprite, int x, int y, int width, int height, int argb) {
        drawNineSliceGrid(graphics, sprite, x, y, width, height, 1, 1, argb);
    }

    /**
     * Dessine le même sprite 9-slice teinté {@code columns} × {@code rows} fois, côte à côte, en
     * un seul appel de dessin : le fond des cases d'une grille, par exemple. Même rendu que
     * {@code columns × rows} appels à {@link #drawNineSlice}, en beaucoup moins coûteux.
     *
     * @param graphics contexte de rendu fourni par l'écran
     * @param sprite   identifiant du sprite dans l'atlas d'interface
     * @param x        abscisse du coin supérieur gauche de la première copie
     * @param y        ordonnée du coin supérieur gauche de la première copie
     * @param width    largeur d'une copie, qui est aussi le pas horizontal
     * @param height   hauteur d'une copie, qui est aussi le pas vertical
     * @param columns  nombre de copies par rangée
     * @param rows     nombre de rangées
     * @param argb     teinte, en ARGB empaqueté
     */
    public static void drawNineSliceGrid(GuiGraphics graphics, ResourceLocation sprite, int x, int y, int width, int height,
                                         int columns, int rows, int argb) {
        if (width <= 0 || height <= 0 || columns <= 0 || rows <= 0) {
            return;
        }
        GuiSpriteManager sprites = Minecraft.getInstance().getGuiSprites();
        TextureAtlasSprite atlasSprite = sprites.getSprite(sprite);
        if (!(sprites.getSpriteScaling(atlasSprite) instanceof GuiSpriteScaling.NineSlice nineSlice)) {
            for (int row = 0; row < rows; row++) {
                for (int column = 0; column < columns; column++) {
                    drawSpriteVanilla(graphics, sprite, x + column * width, y + row * height, width, height, argb);
                }
            }
            return;
        }
        int sliceWidth = nineSlice.width();
        int sliceHeight = nineSlice.height();
        GuiSpriteScaling.NineSlice.Border border = nineSlice.border();
        int left = Math.min(border.left(), width / 2);
        int right = Math.min(border.right(), width / 2);
        int top = Math.min(border.top(), height / 2);
        int bottom = Math.min(border.bottom(), height / 2);

        // Colonnes et rangées du 9-slice : zone à l'écran (début, taille) et zone source dans le sprite.
        int[] areaX = {0, left, width - right};
        int[] areaW = {left, width - left - right, right};
        int[] srcX = {0, left, sliceWidth - right};
        int[] srcW = {left, sliceWidth - left - right, right};
        int[] areaY = {0, top, height - bottom};
        int[] areaH = {top, height - top - bottom, bottom};
        int[] srcY = {0, top, sliceHeight - bottom};
        int[] srcH = {top, sliceHeight - top - bottom, bottom};

        Matrix4f pose = graphics.pose().last().pose();
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        for (int copyRow = 0; copyRow < rows; copyRow++) {
            for (int copyColumn = 0; copyColumn < columns; copyColumn++) {
                int originX = x + copyColumn * width;
                int originY = y + copyRow * height;
                for (int row = 0; row < 3; row++) {
                    for (int column = 0; column < 3; column++) {
                        addTiled(buffer, pose, atlasSprite, argb,
                                originX + areaX[column], originY + areaY[row], areaW[column], areaH[row],
                                srcX[column], srcY[row], srcW[column], srcH[row], sliceWidth, sliceHeight);
                    }
                }
            }
        }
        MeshData mesh = buffer.build();
        if (mesh == null) {
            return;
        }
        RenderSystem.setShaderTexture(0, atlasSprite.atlasLocation());
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        BufferUploader.drawWithShader(mesh);
    }

    /**
     * Exécute {@code draw} en regroupant le texte et les aplats ({@link #text},
     * {@link #drawFill}, {@link #drawItemCount}...) : ils partent en un seul envoi à la carte
     * graphique à la fin, au lieu d'un envoi par appel. Utile pour beaucoup de petits textes,
     * comme les quantités d'une grille.
     *
     * <p>Ce qui est dessiné dans {@code draw} passe donc <strong>au-dessus</strong> des sprites et
     * des objets dessinés pendant ce temps : n'y mettre que des éléments qui se superposent à
     * tout le reste. Implémentation 1.21.1 : {@link GuiGraphics#drawManaged(Runnable)}, marquée
     * dépréciée par Minecraft car remplacée en 1.21.6 ; c'est la raison d'être de cette couche.</p>
     *
     * @param graphics contexte de rendu fourni par l'écran
     * @param draw     les dessins à regrouper
     */
    @SuppressWarnings("deprecation")
    public static void drawBatched(GuiGraphics graphics, Runnable draw) {
        graphics.drawManaged(draw);
    }

    /**
     * Ajoute au tampon les tuiles qui couvrent une zone de l'écran en répétant une zone du
     * sprite ; la dernière tuile de chaque rangée et de chaque colonne est rognée.
     *
     * @param areaX        abscisse de la zone à couvrir
     * @param areaY        ordonnée de la zone à couvrir
     * @param areaW        largeur de la zone à couvrir ; rien n'est ajouté si elle est nulle
     * @param areaH        hauteur de la zone à couvrir
     * @param srcX         abscisse de la zone source, en pixels du sprite
     * @param srcY         ordonnée de la zone source
     * @param srcW         largeur de la zone source ; rien n'est ajouté si elle est nulle
     * @param srcH         hauteur de la zone source
     * @param spriteWidth  largeur du sprite en pixels (celle du {@code .mcmeta})
     * @param spriteHeight hauteur du sprite en pixels
     */
    private static void addTiled(BufferBuilder buffer, Matrix4f pose, TextureAtlasSprite sprite, int argb,
                                 int areaX, int areaY, int areaW, int areaH,
                                 int srcX, int srcY, int srcW, int srcH, int spriteWidth, int spriteHeight) {
        if (areaW <= 0 || areaH <= 0 || srcW <= 0 || srcH <= 0) {
            return;
        }
        for (int dy = 0; dy < areaH; dy += srcH) {
            int tileH = Math.min(srcH, areaH - dy);
            float v0 = sprite.getV((float) srcY / spriteHeight);
            float v1 = sprite.getV((float) (srcY + tileH) / spriteHeight);
            for (int dx = 0; dx < areaW; dx += srcW) {
                int tileW = Math.min(srcW, areaW - dx);
                float u0 = sprite.getU((float) srcX / spriteWidth);
                float u1 = sprite.getU((float) (srcX + tileW) / spriteWidth);
                float x0 = areaX + dx;
                float y0 = areaY + dy;
                float x1 = x0 + tileW;
                float y1 = y0 + tileH;
                buffer.addVertex(pose, x0, y0, 0).setUv(u0, v0).setColor(argb);
                buffer.addVertex(pose, x0, y1, 0).setUv(u0, v1).setColor(argb);
                buffer.addVertex(pose, x1, y1, 0).setUv(u1, v1).setColor(argb);
                buffer.addVertex(pose, x1, y0, 0).setUv(u1, v0).setColor(argb);
            }
        }
    }

    /**
     * Chemin vanilla, pour un sprite qui n'est pas en 9-slice : {@link GuiGraphics#setColor} (qui
     * vide le tampon en attente), le même état que {@code AbstractButton.renderWidget}, puis
     * {@code blitSprite}. La couleur est remise à blanc après le dessin.
     */
    private static void drawSpriteVanilla(GuiGraphics graphics, ResourceLocation sprite, int x, int y, int width, int height, int argb) {
        graphics.setColor(channel(argb, 16), channel(argb, 8), channel(argb, 0), channel(argb, 24));
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        graphics.blitSprite(sprite, x, y, width, height);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    /**
     * Dessine le contour d'un rectangle, sans remplir l'intérieur.
     *
     * <p>Le trait fait 1 pixel d'interface et reste à l'intérieur de la zone donnée : le contour
     * n'empiète jamais sur les pixels voisins. L'alpha de {@code argb} est respecté, ce qui permet
     * un trait translucide qui ne cache pas ce qu'il entoure. Aucun état de rendu n'est modifié.</p>
     *
     * <p>Sert surtout au mode debug des layouts, mais reste utilisable pour tout cadre.</p>
     *
     * @param graphics contexte de rendu fourni par l'écran
     * @param x        abscisse du coin supérieur gauche
     * @param y        ordonnée du coin supérieur gauche
     * @param width    largeur, en pixels d'interface
     * @param height   hauteur, en pixels d'interface
     * @param argb     couleur du trait, en ARGB empaqueté
     */
    public static void drawOutline(GuiGraphics graphics, int x, int y, int width, int height, int argb) {
        graphics.renderOutline(x, y, width, height, argb);
    }

    /**
     * Remplit un rectangle d'une couleur unie.
     *
     * <p>L'alpha de {@code argb} est respecté : une couleur translucide laisse voir ce qui est
     * dessous. Aucun état de rendu n'est modifié.</p>
     *
     * @param graphics contexte de rendu fourni par l'écran
     * @param x        abscisse du coin supérieur gauche
     * @param y        ordonnée du coin supérieur gauche
     * @param width    largeur, en pixels d'interface
     * @param height   hauteur, en pixels d'interface
     * @param argb     couleur de remplissage, en ARGB empaqueté
     */
    public static void drawFill(GuiGraphics graphics, int x, int y, int width, int height, int argb) {
        graphics.fill(x, y, x + width, y + height, argb);
    }

    /**
     * Commence un découpage : jusqu'au {@link #popClip(GuiGraphics)} correspondant, seuls les
     * pixels situés dans ce rectangle sont dessinés, le reste est coupé.
     *
     * <p>Les découpages s'empilent : un découpage ouvert à l'intérieur d'un autre ne garde que
     * l'intersection des deux (une liste dans un onglet dans un panneau ne déborde d'aucun des
     * trois). Chaque {@code pushClip} doit être suivi de son {@code popClip}, y compris quand le
     * dessin lève une exception, sinon tout le reste de l'écran resterait découpé :
     * {@link #drawClipped(GuiGraphics, int, int, int, int, Runnable)} s'en charge.</p>
     *
     * <p>Implémentation 1.21.1 : {@link GuiGraphics#enableScissor(int, int, int, int)}, qui gère
     * la pile et vide le tampon de dessin en attente. Le rectangle est en coordonnées d'écran
     * (pixels d'interface) et Minecraft 1.21.1 n'y applique pas la pose de {@code graphics} :
     * on le fait ici, pour qu'une zone découpée suive son contenu quand il est déplacé (par
     * exemple par {@link #drawOffset}, pendant une animation). Une largeur ou une hauteur négative est
     * ramenée à zéro, ce qui ne laisse rien passer.</p>
     *
     * @param graphics contexte de rendu fourni par l'écran
     * @param x        abscisse du coin supérieur gauche de la zone visible
     * @param y        ordonnée du coin supérieur gauche de la zone visible
     * @param width    largeur de la zone visible, en pixels d'interface
     * @param height   hauteur de la zone visible, en pixels d'interface
     */
    public static void pushClip(GuiGraphics graphics, int x, int y, int width, int height) {
        Matrix4f pose = graphics.pose().last().pose();
        Vector3f a = pose.transformPosition(x, y, 0, new Vector3f());
        Vector3f b = pose.transformPosition(x + Math.max(0, width), y + Math.max(0, height), 0, new Vector3f());
        graphics.enableScissor(Math.round(Math.min(a.x, b.x)), Math.round(Math.min(a.y, b.y)),
                Math.round(Math.max(a.x, b.x)), Math.round(Math.max(a.y, b.y)));
    }

    /**
     * Exécute {@code draw} décalé de {@code (dx, dy)} pixels d'interface, puis rétablit la
     * position, même si le dessin lève une exception. Sert aux animations de mouvement : tout ce
     * que dessine {@code draw} (sprites, texte, objets, zones découpées) est déplacé d'un bloc.
     *
     * <p>Seul le dessin bouge : les zones de clic des widgets restent où elles sont. Implémentation
     * 1.21.1 : {@code pose().pushPose()}, {@code translate}, {@code popPose()}.</p>
     *
     * @param graphics contexte de rendu fourni par l'écran
     * @param dx       décalage horizontal, en pixels d'interface
     * @param dy       décalage vertical, en pixels d'interface
     * @param draw     le dessin à décaler
     */
    public static void drawOffset(GuiGraphics graphics, float dx, float dy, Runnable draw) {
        if (dx == 0 && dy == 0) {
            draw.run();
            return;
        }
        PoseStack pose = graphics.pose();
        pose.pushPose();
        try {
            pose.translate(dx, dy, 0);
            draw.run();
        } finally {
            pose.popPose();
        }
    }

    /**
     * Termine le dernier découpage ouvert par {@link #pushClip(GuiGraphics, int, int, int, int)}
     * et rétablit le précédent (ou plus aucun découpage s'il n'y en avait qu'un).
     *
     * <p>Implémentation 1.21.1 : {@link GuiGraphics#disableScissor()}. Appeler cette méthode sans
     * découpage ouvert est une erreur de programmation : Minecraft lève alors une
     * {@link IllegalStateException}.</p>
     *
     * @param graphics contexte de rendu fourni par l'écran
     */
    public static void popClip(GuiGraphics graphics) {
        graphics.disableScissor();
    }

    /**
     * Exécute {@code draw} avec un découpage actif, puis le termine dans tous les cas, même si le
     * dessin lève une exception. Forme à préférer à un {@code pushClip} / {@code popClip} écrit
     * à la main.
     *
     * <pre>{@code
     * GaldurRender.drawClipped(graphics, getX(), getY(), getWidth(), getHeight(), () -> {
     *     for (Ligne ligne : lignesVisibles) {
     *         ligne.render(graphics, ...);
     *     }
     * });
     * }</pre>
     *
     * @param graphics contexte de rendu fourni par l'écran
     * @param x        abscisse du coin supérieur gauche de la zone visible
     * @param y        ordonnée du coin supérieur gauche de la zone visible
     * @param width    largeur de la zone visible, en pixels d'interface
     * @param height   hauteur de la zone visible, en pixels d'interface
     * @param draw     le dessin à découper
     */
    public static void drawClipped(GuiGraphics graphics, int x, int y, int width, int height, Runnable draw) {
        pushClip(graphics, x, y, width, height);
        try {
            draw.run();
        } finally {
            popClip(graphics);
        }
    }

    /**
     * Dessine l'icône d'un objet, avec sa barre d'usure s'il en a une, mais sans sa quantité :
     * voir {@link #drawItemCount(GuiGraphics, String, int, int, int)}.
     *
     * <p>Implémentation 1.21.1 : {@link GuiGraphics#renderItem(ItemStack, int, int)} puis
     * {@link GuiGraphics#renderItemDecorations(Font, ItemStack, int, int, String)} avec un texte
     * vide, qui dessine l'usure et le temps de recharge sans le nombre vanilla.</p>
     *
     * @param graphics contexte de rendu fourni par l'écran
     * @param stack    l'objet ; rien n'est dessiné s'il est vide
     * @param x        abscisse du coin supérieur gauche de l'icône (16 × 16)
     * @param y        ordonnée du coin supérieur gauche de l'icône
     */
    public static void drawItem(GuiGraphics graphics, ItemStack stack, int x, int y) {
        if (stack.isEmpty()) {
            return;
        }
        graphics.renderItem(stack, x, y);
        graphics.renderItemDecorations(font(), stack, x, y, "");
    }

    /**
     * Voile une icône d'objet déjà dessinée, par exemple pour la montrer désactivée.
     *
     * <p>Implémentation 1.21.1 : les objets sont dessinés en avant de l'interface ({@code z}
     * + 150) ; un simple remplissage passerait derrière eux. La pose de {@code graphics} est donc
     * décalée vers l'avant ({@code z} + 200, comme la quantité) le temps du remplissage, puis
     * rétablie.</p>
     *
     * @param graphics contexte de rendu fourni par l'écran
     * @param x        abscisse du coin supérieur gauche de l'icône (16 × 16)
     * @param y        ordonnée du coin supérieur gauche de l'icône
     * @param argb     couleur du voile, en ARGB empaqueté ; son alpha règle l'opacité
     */
    public static void dimItem(GuiGraphics graphics, int x, int y, int argb) {
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(0.0F, 0.0F, 200.0F);
        graphics.fill(x, y, x + ITEM_SIZE, y + ITEM_SIZE, argb);
        pose.popPose();
    }

    /**
     * Dessine une quantité dans le coin inférieur droit d'une icône d'objet, par-dessus l'icône.
     *
     * <p>Un texte qui tient dans les 16 pixels de l'icône (« 64 ») est écrit à la taille normale,
     * à la même place que le nombre vanilla. Un texte plus large (« 1,2k ») est écrit à demi-taille
     * pour rester dans la case.</p>
     *
     * <p>Implémentation 1.21.1 : la pose de {@code graphics} est décalée vers l'avant
     * ({@code z} + 200, comme vanilla) pour passer devant l'objet, puis rétablie.</p>
     *
     * @param graphics contexte de rendu fourni par l'écran
     * @param text     le texte à écrire, en général {@code GaldurFormat.amount(...)}
     * @param x        abscisse du coin supérieur gauche de l'icône
     * @param y        ordonnée du coin supérieur gauche de l'icône
     * @param argb     couleur du texte, en ARGB empaqueté
     */
    public static void drawItemCount(GuiGraphics graphics, String text, int x, int y, int argb) {
        Font font = font();
        int width = font.width(text);
        PoseStack pose = graphics.pose();
        pose.pushPose();
        if (width <= ITEM_SIZE) {
            pose.translate(0.0F, 0.0F, 200.0F);
            graphics.drawString(font, text, x + ITEM_SIZE + 1 - width, y + 9, argb, true);
        } else {
            pose.translate(x + ITEM_SIZE, y + ITEM_SIZE, 200.0F);
            pose.scale(0.5F, 0.5F, 1.0F);
            graphics.drawString(font, text, -width, -font.lineHeight + 1, argb, true);
        }
        pose.popPose();
    }

    /**
     * Demande à l'écran courant d'afficher l'infobulle d'un objet à la fin de son rendu, par-dessus
     * tous les widgets : nom, description, enchantements... comme dans un inventaire.
     *
     * <p>À appeler pendant le rendu, à chaque image où la souris survole l'objet. Les lignes de
     * {@code extra} sont ajoutées à la fin (par exemple la quantité exacte).</p>
     *
     * <p>Implémentation 1.21.1 : {@link Screen#getTooltipFromItem(Minecraft, ItemStack)} puis
     * {@link Screen#setTooltipForNextRenderPass(List)}. Les infobulles à image (contenu d'un sac)
     * ne sont pas affichées.</p>
     *
     * @param stack l'objet ; sans effet s'il est vide ou s'il n'y a pas d'écran ouvert
     * @param extra lignes ajoutées après celles de l'objet
     */
    public static void showItemTooltip(ItemStack stack, List<Component> extra) {
        Minecraft minecraft = Minecraft.getInstance();
        Screen screen = minecraft.screen;
        if (stack.isEmpty() || screen == null) {
            return;
        }
        List<Component> lines = new ArrayList<>(Screen.getTooltipFromItem(minecraft, stack));
        lines.addAll(extra);
        List<FormattedCharSequence> sequences = new ArrayList<>(lines.size());
        for (Component line : lines) {
            sequences.add(line.getVisualOrderText());
        }
        screen.setTooltipForNextRenderPass(sequences);
    }

    /**
     * {@return la composante de {@code argb} décalée de {@code shift} bits, entre 0 et 1}
     */
    private static float channel(int argb, int shift) {
        return ((argb >>> shift) & 0xFF) / 255.0F;
    }

    /**
     * Dessine une ligne de texte alignée à gauche.
     *
     * <p>Seul appel à {@link GuiGraphics#drawString} de la bibliothèque : les autres méthodes
     * de texte passent par ici.</p>
     *
     * @param graphics contexte de rendu
     * @param text     texte à dessiner
     * @param x        abscisse du premier caractère
     * @param y        ordonnée du haut de la ligne
     * @param argb     couleur du texte, en ARGB empaqueté
     * @param shadow   {@code true} pour dessiner l'ombre portée
     */
    public static void text(GuiGraphics graphics, Component text, int x, int y, int argb, boolean shadow) {
        graphics.drawString(font(), text, x, y, argb, shadow);
    }

    public static void text(GuiGraphics graphics, FormattedCharSequence text, int x, int y, int argb, boolean shadow) {
        graphics.drawString(font(), text, x, y, argb, shadow);
    }

    /**
     * Dessine une ligne de texte avec ombre portée, centrée horizontalement.
     *
     * @param graphics contexte de rendu
     * @param text     texte à dessiner
     * @param centerX  abscisse du centre de la ligne
     * @param y        ordonnée du haut de la ligne
     * @param argb     couleur du texte, en ARGB empaqueté
     */
    public static void drawCenteredText(GuiGraphics graphics, Component text, int centerX, int y, int argb) {
        text(graphics, text, centerX - textWidth(text) / 2, y, argb, true);
    }

    public static FormattedCharSequence truncateText(Component text, int maxWidth) {
        int textWidth = font().width(text);
        if (maxWidth <= 0 || textWidth <= maxWidth) {
            return Language.getInstance().getVisualOrder(text);
        }
        int truncatedWidth = maxWidth - font().width(CommonComponents.ELLIPSIS);
        FormattedText truncatedText = font().substrByWidth(text, truncatedWidth);
        return Language.getInstance().getVisualOrder(FormattedText.composite(truncatedText, CommonComponents.ELLIPSIS));
    }

    /**
     * {@return la largeur de {@code text} en pixels d'interface}
     *
     * <p>Permet aux widgets de se dimensionner sans toucher à {@link Font}.</p>
     *
     * @param text texte à mesurer
     */
    public static int textWidth(Component text) {
        return font().width(text);
    }

    /**
     * {@return la hauteur d'une ligne de texte de la police courante, en pixels d'interface}
     */
    public static int lineHeight() {
        return font().lineHeight;
    }

    private static Font font() {
        return Minecraft.getInstance().font;
    }
}
