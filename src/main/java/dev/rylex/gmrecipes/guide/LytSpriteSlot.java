package dev.rylex.gmrecipes.guide;

import guideme.color.ConstantColor;
import guideme.document.LytRect;
import guideme.document.block.LytBlock;
import guideme.document.block.LytSlot;
import guideme.document.interaction.GuideTooltip;
import guideme.document.interaction.InteractiveElement;
import guideme.layout.LayoutContext;
import guideme.render.GuiAssets;
import guideme.render.RenderContext;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.data.AtlasIds;
import org.jetbrains.annotations.Nullable;

public final class LytSpriteSlot extends LytBlock implements InteractiveElement {

    private static final int SPRITE_SIZE = 16;
    private static final int PADDING = 1;
    private static final int CYCLE_TIME = 2000;

    private final List<SpriteContent> variants;

    public LytSpriteSlot(List<SpriteContent> variants) {
        this.variants = List.copyOf(variants);
    }

    @Override
    protected LytRect computeLayout(LayoutContext context, int x, int y, int availableWidth) {
        return new LytRect(x, y, LytSlot.OUTER_SIZE, LytSlot.OUTER_SIZE);
    }

    @Override
    protected void onLayoutMoved(int deltaX, int deltaY) {}

    @Override
    public void render(RenderContext context) {
        context.fillIcon(getBounds(), GuiAssets.SLOT);
        SpriteContent displayed = displayed();
        if (displayed == null) {
            return;
        }
        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getAtlasManager()
                .getAtlasOrThrow(AtlasIds.BLOCKS)
                .getSprite(displayed.sprite());
        LytRect bounds = getBounds();
        context.fillTexturedRect(
                new LytRect(bounds.x() + PADDING, bounds.y() + PADDING, SPRITE_SIZE, SPRITE_SIZE),
                sprite,
                new ConstantColor(0xFF000000 | displayed.tint()));
        SlotAmount.draw(context, bounds, displayed.amount());
    }

    @Override
    public Optional<GuideTooltip> getTooltip(float x, float y) {
        SpriteContent displayed = displayed();
        return displayed == null || displayed.tooltip().isEmpty()
                ? Optional.empty()
                : Optional.of(new SpriteTooltip(displayed));
    }

    @Nullable
    private SpriteContent displayed() {
        if (variants.isEmpty()) {
            return null;
        }
        long cycle = System.nanoTime() / TimeUnit.MILLISECONDS.toNanos(CYCLE_TIME);
        return variants.get((int) (cycle % variants.size()));
    }
}
