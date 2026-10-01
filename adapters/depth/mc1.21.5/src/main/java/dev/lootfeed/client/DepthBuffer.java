package dev.lootfeed.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;

final class DepthBuffer {

    private DepthBuffer() {
    }

    static void clear() {
        RenderSystem.getDevice()
                .createCommandEncoder()
                .clearDepthTexture(Minecraft.getInstance().getMainRenderTarget().getDepthTexture(), 1.0);
    }
}
