package dev.rylex.gmrecipes.guide;

import guideme.document.LytRect;
import guideme.document.block.LytBlock;
import guideme.document.block.LytSlot;
import guideme.document.interaction.GuideTooltip;
import guideme.document.interaction.InteractiveElement;
import guideme.document.interaction.TextTooltip;
import guideme.layout.LayoutContext;
import guideme.render.GuiAssets;
import guideme.render.RenderContext;
import java.util.Optional;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class LytTextureSlot extends LytBlock implements InteractiveElement {

    private static final int TEXTURE_SIZE = 16;
    private static final int PADDING = 1;

    private final ResourceLocation texture;
    private final Component tooltip;

    public LytTextureSlot(ResourceLocation texture, Component tooltip) {
        this.texture = texture;
        this.tooltip = tooltip;
    }

    @Override
    protected LytRect computeLayout(LayoutContext context, int x, int y, int availableWidth) {
        return new LytRect(x, y, LytSlot.OUTER_SIZE, LytSlot.OUTER_SIZE);
    }

    @Override
    protected void onLayoutMoved(int deltaX, int deltaY) {}

    @Override
    public void renderBatch(RenderContext context, MultiBufferSource buffers) {}

    @Override
    public void render(RenderContext context) {
        LytRect bounds = getBounds();
        context.fillIcon(bounds, GuiAssets.SLOT);
        context.fillTexturedRect(
                new LytRect(bounds.x() + PADDING, bounds.y() + PADDING, TEXTURE_SIZE, TEXTURE_SIZE), texture);
    }

    @Override
    public Optional<GuideTooltip> getTooltip(float x, float y) {
        return Optional.of(new TextTooltip(tooltip));
    }
}
