package com.galdurstudio.galdurui.internal.dev;

import com.galdurstudio.galdurui.GaldurUI;
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

/**
 * Blocs de développement (client <strong>et</strong> serveur) : le coffre Galdur de test, son
 * objet et son entité de bloc. Rangé dans l'onglet créatif « Blocs fonctionnels ».
 *
 * <p>Enregistré seulement hors production, par {@code GaldurUI} : le registre est le même sur le
 * client et le serveur de développement. Les modèles, la table de butin et les traductions sont
 * dans les ressources du jar, sans effet tant que le bloc n'est pas enregistré.</p>
 */
public final class DevBlocks {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(GaldurUI.MODID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(GaldurUI.MODID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, GaldurUI.MODID);

    /** Le coffre Galdur de test. */
    public static final DeferredBlock<TestChestBlock> TEST_CHEST = BLOCKS.register("test_chest",
            () -> new TestChestBlock(BlockBehaviour.Properties.of().strength(2.5F)));
    /** Son objet, pour le poser. */
    public static final DeferredItem<BlockItem> TEST_CHEST_ITEM = ITEMS.registerSimpleBlockItem(TEST_CHEST);
    /** Son entité de bloc, qui garde le contenu. */
    // build(null) : aucun type de conversion de données (data fixer), comme la plupart des mods.
    @SuppressWarnings("DataFlowIssue")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TestChestBlockEntity>> TEST_CHEST_ENTITY =
            BLOCK_ENTITIES.register("test_chest",
                    () -> BlockEntityType.Builder.of(TestChestBlockEntity::new, TEST_CHEST.get()).build(null));

    private DevBlocks() {
    }

    /**
     * Enregistre les blocs, objets et entités de bloc, et l'ajout à l'onglet créatif.
     *
     * @param modEventBus le bus du mod
     */
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
