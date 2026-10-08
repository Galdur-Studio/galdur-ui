#!/usr/bin/env python3
"""Génère les sprites 9-slice de Galdur UI.

Les PNG du dépôt sont volontairement minuscules (16x16) et entièrement décrits par ce
script : pour retoucher la charte, on modifie les couleurs ci-dessous et on régénère,
plutôt que d'éditer les images à la main.

    pip install Pillow
    python tools/generate_sprites.py

Chaque sprite est accompagné d'un .png.mcmeta en nine_slice (bordure de 4 px) : les
coins restent nets quelle que soit la taille du widget, seuls les bords et le centre
sont étirés.

Note : une version Java équivalente est fournie dans tools/GenerateSprites.java pour
les postes sans Python ; les deux produisent exactement les mêmes fichiers.
"""

import json
import math
import os

from PIL import Image

SIZE = 16          # côté du sprite, en pixels
RADIUS = 4         # rayon des coins arrondis, égal à la bordure 9-slice
BORDER = 4         # bordure 9-slice

OUT_DIR = os.path.join(
    os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
    "src", "main", "resources", "assets", "galdurui", "textures", "gui", "sprites",
)

# Couleurs de remplissage, reprises de assets/galdurui/theme.json.
# « inset » inverse le relief : la lumière passe en bas, le widget paraît enfoncé.
SPRITES = {
    "panel":           ("#2A2420", False),  # background / charbon
    "button":          ("#B8341F", False),  # accent / redstone
    "button_hover":    ("#D9573D", False),  # accent_hover
    "button_pressed":  ("#952A18", True),   # accent_pressed
    "button_disabled": ("#352E29", False),  # surface
}

MCMETA = {"gui": {"scaling": {"type": "nine_slice", "width": SIZE, "height": SIZE, "border": BORDER}}}


def hex_to_rgb(value):
    value = value.lstrip("#")
    return tuple(int(value[i:i + 2], 16) for i in (0, 2, 4))


def scale(rgb, factor):
    """Éclaircit (factor > 1) ou assombrit (factor < 1) une couleur, en restant dans [0, 255]."""
    return tuple(max(0, min(255, round(channel * factor))) for channel in rgb)


def distance_to_rounded_rect(x, y):
    """Distance du centre du pixel (x, y) au rectangle intérieur, pour tailler les coins.

    On ramène le point sur le rectangle central ; sur les bords droits la distance vaut
    l'écart à l'arête, et dans les coins elle vaut la distance au centre du cercle.
    """
    px, py = x + 0.5, y + 0.5
    cx = min(max(px, RADIUS), SIZE - RADIUS)
    cy = min(max(py, RADIUS), SIZE - RADIUS)
    return math.hypot(px - cx, py - cy)


def build_sprite(fill_hex, inset):
    fill = hex_to_rgb(fill_hex)
    outline = scale(fill, 0.45)    # liseré sombre : détache le widget du fond
    light = scale(fill, 1.22)      # arête éclairée
    dark = scale(fill, 0.75)       # arête dans l'ombre
    top, bottom = (dark, light) if inset else (light, dark)

    image = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    pixels = image.load()
    for y in range(SIZE):
        for x in range(SIZE):
            d = distance_to_rounded_rect(x, y)
            if d > RADIUS:
                continue                       # coin taillé : transparent
            if d > RADIUS - 1.0:
                pixels[x, y] = outline + (255,)
            elif y == 1:
                pixels[x, y] = top + (255,)
            elif y == SIZE - 2:
                pixels[x, y] = bottom + (255,)
            else:
                pixels[x, y] = fill + (255,)
    return image


def main():
    os.makedirs(OUT_DIR, exist_ok=True)
    for name, (fill_hex, inset) in SPRITES.items():
        png = os.path.join(OUT_DIR, name + ".png")
        build_sprite(fill_hex, inset).save(png)
        with open(png + ".mcmeta", "w", encoding="utf-8", newline="\n") as handle:
            json.dump(MCMETA, handle, indent=2)
            handle.write("\n")
        print("écrit", png)


if __name__ == "__main__":
    main()
