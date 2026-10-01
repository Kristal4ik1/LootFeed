package dev.lootfeed.client;

import dev.lootfeed.core.Canvas;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

public final class DrawCanvas implements Canvas {

    private final GuiGraphics graphics;
    private final Font font;

    private DrawCanvas(GuiGraphics graphics, Font font) {
        this.graphics = graphics;
        this.font = font;
    }

    public static DrawCanvas begin(GuiGraphics graphics) {
        graphics.flush();
        DepthBuffer.clear();
        return new DrawCanvas(graphics, Minecraft.getInstance().font);
    }

    @Override
    public int width() {
        return graphics.guiWidth();
    }

    @Override
    public int height() {
        return graphics.guiHeight();
    }

    @Override
    public void push() {
        graphics.pose().pushPose();
    }

    @Override
    public void pop() {
        graphics.pose().popPose();
    }

    @Override
    public void translate(float x, float y) {
        graphics.pose().translate(x, y, 0.0f);
    }

    @Override
    public void scale(float factor) {
        graphics.pose().scale(factor, factor, 1.0f);
    }

    @Override
    public void fill(int left, int top, int right, int bottom, int argb) {
        graphics.fill(left, top, right, bottom, argb);
    }

    @Override
    public int textWidth(String text) {
        return font.width(text);
    }

    @Override
    public void text(String text, int x, int y, int argb, boolean shadow) {
        graphics.drawString(font, text, x, y, argb, shadow);
    }

    @Override
    public void item(Object stack, int x, int y) {
        graphics.renderItem((ItemStack) stack, x, y);
    }
}
