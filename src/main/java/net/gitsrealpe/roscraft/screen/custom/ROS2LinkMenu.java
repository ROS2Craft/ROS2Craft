package net.gitsrealpe.roscraft.screen.custom;

import net.gitsrealpe.roscraft.block.ModBlocks;
import net.gitsrealpe.roscraft.block.entity.ROS2LinkBlockEntity;
import net.gitsrealpe.roscraft.screen.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ROS2LinkMenu extends AbstractContainerMenu {
    public final ROS2LinkBlockEntity blockEntity;
    private final Level level;

    public ROS2LinkMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    // TODO: remove this? BLOCK DOENST CARRY INVENTORY
    public ROS2LinkMenu(int containerId, Inventory inv, BlockEntity blockEntity) {
        super(ModMenuTypes.ROS2LINK_MENU.get(), containerId);
        this.blockEntity = ((ROS2LinkBlockEntity) blockEntity);
        this.level = inv.player.level();
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), player,
                ModBlocks.ROS2LINK_BLOCK.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'quickMoveStack'");
    }

}
