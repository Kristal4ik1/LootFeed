package dev.lootfeed.legacy;

import dev.lootfeed.core.Canvas;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;

final class DrawCanvas implements Canvas {

    private final Minecraft client;
    private final ScaledResolution resolution;

    DrawCanvas(Minecraft client, ScaledResolution resolution) {
        this.client = client;
        this.resolution = resolution;
    }

    @Override
    public int width() {
        return resolution.getScaledWidth();
    }

    @Override
    public int height() {
        return resolution.getScaledHeight();
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
        GlStateManager.translate(x, y, 0.0f);
    }

    @Override
    public void scale(float factor) {
        GlStateManager.scale(factor, factor, 1.0f);
    }

    @Override
    public void fill(int left, int top, int right, int bottom, int argb) {
        Gui.drawRect(left, top, right, bottom, argb);
    }

    @Override
    public int textWidth(String text) {
        return client.fontRenderer.getStringWidth(text);
    }

    @Override
    public void text(String text, int x, int y, int argb, boolean shadow) {
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);
        client.fontRenderer.drawString(text, x, y, argb, shadow);
        GlStateManager.disableBlend();
    }

    @Override
    public void item(Object stack, int x, int y) {
        GlStateManager.enableRescaleNormal();
        RenderHelper.enableGUIStandardItemLighting();
        client.getRenderItem().renderItemAndEffectIntoGUI((ItemStack) stack, x, y);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
    }
}
