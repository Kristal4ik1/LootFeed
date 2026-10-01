package dev.lootfeed.core.hud;

import dev.lootfeed.core.Canvas;
import dev.lootfeed.core.Platform;
import dev.lootfeed.core.config.Settings;
import dev.lootfeed.core.feed.EntryKind;
import dev.lootfeed.core.feed.Feed;
import dev.lootfeed.core.feed.FeedEntry;
import dev.lootfeed.core.feed.Filter;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public final class FeedRenderer {

    private static final int ROW_HEIGHT = 20;
    private static final int DISCOVERY_HEIGHT = 32;
    private static final int CHIP_HEIGHT = 14;
    private static final int GAP = 2;
    private static final int TEXT_LEFT = 25;
    private static final int NAME_LIMIT = 150;
    private static final float MIN_ALPHA = 0.04f;
    private static final float HIDDEN = 0.01f;
    private static final long SHIMMER_PERIOD = 2400L;
    private static final long GLOW_PERIOD = 1600L;

    private final Platform platform;
    private final Map<FeedEntry, Row> rows = new IdentityHashMap<>();
    private final List<Segment> chipText = new ArrayList<>();
    private long lastFrame;
    private long frames;
    private float chip;
    private float chipOffset;

    public FeedRenderer(Platform platform) {
        this.platform = platform;
    }

    public void render(Canvas canvas, Feed feed, Settings settings, Filter filter, boolean history, String notice) {
        long frame = System.nanoTime();
        float delta = lastFrame == 0L ? 0.0f : Math.min(0.1f, (frame - lastFrame) / 1.0e9f);
        lastFrame = frame;
        frames++;
        long now = System.currentTimeMillis();

        List<FeedEntry> entries = feed.entries();
        int limit = history ? settings.maxRows * 2 : settings.maxRows;
        long lifetime = settings.visibleSeconds * 1000L;

        float cursor = 0.0f;
        int shown = 0;
        int gained = 0;
        int removed = 0;
        for (FeedEntry entry : entries) {
            boolean accepted = settings.enabled && filter.accepts(entry);
            if (accepted && entry.kind() == EntryKind.PICKED_UP) {
                gained += entry.count();
            } else if (accepted) {
                removed += entry.count();
            }
            boolean wanted = accepted && shown < limit && (history || now - entry.touched() < lifetime);
            Row row = rowOf(entry);
            if (wanted) {
                row.place(cursor);
                cursor += heightOf(entry) + GAP;
                shown++;
            }
            row.animate(entry, wanted, delta, frames);
        }
        rows.values().removeIf(row -> row.frame != frames);

        boolean chipWanted = history && settings.enabled || notice != null;
        if (chipWanted) {
            chipText.clear();
            if (history && settings.enabled) {
                describeHistory(settings, filter, shown, gained, removed);
            } else {
                chipText.add(new Segment(notice, Palette.TEXT));
            }
        }
        chip = approach(chip, chipWanted ? 1.0f : 0.0f, delta, 10.0f);
        chipOffset = approach(chipOffset, cursor, delta, 14.0f);

        Layout layout = new Layout(canvas, settings);
        canvas.push();
        canvas.scale(settings.scale);
        for (int i = entries.size() - 1; i >= 0; i--) {
            FeedEntry entry = entries.get(i);
            Row row = rows.get(entry);
            if (row.placed && row.slide > HIDDEN) {
                drawEntry(canvas, layout, entry, row, now, history);
            }
        }
        if (chip > HIDDEN && !chipText.isEmpty()) {
            drawChip(canvas, layout);
        }
        canvas.pop();
    }

    private Row rowOf(FeedEntry entry) {
        Row row = rows.get(entry);
        if (row == null) {
            row = new Row(entry.count());
            rows.put(entry, row);
        }
        return row;
    }

    private void describeHistory(Settings settings, Filter filter, int shown, int gained, int removed) {
        chipText.add(new Segment(platform.translate("lootfeed.history", settings.historyMinutes), Palette.TEXT));
        if (filter != Filter.ALL) {
            chipText.add(new Segment(platform.translate("lootfeed.filter." + filter.id()), Palette.FRESH));
        }
        if (gained > 0) {
            chipText.add(new Segment("+" + gained, Palette.GAIN));
        }
        if (removed > 0) {
            chipText.add(new Segment("-" + removed, Palette.DROP));
        }
        if (shown == 0) {
            chipText.add(new Segment(platform.translate("lootfeed.empty"), Palette.MUTED));
        }
    }

    private void drawEntry(Canvas canvas, Layout layout, FeedEntry entry, Row row, long now, boolean history) {
        float alpha = ease(row.slide);
        boolean discovery = entry.discovery();
        int height = heightOf(entry);
        int textY = discovery ? 18 : 6;

        String count = (entry.kind() == EntryKind.PICKED_UP ? "+" : "-") + entry.count();
        String name = fit(canvas, entry.stack().name, NAME_LIMIT);
        String tag = tagOf(entry);
        String age = history ? ageOf(now - entry.touched()) : null;
        String caption = discovery ? platform.translate("lootfeed.discovery") : null;

        int countWidth = canvas.textWidth(count);
        int nameWidth = canvas.textWidth(name);
        int tagWidth = tag == null ? 0 : canvas.textWidth(tag) + 6;
        int ageWidth = age == null ? 0 : Math.max(canvas.textWidth(age), canvas.textWidth(platform.translate("lootfeed.time.seconds", 59)));

        int width = TEXT_LEFT + countWidth + 4 + nameWidth + 6;
        if (tag != null) {
            width += tagWidth + 5;
        }
        if (age != null) {
            width += ageWidth + 6;
        }
        if (caption != null) {
            width = Math.max(width, canvas.textWidth(caption) + 14);
        }

        canvas.push();
        canvas.translate(layout.x(width, alpha), layout.y(height, row.offset));

        panel(canvas, 0, 0, width, height, Palette.argb(discovery ? Palette.PANEL_GOLD : Palette.PANEL, 0.82f * alpha));
        canvas.fill(2, 1, width - 1, 2, Palette.argb(Palette.SHEEN, 0.07f * alpha));
        canvas.fill(0, 1, 2, height - 1, Palette.argb(accentOf(entry), alpha));
        if (discovery) {
            gild(canvas, width, height, alpha, now);
            text(canvas, caption, 7, 4, Palette.GOLD, alpha, true);
        }
        if (entry.stack().handle != null) {
            canvas.item(entry.stack().handle, 5, textY - 4);
        }

        int cursor = TEXT_LEFT;
        canvas.push();
        canvas.translate(cursor + countWidth / 2.0f, textY + 4.0f);
        canvas.scale(1.0f + 0.35f * row.pulse * row.pulse);
        text(canvas, count, -countWidth / 2, -4, discovery ? Palette.GOLD_SOFT : Palette.kind(entry.kind()), alpha, true);
        canvas.pop();
        cursor += countWidth + 4;

        text(canvas, name, cursor, textY, Palette.rarity(entry.stack().rarity), alpha, true);
        cursor += nameWidth;

        if (tag != null) {
            cursor += 5;
            boolean lost = entry.kind() == EntryKind.LOST;
            panel(canvas, cursor, textY - 2, tagWidth, 11, Palette.argb(lost ? Palette.LOSS : Palette.FRESH, 0.92f * alpha));
            text(canvas, tag, cursor + 3, textY, lost ? Palette.LOSS_TEXT : Palette.FRESH_TEXT, alpha, false);
        }
        if (age != null) {
            text(canvas, age, width - 6 - canvas.textWidth(age), textY, Palette.MUTED, alpha, true);
        }
        canvas.pop();
    }

    private void drawChip(Canvas canvas, Layout layout) {
        float alpha = ease(chip);
        int width = 12 + (chipText.size() - 1) * 6;
        for (Segment segment : chipText) {
            width += canvas.textWidth(segment.text);
        }

        canvas.push();
        canvas.translate(layout.x(width, alpha), layout.y(CHIP_HEIGHT, chipOffset));
        panel(canvas, 0, 0, width, CHIP_HEIGHT, Palette.argb(Palette.PANEL, 0.88f * alpha));
        canvas.fill(2, 1, width - 2, 2, Palette.argb(Palette.SHEEN, 0.07f * alpha));
        int cursor = 6;
        for (Segment segment : chipText) {
            text(canvas, segment.text, cursor, 3, segment.color, alpha, true);
            cursor += canvas.textWidth(segment.text) + 6;
        }
        canvas.pop();
    }

    private void gild(Canvas canvas, int width, int height, float alpha, long now) {
        float glow = 0.65f + 0.3f * (float) Math.sin(now % GLOW_PERIOD / (double) GLOW_PERIOD * Math.PI * 2.0);
        int edge = Palette.argb(Palette.GOLD, glow * alpha);
        canvas.fill(1, 0, width - 1, 1, edge);
        canvas.fill(1, height - 1, width - 1, height, edge);
        canvas.fill(width - 1, 1, width, height - 1, edge);

        float phase = now % SHIMMER_PERIOD / (float) SHIMMER_PERIOD;
        int center = Math.round(-40.0f + phase * (width + 80.0f));
        for (int i = -12; i < 12; i++) {
            int column = center + i;
            if (column >= 2 && column < width - 1) {
                float strength = 1.0f - Math.abs(i + 0.5f) / 12.0f;
                canvas.fill(column, 1, column + 1, height - 1, Palette.argb(Palette.GOLD_SOFT, 0.22f * strength * alpha));
            }
        }
    }

    private String tagOf(FeedEntry entry) {
        if (entry.kind() == EntryKind.LOST) {
            return platform.translate("lootfeed.tag.lost");
        }
        if (entry.kind() == EntryKind.PICKED_UP && entry.fresh()) {
            return platform.translate("lootfeed.tag.new");
        }
        return null;
    }

    private String ageOf(long millis) {
        long seconds = Math.max(0L, millis / 1000L);
        if (seconds < 60L) {
            return platform.translate("lootfeed.time.seconds", seconds);
        }
        return platform.translate("lootfeed.time.minutes", seconds / 60L);
    }

    private static int accentOf(FeedEntry entry) {
        if (entry.discovery()) {
            return Palette.GOLD;
        }
        if (entry.kind() == EntryKind.PICKED_UP && entry.stack().rarity > 0) {
            return Palette.rarity(entry.stack().rarity);
        }
        return Palette.kind(entry.kind());
    }

    private static int heightOf(FeedEntry entry) {
        return entry.discovery() ? DISCOVERY_HEIGHT : ROW_HEIGHT;
    }

    private static String fit(Canvas canvas, String value, int limit) {
        if (canvas.textWidth(value) <= limit) {
            return value;
        }
        String cut = value;
        while (cut.length() > 1 && canvas.textWidth(cut + "...") > limit) {
            cut = cut.substring(0, cut.length() - 1);
        }
        return cut + "...";
    }

    private static void text(Canvas canvas, String value, int x, int y, int rgb, float alpha, boolean shadow) {
        if (alpha > MIN_ALPHA) {
            canvas.text(value, x, y, Palette.argb(rgb, alpha), shadow);
        }
    }

    private static void panel(Canvas canvas, int x, int y, int width, int height, int argb) {
        canvas.fill(x + 1, y, x + width - 1, y + 1, argb);
        canvas.fill(x, y + 1, x + width, y + height - 1, argb);
        canvas.fill(x + 1, y + height - 1, x + width - 1, y + height, argb);
    }

    private static float approach(float value, float target, float delta, float rate) {
        float next = value + (target - value) * (1.0f - (float) Math.exp(-rate * delta));
        return Math.abs(target - next) < 0.005f ? target : next;
    }

    private static float ease(float t) {
        float inverse = 1.0f - Math.max(0.0f, Math.min(1.0f, t));
        return 1.0f - inverse * inverse * inverse;
    }

    private static final class Row {

        float slide;
        float offset;
        float target;
        float pulse;
        boolean placed;
        long frame;
        private int count;

        Row(int count) {
            this.count = count;
        }

        void place(float slot) {
            target = slot;
            if (!placed) {
                placed = true;
                offset = slot;
            }
        }

        void animate(FeedEntry entry, boolean wanted, float delta, long frame) {
            this.frame = frame;
            if (count != entry.count()) {
                count = entry.count();
                pulse = 1.0f;
            }
            slide = approach(slide, wanted ? 1.0f : 0.0f, delta, 9.0f);
            offset = approach(offset, target, delta, 14.0f);
            pulse = Math.max(0.0f, pulse - delta * 3.5f);
        }
    }

    private static final class Layout {

        private final Anchor anchor;
        private final int width;
        private final int height;
        private final int offsetX;
        private final int offsetY;

        Layout(Canvas canvas, Settings settings) {
            this.anchor = Anchor.parse(settings.anchor);
            this.width = (int) (canvas.width() / settings.scale);
            this.height = (int) (canvas.height() / settings.scale);
            this.offsetX = settings.offsetX;
            this.offsetY = settings.offsetY;
        }

        float x(int elementWidth, float visibility) {
            float hidden = (1.0f - visibility) * (elementWidth + offsetX + 8);
            return anchor.right ? width - offsetX - elementWidth + hidden : offsetX - hidden;
        }

        float y(int elementHeight, float stackOffset) {
            return anchor.bottom ? height - offsetY - elementHeight - stackOffset : offsetY + stackOffset;
        }
    }

    private static final class Segment {

        final String text;
        final int color;

        Segment(String text, int color) {
            this.text = text;
            this.color = color;
        }
    }
}
