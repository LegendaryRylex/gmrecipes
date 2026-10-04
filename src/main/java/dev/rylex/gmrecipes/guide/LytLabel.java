package dev.rylex.gmrecipes.guide;

import guideme.document.LytRect;
import guideme.document.block.LytBlock;
import guideme.layout.LayoutContext;
import guideme.render.RenderContext;
import guideme.style.ResolvedTextStyle;

public final class LytLabel extends LytBlock {

    private final String text;
    private final ResolvedTextStyle style;

    public LytLabel(String text, ResolvedTextStyle style) {
        this.text = text;
        this.style = style;
    }

    @Override
    protected LytRect computeLayout(LayoutContext context, int x, int y, int availableWidth) {
        return new LytRect(x, y, textWidth(context), context.getLineHeight(style));
    }

    /**
     * {@link LayoutContext#getAdvance} is unscaled, unlike {@link LayoutContext#getLineHeight}.
     */
    private int textWidth(LayoutContext context) {
        double advances = text.codePoints()
                .mapToDouble(cp -> context.getAdvance(cp, style))
                .sum();
        return (int) Math.ceil(advances * style.fontScale());
    }

    @Override
    protected void onLayoutMoved(int deltaX, int deltaY) {}

    @Override
    public void render(RenderContext context) {
        context.renderText(text, style, getBounds().x(), getBounds().y());
    }
}
