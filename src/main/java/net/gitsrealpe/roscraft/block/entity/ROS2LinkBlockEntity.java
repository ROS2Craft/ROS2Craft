package net.gitsrealpe.roscraft.block.entity;

import javax.annotation.Nullable;

import edu.wpi.rail.jrosbridge.Ros;
import net.gitsrealpe.roscraft.ROScraft;
import net.gitsrealpe.roscraft.screen.custom.ROS2LinkMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ROS2LinkBlockEntity extends BlockEntity implements MenuProvider {

    private String ws_address = "localhost";
    private Ros ros;
    private boolean hasConnection = false;

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
        return hasConnection;
    }

    public boolean connectROS() {
        ros = new Ros(this.ws_address);
        try {
            ros.connect();
            ROScraft.LOGGER.info("ROS connection established");
            this.hasConnection = true;
            return true;
        } catch (Exception e) {
            ROScraft.LOGGER.error("Failed to connect to ROS: ", e);
            return false;
        }
    }

    public boolean disconnectROS() {
        ROScraft.LOGGER.info("ROS connection closed");
        this.hasConnection = false;
        return ros.disconnect();
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
