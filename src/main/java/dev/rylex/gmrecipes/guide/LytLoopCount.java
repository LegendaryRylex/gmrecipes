package dev.rylex.gmrecipes.guide;

import dev.rylex.gmrecipes.GmRecipes;
import guideme.document.LytRect;
import guideme.document.block.LytBlock;
import guideme.document.interaction.GuideTooltip;
import guideme.document.interaction.InteractiveElement;
import guideme.document.interaction.TextTooltip;
import guideme.layout.LayoutContext;
import guideme.render.RenderContext;
import guideme.style.ResolvedTextStyle;
import java.util.Optional;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class LytLoopCount extends LytBlock implements InteractiveElement {

    private static final ResourceLocation TEXTURE = GmRecipes.id("textures/gui/loop.png");
    private static final ResolvedTextStyle STYLE = RecipeBoxStyle.TEXT;
    private static final String TOOLTIP_KEY = GmRecipes.MOD_ID + ".recipe.repeats";
    private static final int SIZE = 24;

    private final int count;
    private final String label;
    private int labelX;
    private int labelY;

    public LytLoopCount(int count) {
        this.count = count;
        this.label = Integer.toString(count);
    }

    @Override
    protected LytRect computeLayout(LayoutContext context, int x, int y, int availableWidth) {
        double advances = label.codePoints()
                .mapToDouble(cp -> context.getAdvance(cp, STYLE))
                .sum();
        labelX = (SIZE - (int) Math.ceil(advances * STYLE.fontScale())) / 2;
        labelY = (SIZE - context.getLineHeight(STYLE)) / 2;
        return new LytRect(x, y, SIZE, SIZE);
    }

    @Override
    protected void onLayoutMoved(int deltaX, int deltaY) {}

    @Override
    public void render(RenderContext context) {
        context.fillTexturedRect(getBounds(), TEXTURE, RecipeBoxStyle.ICON);
    }

    @Override
    public void renderBatch(RenderContext context, MultiBufferSource buffers) {
        context.renderTextInBatch(
                label, STYLE, getBounds().x() + labelX, getBounds().y() + labelY, buffers);
    }

    @Override
    public Optional<GuideTooltip> getTooltip(float x, float y) {
        return Optional.of(new TextTooltip(Component.translatable(TOOLTIP_KEY, count)));
    }
}
