package dev.lootfeed.core.hud;

import dev.lootfeed.core.feed.EntryKind;

final class Palette {

    static final int PANEL = 0x0F1117;
    static final int PANEL_GOLD = 0x241B08;
    static final int SHEEN = 0xFFFFFF;
    static final int TEXT = 0xF1F3F8;
    static final int MUTED = 0x9AA1B2;
    static final int GAIN = 0x5FE08A;
    static final int DROP = 0xF4B860;
    static final int LOSS = 0xF2636B;
    static final int FRESH = 0x45D4FF;
    static final int FRESH_TEXT = 0x04222D;
    static final int LOSS_TEXT = 0x2B0507;
    static final int GOLD = 0xFFCB45;
    static final int GOLD_SOFT = 0xFFE9A8;

    private static final int[] RARITY = {TEXT, 0xFFF07A, 0x6FE7FF, 0xF08CFF};

    private Palette() {
    }

    static int rarity(int level) {
        return RARITY[Math.max(0, Math.min(RARITY.length - 1, level))];
    }

    static int kind(EntryKind kind) {
        switch (kind) {
            case DROPPED:
                return DROP;
            case LOST:
                return LOSS;
            default:
                return GAIN;
        }
    }

    static int argb(int rgb, float alpha) {
        int a = Math.round(Math.max(0.0f, Math.min(1.0f, alpha)) * 255.0f);
        return a << 24 | rgb & 0xFFFFFF;
    }
}
