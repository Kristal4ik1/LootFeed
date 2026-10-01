package dev.lootfeed.client;

import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11;

final class DepthBuffer {

    private DepthBuffer() {
    }

    static void clear() {
        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT);
    }
}
