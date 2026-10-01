package dev.lootfeed.client;

import com.mojang.blaze3d.systems.RenderSystem;
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
        return client.getWindow().getGuiScaledWidth();
    }

    @Override
    public int height() {
        return client.getWindow().getGuiScaledHeight();
    }

    @Override
    public void push() {
        RenderSystem.pushMatrix();
    }

    @Override
    public void pop() {
        RenderSystem.popMatrix();
    }

    @Override
    public void translate(float x, float y) {
        RenderSystem.translatef(x, y, 0.0f);
    }

    @Override
    public void scale(float factor) {
        RenderSystem.scalef(factor, factor, 1.0f);
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
        client.getItemRenderer().renderGuiItem((ItemStack) stack, x, y);
    }
}
