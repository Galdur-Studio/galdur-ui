package com.galdurstudio.galdurui.testmod;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.client.gui.screens.inventory.BeaconScreen;
import net.minecraft.client.gui.screens.inventory.BrewingStandScreen;
import net.minecraft.client.gui.screens.inventory.CartographyTableScreen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.client.gui.screens.inventory.FurnaceScreen;
import net.minecraft.client.gui.screens.inventory.GrindstoneScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.LoomScreen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.client.gui.screens.inventory.SmithingScreen;
import net.minecraft.client.gui.screens.inventory.StonecutterScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.inventory.BrewingStandMenu;
import net.minecraft.world.inventory.CartographyTableMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.inventory.FurnaceMenu;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.inventory.LoomMenu;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.inventory.StonecutterMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

/**
 * The vanilla screens with slots, opened on the client alone with a few items in them, to check
 * how they look on each Minecraft version (ScreenshotTour with {@code galduruitest.containers}).
 */
final class ContainerScreens {
    private ContainerScreens() {
    }

    /** {@return each screen by name, in a world} */
    static Map<String, Supplier<Screen>> all(Minecraft minecraft) {
        Map<String, Supplier<Screen>> screens = new LinkedHashMap<>();
        screens.put("inventory", () -> new InventoryScreen(minecraft.player));
        screens.put("crafting", () -> new CraftingScreen(new CraftingMenu(0, inventory(minecraft)), inventory(minecraft),
                Component.translatable("container.crafting")));
        screens.put("furnace", () -> furnace(minecraft));
        screens.put("brewing", () -> brewing(minecraft));
        screens.put("anvil", () -> new AnvilScreen(new AnvilMenu(0, inventory(minecraft)), inventory(minecraft),
                Component.translatable("container.repair")));
        screens.put("smithing", () -> new SmithingScreen(new SmithingMenu(0, inventory(minecraft)), inventory(minecraft),
                Component.translatable("container.upgrade")));
        screens.put("grindstone", () -> new GrindstoneScreen(new GrindstoneMenu(0, inventory(minecraft)),
                inventory(minecraft), Component.translatable("container.grindstone_title")));
        screens.put("stonecutter", () -> stonecutter(minecraft));
        screens.put("cartography", () -> new CartographyTableScreen(new CartographyTableMenu(0, inventory(minecraft)),
                inventory(minecraft), Component.translatable("container.cartography_table")));
        screens.put("loom", () -> loom(minecraft));
        screens.put("enchanting", () -> enchanting(minecraft));
        screens.put("beacon", () -> beacon(minecraft));
        screens.put("merchant", () -> merchant(minecraft));
        return screens;
    }

    private static Inventory inventory(Minecraft minecraft) {
        return minecraft.player.getInventory();
    }

    private static Screen furnace(Minecraft minecraft) {
        SimpleContainer slots = new SimpleContainer(3);
        slots.setItem(0, new ItemStack(Items.RAW_IRON, 12));
        slots.setItem(1, new ItemStack(Items.COAL, 5));
        slots.setItem(2, new ItemStack(Items.IRON_INGOT, 3));
        // Burning for half its fuel, halfway through the item.
        SimpleContainerData data = new SimpleContainerData(4);
        data.set(0, 800);
        data.set(1, 1600);
        data.set(2, 100);
        data.set(3, 200);
        return new FurnaceScreen(new FurnaceMenu(0, inventory(minecraft), slots, data), inventory(minecraft),
                Component.translatable("container.furnace"));
    }

    private static Screen brewing(Minecraft minecraft) {
        SimpleContainer slots = new SimpleContainer(5);
        slots.setItem(0, new ItemStack(Items.POTION));
        slots.setItem(1, new ItemStack(Items.POTION));
        slots.setItem(3, new ItemStack(Items.NETHER_WART, 8));
        // Halfway through brewing, with some fuel left.
        // Two values (time, fuel) up to 1.21, four in 26.x: four fits both.
        SimpleContainerData data = new SimpleContainerData(4);
        data.set(0, 200);
        data.set(1, 12);
        return new BrewingStandScreen(new BrewingStandMenu(0, inventory(minecraft), slots, data), inventory(minecraft),
                Component.translatable("container.brewing"));
    }

    private static Screen stonecutter(Minecraft minecraft) {
        StonecutterMenu menu = new StonecutterMenu(0, inventory(minecraft));
        menu.getSlot(0).set(new ItemStack(Items.STONE, 16));
        // The second recipe chosen, to see the selected one.
        menu.clickMenuButton(minecraft.player, 1);
        return new StonecutterScreen(menu, inventory(minecraft), Component.translatable("container.stonecutter"));
    }

    private static Screen loom(Minecraft minecraft) {
        LoomMenu menu = new LoomMenu(0, inventory(minecraft));
        //? if >=26.1 {
        /*menu.getSlot(0).set(new ItemStack(Items.BANNER.white()));
        menu.getSlot(1).set(new ItemStack(Items.DYE.red(), 4));
        *///?} else {
        menu.getSlot(0).set(new ItemStack(Items.WHITE_BANNER));
        menu.getSlot(1).set(new ItemStack(Items.RED_DYE, 4));
        //?}
        return new LoomScreen(menu, inventory(minecraft), Component.translatable("container.loom"));
    }

    private static Screen enchanting(Minecraft minecraft) {
        minecraft.player.experienceLevel = 20;
        EnchantmentMenu menu = new EnchantmentMenu(0, inventory(minecraft));
        menu.getSlot(0).set(new ItemStack(Items.DIAMOND_SWORD));
        menu.getSlot(1).set(new ItemStack(Items.LAPIS_LAZULI, 9));
        menu.costs[0] = 5;
        menu.costs[1] = 14;
        menu.costs[2] = 30;
        return new EnchantmentScreen(menu, inventory(minecraft), Component.translatable("container.enchant"));
    }

    private static Screen beacon(Minecraft minecraft) {
        SimpleContainerData data = new SimpleContainerData(3);
        data.set(0, 4);
        return new BeaconScreen(new BeaconMenu(0, inventory(minecraft), data, ContainerLevelAccess.NULL),
                inventory(minecraft), Component.translatable("container.beacon"));
    }

    private static Screen merchant(Minecraft minecraft) {
        MerchantMenu menu = new MerchantMenu(0, inventory(minecraft));
        MerchantOffers offers = new MerchantOffers();
        offers.add(offer(Items.WHEAT, 20, new ItemStack(Items.EMERALD)));
        offers.add(offer(Items.EMERALD, 1, new ItemStack(Items.BREAD, 6)));
        offers.add(offer(Items.EMERALD, 3, new ItemStack(Items.CAKE)));
        menu.setOffers(offers);
        menu.setMerchantLevel(2);
        menu.setXp(25);
        menu.setShowProgressBar(true);
        return new MerchantScreen(menu, inventory(minecraft), Component.literal("Farmer"));
    }

    private static MerchantOffer offer(Item cost, int count, ItemStack result) {
        //? if >=1.21 {
        return new MerchantOffer(new net.minecraft.world.item.trading.ItemCost(cost, count), result, 12, 2, 0.05F);
        //?} else {
        /*return new MerchantOffer(new ItemStack(cost, count), result, 12, 2, 0.05F);
        *///?}
    }
}
