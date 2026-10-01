package dev.lootfeed.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.lootfeed.core.Canvas;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.world.item.ItemStack;

public final class DrawCanvas implements Canvas {

    private final Minecraft client = Minecraft.getInstance();
    private final Font font = client.font;
    private final PoseStack pose;

    private DrawCanvas(PoseStack pose) {
        this.pose = pose;
    }

    public static DrawCanvas begin(PoseStack pose) {
        DepthBuffer.clear();
        return new DrawCanvas(pose);
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
        pose.pushPose();
    }

    @Override
    public void pop() {
        pose.popPose();
    }

    @Override
    public void translate(float x, float y) {
        pose.translate(x, y, 0.0);
    }

    @Override
    public void scale(float factor) {
        pose.scale(factor, factor, 1.0f);
    }

    @Override
    public void fill(int left, int top, int right, int bottom, int argb) {
        GuiComponent.fill(pose, left, top, right, bottom, argb);
    }

    @Override
    public int textWidth(String text) {
        return font.width(text);
    }

    @Override
    public void text(String text, int x, int y, int argb, boolean shadow) {
        if (shadow) {
            font.drawShadow(pose, text, x, y, argb);
        } else {
            font.draw(pose, text, x, y, argb);
        }
    }

    @Override
    public void item(Object stack, int x, int y) {
        ItemIcons.draw(pose, (ItemStack) stack, x, y);
    }
}
