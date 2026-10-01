package dev.lootfeed.client;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

final class DepthBuffer {

    private DepthBuffer() {
    }

    static void clear() {
        GlStateManager.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
    }
}
