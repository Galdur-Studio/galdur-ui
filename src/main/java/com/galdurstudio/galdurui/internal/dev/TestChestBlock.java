package com.galdurstudio.galdurui.internal.dev;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Coffre Galdur de test : un bloc posable qui ouvre un {@link TestChestMenu} affiché dans un
 * {@code GaldurContainerScreen}. Son contenu est sauvegardé dans le monde par
 * {@link TestChestBlockEntity} et tombe au sol quand on le casse. Développement uniquement.
 */
public class TestChestBlock extends Block implements EntityBlock {
    public static final MapCodec<TestChestBlock> CODEC = simpleCodec(TestChestBlock::new);

    public TestChestBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TestChestBlockEntity(pos, state);
    }

    /** Clic droit : le serveur ouvre le menu ; le client se contente de jouer l'animation du bras. */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hitResult) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof TestChestBlockEntity chest) {
            player.openMenu(chest);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /** Fait tomber le contenu au sol quand le coffre est cassé ou remplacé. */
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        Containers.dropContentsOnDestroy(state, newState, level, pos);
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
