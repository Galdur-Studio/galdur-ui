package com.galdurstudio.galdurui.testmod;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** The test chest: block, item and block entity, listed in the "Functional Blocks" creative tab. */
public final class TestBlocks {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(GaldurUITest.MODID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(GaldurUITest.MODID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, GaldurUITest.MODID);

    public static final DeferredBlock<TestChestBlock> TEST_CHEST = BLOCKS.register("test_chest",
            () -> new TestChestBlock(BlockBehaviour.Properties.of().strength(2.5F)));
    public static final DeferredItem<BlockItem> TEST_CHEST_ITEM = ITEMS.registerSimpleBlockItem(TEST_CHEST);
    // build(null): no data fixer type, like most mods.
    @SuppressWarnings("DataFlowIssue")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TestChestBlockEntity>> TEST_CHEST_ENTITY =
            BLOCK_ENTITIES.register("test_chest",
                    () -> BlockEntityType.Builder.of(TestChestBlockEntity::new, TEST_CHEST.get()).build(null));

    private TestBlocks() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        modEventBus.addListener(BuildCreativeModeTabContentsEvent.class, event -> {
            if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
                event.accept(TEST_CHEST_ITEM);
            }
        });
    }
}
