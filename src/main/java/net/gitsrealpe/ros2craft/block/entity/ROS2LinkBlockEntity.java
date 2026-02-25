package net.gitsrealpe.ros2craft.block.entity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import edu.wpi.rail.jrosbridge.Ros;
import net.gitsrealpe.ros2craft.ROS2Craft;
import net.gitsrealpe.ros2craft.entity.custom.RobotEntity;
import net.gitsrealpe.ros2craft.entity.custom.TurtlebotEntity;
import net.gitsrealpe.ros2craft.screen.custom.ROS2LinkMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ROS2LinkBlockEntity extends BlockEntity implements MenuProvider {

    private String ws_address = "localhost";
    private Map<UUID, RobotEntity> robotList = new HashMap<>();

    public ROS2LinkBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ROS2LINK_BE.get(), pos, state);
    }

    public String getWsAddress() {
        return this.ws_address;
    }

    public void setWsAddress(String ws_address) {
        this.ws_address = ws_address;
    }

    public boolean hasConnection() {
        // return hasConnection;
        return true;
    }

    public boolean connectROS() {
        // ros = new Ros(this.ws_address);
        try {
            // ros.connect();
            ROS2Craft.LOGGER.info("ROS connection established");
            // this.hasConnection = true;
            return true;
        } catch (Exception e) {
            ROS2Craft.LOGGER.error("Failed to connect to ROS: ", e);
            return false;
        }
    }

    public boolean disconnectROS() {
        ROS2Craft.LOGGER.info("ROS connection closed");
        // this.hasConnection = false;
        // return ros.disconnect();
        return true;
    }

    // @Override
    // public void setRemoved() {
    // disconnectROS();
    // super.setRemoved();
    // }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("ws_address", this.ws_address);

    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.ws_address = tag.getString("ws_address");
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("ROS2 Link");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ROS2LinkMenu(containerId, playerInventory, this);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider pRegistries) {
        // return saveWithoutMetadata(pRegistries);
        CompoundTag tag = super.getUpdateTag(pRegistries);
        return tag;
    }

}
