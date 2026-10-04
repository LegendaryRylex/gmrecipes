package dev.rylex.gmrecipes.guide;

import guideme.document.LytRect;
import guideme.document.LytSize;
import guideme.document.block.LytBlock;
import guideme.document.block.LytBox;
import guideme.document.block.LytGuiSprite;
import guideme.layout.LayoutContext;
import guideme.render.GuiAssets;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import org.joml.Vector2i;

public final class LytCanvas extends LytBox {

    public static final int ARROW_WIDTH = 24;
    public static final int ARROW_HEIGHT = 17;

    private final Map<LytBlock, Vector2i> offsets = new IdentityHashMap<>();

    public LytCanvas place(int x, int y, LytBlock child) {
        offsets.put(child, new Vector2i(x, y));
        append(child);
        return this;
    }

    public LytCanvas slot(int x, int y, SlotContent content) {
        return content.isEmpty() ? this : place(x, y, LytIngredientGrid.row(List.of(content)));
    }

    public LytCanvas arrow(int x, int y) {
        return place(x, y, new LytGuiSprite(GuiAssets.ARROW, new LytSize(ARROW_WIDTH, ARROW_HEIGHT)));
    }

    @Override
    protected LytRect computeBoxLayout(LayoutContext context, int x, int y, int availableWidth) {
        int right = x;
        int bottom = y;
        for (LytBlock child : children) {
            Vector2i offset = offsets.getOrDefault(child, new Vector2i());
            LytRect bounds = child.layout(context, x + offset.x, y + offset.y, availableWidth);
            right = Math.max(right, bounds.right());
            bottom = Math.max(bottom, bounds.bottom());
        }
        return new LytRect(x, y, right - x, bottom - y);
    }
}
