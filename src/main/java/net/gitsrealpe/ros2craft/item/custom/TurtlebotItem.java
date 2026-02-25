package net.gitsrealpe.ros2craft.item.custom;

import net.gitsrealpe.ros2craft.entity.ModEntities;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class TurtlebotItem extends RobotItem {

    public TurtlebotItem(Properties properties) {
        super(properties);
        this.robot_entype = ModEntities.TURTLEBOT.get();
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        super.useOn(context);
        Level level = context.getLevel();
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

}
