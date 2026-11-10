package com.galdurstudio.galdurui.testmod.neoforge;

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
 * Test chest: a placeable block that opens a {@link TestChestMenu} in a
 * {@code GaldurContainerScreen}. Its contents are saved with the world and dropped when it breaks.
 */
public class TestChestBlock extends Block implements EntityBlock {
    //? if <26.1
    public static final MapCodec<TestChestBlock> CODEC = simpleCodec(TestChestBlock::new);

    public TestChestBlock(Properties properties) {
        super(properties);
    }

    //? if <26.1 {
    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }
    //?}

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TestChestBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hitResult) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof TestChestBlockEntity chest) {
            player.openMenu(chest);
        }
        //? if >=26.1 {
        /*return InteractionResult.SUCCESS;
        *///?} else {
        return InteractionResult.sidedSuccess(level.isClientSide());
        //?}
    }

    // Since 26.1 a block entity that is a container drops its contents by itself.
    //? if <26.1 {
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        Containers.dropContentsOnDestroy(state, newState, level, pos);
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
    //?}
}
