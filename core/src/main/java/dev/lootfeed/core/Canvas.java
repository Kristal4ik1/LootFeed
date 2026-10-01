package dev.lootfeed.core;

public interface Canvas {

    int width();

    int height();

    void push();

    void pop();

    void translate(float x, float y);

    void scale(float factor);

    void fill(int left, int top, int right, int bottom, int argb);

    int textWidth(String text);

    void text(String text, int x, int y, int argb, boolean shadow);

    void item(Object stack, int x, int y);
}
