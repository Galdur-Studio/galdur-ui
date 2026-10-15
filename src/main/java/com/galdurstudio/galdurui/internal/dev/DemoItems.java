package com.galdurstudio.galdurui.internal.dev;

import com.galdurstudio.galdurui.api.widget.GaldurItemGrid;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Données de démonstration partagées par les écrans de développement. */
final class DemoItems {
    private DemoItems() {
    }

    /**
     * {@return tous les objets du jeu (sauf l'air), chacun avec une quantité de démonstration
     * variée : de 0 à plusieurs milliards, pour voir toutes les abréviations}
     */
    static List<GaldurItemGrid.Entry> allWithAmounts() {
        List<GaldurItemGrid.Entry> all = new ArrayList<>();
        int index = 0;
        for (Item item : BuiltInRegistries.ITEM) {
            if (item == Items.AIR) {
                continue;
            }
            long amount = (index % 13 == 0) ? 0 : (long) Math.pow(10, index % 10) * (1 + index % 7);
            all.add(new GaldurItemGrid.Entry(new ItemStack(item), amount));
            index++;
        }
        return all;
    }
}
