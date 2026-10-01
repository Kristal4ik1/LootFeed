package dev.lootfeed.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import dev.lootfeed.core.Canvas;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.world.item.ItemStack;

public final class DrawCanvas implements Canvas {

    private final Minecraft client = Minecraft.getInstance();
    private final Font font = client.font;

    private DrawCanvas() {
    }

    public static DrawCanvas begin() {
        DepthBuffer.clear();
        return new DrawCanvas();
    }

    @Override
    public int width() {
        return client.window.getGuiScaledWidth();
    }

    @Override
    public int height() {
        return client.window.getGuiScaledHeight();
    }

    @Override
    public void push() {
        GlStateManager.pushMatrix();
    }

    @Override
    public void pop() {
        GlStateManager.popMatrix();
    }

    @Override
    public void translate(float x, float y) {
        GlStateManager.translatef(x, y, 0.0f);
    }

    @Override
    public void scale(float factor) {
        GlStateManager.scalef(factor, factor, 1.0f);
    }

    @Override
    public void fill(int left, int top, int right, int bottom, int argb) {
        GuiComponent.fill(left, top, right, bottom, argb);
    }

    @Override
    public int textWidth(String text) {
        return font.width(text);
    }

    @Override
    public void text(String text, int x, int y, int argb, boolean shadow) {
        if (shadow) {
            font.drawShadow(text, x, y, argb);
        } else {
            font.draw(text, x, y, argb);
        }
    }

    @Override
    public void item(Object stack, int x, int y) {
        GlStateManager.enableRescaleNormal();
        Lighting.turnOnGui();
        client.getItemRenderer().renderGuiItem((ItemStack) stack, x, y);
        Lighting.turnOff();
        GlStateManager.disableRescaleNormal();
    }
}
