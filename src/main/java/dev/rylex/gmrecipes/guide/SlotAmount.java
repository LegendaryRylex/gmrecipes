package dev.rylex.gmrecipes.guide;

import guideme.document.LytRect;
import guideme.document.block.LytSlot;
import guideme.render.RenderContext;
import java.math.BigDecimal;
import java.math.RoundingMode;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Amounts are in millibuckets. */
final class SlotAmount {

    private static final BigDecimal THOUSAND = BigDecimal.valueOf(1000);
    private static final int TEXT_RIGHT = LytSlot.OUTER_SIZE;
    private static final int TEXT_TOP = 10;

    private SlotAmount() {}

    static String format(long milliBuckets) {
        if (milliBuckets < 1000) {
            return Long.toString(milliBuckets);
        }
        BigDecimal buckets = BigDecimal.valueOf(milliBuckets).divide(THOUSAND, 1, RoundingMode.DOWN);
        if (buckets.compareTo(THOUSAND) >= 0) {
            return buckets.divide(THOUSAND, 1, RoundingMode.DOWN)
                            .stripTrailingZeros()
                            .toPlainString()
                    + "kB";
        }
        return buckets.stripTrailingZeros().toPlainString() + "B";
    }

    static void draw(RenderContext context, LytRect bounds, long milliBuckets) {
        if (milliBuckets <= 0) {
            return;
        }
        String text = format(milliBuckets);
        Font font = context.font();
        GuiGraphicsExtractor graphics = context.guiGraphics();
        graphics.text(font, text, bounds.x() + TEXT_RIGHT - font.width(text), bounds.y() + TEXT_TOP, 0xFFFFFFFF, true);
    }
}
