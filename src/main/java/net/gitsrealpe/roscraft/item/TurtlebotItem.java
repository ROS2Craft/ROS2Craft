package net.gitsrealpe.roscraft.item;

import java.util.function.Consumer;

import net.gitsrealpe.roscraft.entity.ModEntities;
import net.gitsrealpe.roscraft.entity.custom.TurtlebotEntity;
import net.gitsrealpe.roscraft.item.custom.RobotItem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public class TurtlebotItem extends RobotItem {

    public TurtlebotItem(Properties properties) {
        super(properties);
        this.robot_entype = ModEntities.TURTLEBOT.get();
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        super.useOn(context);
        Level level = context.getLevel();
        BlockPlaceContext blockPlaceContext = new BlockPlaceContext(context);
        BlockPos blockPos = blockPlaceContext.getClickedPos();
        ItemStack itemStack = context.getItemInHand();

        if (level instanceof ServerLevel serverLevel) {
            Consumer<TurtlebotEntity> consumer = EntityType.createDefaultStackConfig(serverLevel, itemStack,
                    context.getPlayer());
            TurtlebotEntity turtlebot = ModEntities.TURTLEBOT.get()
                    .create(serverLevel, consumer, blockPos, MobSpawnType.SPAWN_EGG, true, true);
            if (turtlebot == null) {
                return InteractionResult.FAIL;
            }

            // place oriented
            float f = (float) Mth.floor((Mth.wrapDegrees(context.getRotation() - 180.0F) + 22.5F) / 45.0F) * 45.0F;
            turtlebot.moveTo(turtlebot.getX(), turtlebot.getY(), turtlebot.getZ(), f, 0.0F);
            serverLevel.addFreshEntityWithPassengers(turtlebot);
            // serverLevel.addFreshEntity(turtlebot);
            turtlebot.playSound(SoundEvents.SMITHING_TABLE_USE, 0.9f, 1.3f);
            turtlebot.gameEvent(GameEvent.ENTITY_PLACE, context.getPlayer());
        }

        itemStack.shrink(1);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

}
