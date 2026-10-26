package com.galdurstudio.galdurui.testmod;

import com.galdurstudio.galdurui.api.widget.GaldurItemGrid;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Demo data shared by the test screens. */
final class DemoItems {
    private DemoItems() {
    }

    /**
     * {@return every item except air, each with a varied demo amount, from 0 to several billions,
     * to show every abbreviation}
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
