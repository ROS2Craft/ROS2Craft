package net.gitsrealpe.roscraft.item.custom;

import java.util.List;

import net.gitsrealpe.roscraft.block.entity.ROS2LinkBlockEntity;
import net.gitsrealpe.roscraft.component.ModDataComponents;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class RobotItem extends Item {
    public EntityType<?> robot_entype;

    public RobotItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        ItemStack stack = context.getItemInHand();
        BlockEntity be = level.getBlockEntity(context.getClickedPos());
        // block pos as indirect reference to ros2linkBE
        if (be instanceof ROS2LinkBlockEntity ros2LinkBE) {
            if (!level.isClientSide()) {
                level.playSound(null, context.getClickedPos(), SoundEvents.NOTE_BLOCK_HARP.value(), SoundSource.BLOCKS,
                        1.0F, 1.0F);
                stack.set(ModDataComponents.COORDINATES, ros2LinkBE.getBlockPos());
            }
            return InteractionResult.SUCCESS;
        }

        BlockPlaceContext blockPlaceContext = new BlockPlaceContext(context);
        BlockPos blockPos = blockPlaceContext.getClickedPos();
        Vec3 pos = Vec3.atBottomCenterOf(blockPos);
        AABB aabb = this.robot_entype.getDimensions().makeBoundingBox(pos.x(), pos.y(), pos.z());
        if (!level.noCollision(null, aabb) || !level.getEntities(null, aabb).isEmpty())
            return InteractionResult.FAIL;

        // stack.shrink(1);
        return InteractionResult.sidedSuccess(level.isClientSide);
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
