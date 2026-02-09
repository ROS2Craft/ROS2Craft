package net.gitsrealpe.roscraft.item;

import java.util.function.Consumer;

import net.gitsrealpe.roscraft.entity.ModEntities;
import net.gitsrealpe.roscraft.entity.custom.TurtlebotEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class TurtlebotItem extends Item {

    public TurtlebotItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPlaceContext blockPlaceContext = new BlockPlaceContext(context);
        BlockPos blockPos = blockPlaceContext.getClickedPos();
        ItemStack itemStack = context.getItemInHand();
        Vec3 pos = Vec3.atBottomCenterOf(blockPos);
        AABB aabb = ModEntities.TURTLEBOT.get().getDimensions().makeBoundingBox(pos.x(), pos.y(), pos.z());
        if (!level.noCollision(null, aabb) || !level.getEntities(null, aabb).isEmpty())
            return InteractionResult.FAIL;

        if (level instanceof ServerLevel serverLevel) {
            Consumer<TurtlebotEntity> consumer = EntityType.createDefaultStackConfig(serverLevel, itemStack,
                    context.getPlayer());
            TurtlebotEntity turtlebot = ModEntities.TURTLEBOT.get()
                    .create(serverLevel, consumer, blockPos, MobSpawnType.SPAWN_EGG, true, true);
            if (turtlebot == null) {
                return InteractionResult.FAIL;
            }

            float f = (float) Mth.floor((Mth.wrapDegrees(context.getRotation() - 180.0F) + 22.5F) / 45.0F) * 45.0F;
            turtlebot.moveTo(turtlebot.getX(), turtlebot.getY(), turtlebot.getZ(), f, 0.0F);
            serverLevel.addFreshEntityWithPassengers(turtlebot);
            turtlebot.playSound(SoundEvents.SMITHING_TABLE_USE, 0.9f, 1.3f);
            turtlebot.gameEvent(GameEvent.ENTITY_PLACE, context.getPlayer());
        }

        itemStack.shrink(1);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

}
