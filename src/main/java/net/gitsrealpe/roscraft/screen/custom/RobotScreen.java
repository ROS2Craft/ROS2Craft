package net.gitsrealpe.roscraft.screen.custom;

import java.util.UUID;

import com.mojang.blaze3d.systems.RenderSystem;

import net.gitsrealpe.roscraft.ROScraft;
import net.gitsrealpe.roscraft.entity.custom.RobotEntity;
import net.minecraft.Util;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

public class RobotScreen extends AbstractContainerScreen<RobotMenu> {
    private static final ResourceLocation GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath(ROScraft.MODID,
            "textures/gui/pedestal/pedestal_gui.png");

    private RobotEntity entity;
    private String ownerName;

    public RobotScreen(RobotMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    public void init() {
        super.init();
        if (this.menu.entityId != 0 && minecraft.level != null) {
            this.entity = (RobotEntity) minecraft.level.getEntity(this.menu.entityId);

            UUID ownerUuid = this.entity.getOwnerUUID();
            if (ownerUuid != Util.NIL_UUID) {
                this.ownerName = this.entity.getOwnerName();
            } else {
                this.ownerName = "Robot has no owner";
            }
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, GUI_TEXTURE);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(GUI_TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
        guiGraphics.drawString(this.font, ownerName, this.leftPos + 8, this.topPos + 21, 0xFFFFFF);

        float health = this.entity.getHealth();
        int fullHearts = Mth.ceil(health / 2.0F);
        boolean hasHalfHeart = (health % 2.0F) > 0.0F;

        int heartX = 8;
        int heartY = 6;

        if (this.entity != null && this.entity.isAlive()) {
            for (int i = 0; i < fullHearts; ++i) {
                boolean isHalf = hasHalfHeart && i == (fullHearts - 1);
                ResourceLocation heartSprite = Gui.HeartType.NORMAL.getSprite(false, isHalf, false);
                guiGraphics.blitSprite(heartSprite, heartX + i * 8, heartY, 0, 9, 9);
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

}
