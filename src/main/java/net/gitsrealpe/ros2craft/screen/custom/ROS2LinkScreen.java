package net.gitsrealpe.ros2craft.screen.custom;

import com.mojang.blaze3d.systems.RenderSystem;

import net.gitsrealpe.ros2craft.ROS2Craft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ROS2LinkScreen extends AbstractContainerScreen<ROS2LinkMenu> {
    private static final ResourceLocation GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath(ROS2Craft.MODID,
            "textures/gui/ros2link/ros2link_gui.png");

    private EditBox textBox;
    private Button button;

    public ROS2LinkScreen(ROS2LinkMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();
        clearWidgets();

        // Create text box (adjust x,y,width,height to your GUI)
        this.textBox = new EditBox(this.font, this.leftPos + 34, this.topPos + 32, 60, 14,
                Component.literal("Enter text"));
        this.textBox.setCanLoseFocus(false);
        // this.textBox.setBordered(false);
        this.textBox.setMaxLength(32);
        // this.textBox.setResponder(this::onWSChanged);
        this.textBox.setValue(this.menu.blockEntity.getWsAddress());
        this.textBox.setTooltip(Tooltip.create(
                Component.literal("Set up the WebSocket address to the rosbridge_server")));
        this.addRenderableWidget(this.textBox);

        String state = "Connect";
        // if (!this.menu.blockEntity.hasConnection()) {
        // state = "Connect";
        // } else {
        // state = "Disconnect";
        // }
        this.button = addRenderableWidget(
                Button.builder(Component.literal(state), this::handleConnectButton)
                        .bounds(this.leftPos + 100, this.topPos + 140, 60, 20)
                        // .tooltip(Tooltip.create(Component.literal("Apply Changes")))
                        .build());
    }

    private void handleConnectButton(Button button) {
        // if (!this.menu.blockEntity.hasConnection()) {
        // this.menu.blockEntity.setWsAddress(this.textBox.getValue());
        // this.button.setMessage(Component.literal("Connecting..."));
        // if (this.menu.blockEntity.connectROS()) {
        // this.button.setMessage(Component.literal("Disconnect"));
        // }
        // } else {
        // if (this.menu.blockEntity.disconnectROS()) {
        // this.button.setMessage(Component.literal("Connect"));
        // }
        // }
    }

    private void onWSChanged(String address) {
        ROS2Craft.LOGGER.info("WebSocket Address set: " + address);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            this.minecraft.player.closeContainer();
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, GUI_TEXTURE);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(GUI_TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        guiGraphics.drawString(this.font, "WebSocket address",
                this.leftPos + 8, this.topPos + 21, 0xFFFFFF);
        guiGraphics.drawString(this.font, "ws://",
                this.leftPos + 8, this.topPos + 35, 0xFFFFFF);

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

}
