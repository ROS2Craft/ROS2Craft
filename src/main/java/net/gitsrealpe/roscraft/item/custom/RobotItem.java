package net.gitsrealpe.roscraft.item.custom;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import net.gitsrealpe.roscraft.block.entity.ROS2LinkBlockEntity;
import net.gitsrealpe.roscraft.component.ModDataComponents;
import net.gitsrealpe.roscraft.entity.custom.RobotEntity;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class RobotItem extends Item {
    public EntityType<?> robot_entype;
    public RobotEntity robot;

    public RobotItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        ItemStack stack = context.getItemInHand();
        BlockEntity be = level.getBlockEntity(context.getClickedPos());
        // Link the item to ROSLink block pos as indirect reference to ros2linkBE
        if (be instanceof ROS2LinkBlockEntity ros2LinkBE) {
            if (!level.isClientSide()) {
                level.playSound(null, context.getClickedPos(), SoundEvents.NOTE_BLOCK_HARP.value(),
                        SoundSource.BLOCKS, 1.0F, 1.0F);
                stack.set(ModDataComponents.COORDINATES, ros2LinkBE.getBlockPos());
                stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
            }
            return InteractionResult.SUCCESS;
        }

        return tryPlaceRobot(context);
    }

    protected InteractionResult tryPlaceRobot(UseOnContext context) {
        Level level = context.getLevel();
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        // ... check valid bbox for placememnt
        BlockPlaceContext blockPlaceContext = new BlockPlaceContext(context);
        BlockPos blockPos = blockPlaceContext.getClickedPos();
        Vec3 pos = Vec3.atBottomCenterOf(blockPos);
        AABB aabb = robot_entype.getDimensions().makeBoundingBox(pos.x(), pos.y(), pos.z());
        if (!level.noCollision(null, aabb) || !level.getEntities(null, aabb).isEmpty()) {
            return InteractionResult.FAIL;
        }

        ItemStack stack = context.getItemInHand();
        // build and create the robot entity
        Consumer<RobotEntity> consumer = EntityType.createDefaultStackConfig(serverLevel, stack, context.getPlayer());
        RobotEntity robot = (RobotEntity) robot_entype.create(serverLevel);
        if (robot == null)
            return InteractionResult.FAIL;
        consumer.accept(robot);
        // set the fixed frame and ownership of the robot
        robot.setFixedFrame(stack.get(ModDataComponents.COORDINATES));
        if (context.getPlayer() != null) {
            robot.setOwnerUUID(context.getPlayer().getUUID());
        }
        // entity placement after creation
        float rotation = (float) Mth.floor((Mth.wrapDegrees(context.getRotation()) + 22.5F) / 45.0F) * 45.0F;
        robot.moveTo(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5, rotation, 0.0F);
        serverLevel.addFreshEntityWithPassengers(robot);

        // Spawn sound effect
        SoundType soundType = level.getBlockState(blockPos).getSoundType(level, blockPos, robot);
        robot.playSound(soundType.getBreakSound(), 0.9f, 1.3f);

        robot.gameEvent(GameEvent.ENTITY_PLACE, context.getPlayer());
        stack.shrink(1);
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents,
            TooltipFlag tooltipFlag) {

        if (Screen.hasShiftDown()) {
            if (stack.get(ModDataComponents.COORDINATES) != null) {
                tooltipComponents
                        .add(Component.literal("Fixed frame set at: \n" +
                                "§7X:" + stack.get(ModDataComponents.COORDINATES).getX() + ", " +
                                "Y:" + stack.get(ModDataComponents.COORDINATES).getY() + ", " +
                                "Z:" + stack.get(ModDataComponents.COORDINATES).getZ() + "\n" +
                                "§nin Minecraft Coordinates."));
            } else {
                tooltipComponents.add(Component.literal("Robot not linked to ROS2"));
            }
        } else {
            tooltipComponents.add(Component.translatable("tooltip.roscraft.robot_info"));
        }

        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

}
