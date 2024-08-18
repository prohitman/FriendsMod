package com.prohitman.friendsmod.common.entity.goals;

import com.prohitman.friendsmod.common.entity.MimicEntity;
import com.prohitman.friendsmod.loot.LootUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.event.EventHooks;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class PlaceBlockGoal extends Goal {

    private final MimicEntity mimic;

    public PlaceBlockGoal(MimicEntity mimic) {
        this.mimic = mimic;
    }

    public boolean canUse() {
        return EventHooks.canEntityGrief(this.mimic.level(), this.mimic) && this.mimic.getRandom().nextInt(reducedTickDelay(1500)) == 0;
    }

    public void tick() {
        if(!this.mimic.level().isClientSide){
            RandomSource randomsource = this.mimic.getRandom();
            Level level = this.mimic.level();
            int i = Mth.floor(this.mimic.getX() - 1.0 + randomsource.nextDouble() * 2.0);
            int j = Mth.floor(this.mimic.getY() + randomsource.nextDouble() * 2.0);
            int k = Mth.floor(this.mimic.getZ() - 1.0 + randomsource.nextDouble() * 2.0);
            Direction direction = Direction.getRandom(this.mimic.level().random);
            BlockPos blockpos = new BlockPos(i, j, k).relative(direction);
            BlockState blockstate = level.getBlockState(blockpos);
            BlockPos blockpos1 = blockpos.below();
            BlockState blockstate1 = level.getBlockState(blockpos1);

            ItemStack itemStack;

            LootTable lootTable = LootUtil.getSpawnWithLootTable((ServerLevel) this.mimic.level(), mimic, "_placeable");
            LootContext lootContext = LootUtil.createSpawnWithContext((ServerLevel) this.mimic.level(), mimic, lootTable);
            Optional<ItemStack> optionalItemStack = LootUtil.generateSingleItem(lootTable, lootContext, "mimic_placeable");
            itemStack = optionalItemStack.orElseGet(() -> new ItemStack(Items.DIRT));

            BlockState blockstate2 = ((BlockItem)itemStack.getItem()).getBlock().defaultBlockState();
            if (blockstate2 != null) {
                blockstate2 = Block.updateFromNeighbourShapes(blockstate2, this.mimic.level(), blockpos);
                if (this.canPlaceBlock(level, blockpos, blockstate2, blockstate, blockstate1, blockpos1) && !EventHooks.onBlockPlace(this.mimic, BlockSnapshot.create(level.dimension(), level, blockpos1), Direction.UP)) {
                    level.setBlock(blockpos, blockstate2, 3);
                    level.playSound(null, blockpos, blockstate2.getSoundType().getBreakSound(), SoundSource.BLOCKS, 1,1);
                    mimic.swing(InteractionHand.MAIN_HAND);
                    level.gameEvent(GameEvent.BLOCK_PLACE, blockpos, GameEvent.Context.of(this.mimic, blockstate2));
                }
            }
        }
    }

    private boolean canPlaceBlock(Level level, BlockPos destinationPos, BlockState carriedState, BlockState destinationState, BlockState belowDestinationState, BlockPos belowDestinationPos) {
        return level.getBlockState(destinationPos.above(2)).isAir() && carriedState.canSurvive(level, destinationPos) && destinationState.isAir() && !belowDestinationState.isAir() && !belowDestinationState.is(Blocks.BEDROCK) && !belowDestinationState.is(net.neoforged.neoforge.common.Tags.Blocks.ENDERMAN_PLACE_ON_BLACKLIST) && belowDestinationState.isCollisionShapeFullBlock(level, belowDestinationPos) && carriedState.canSurvive(level, destinationPos) && level.getEntities(this.mimic, AABB.unitCubeFromLowerCorner(Vec3.atLowerCornerOf(destinationPos))).isEmpty();
    }
}
