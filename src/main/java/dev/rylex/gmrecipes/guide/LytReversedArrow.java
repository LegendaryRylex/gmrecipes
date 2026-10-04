package dev.rylex.gmrecipes.guide;

import guideme.color.ColorValue;
import guideme.document.LytSize;
import guideme.document.block.LytGuiSprite;
import guideme.render.GuiAssets;
import guideme.render.RenderContext;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public final class LytReversedArrow extends LytGuiSprite {

    public LytReversedArrow() {
        super(GuiAssets.ARROW, new LytSize(LytCanvas.ARROW_WIDTH, LytCanvas.ARROW_HEIGHT));
    }

    @Override
    public void render(RenderContext context) {
        TextureAtlasSprite sprite = getSprite().atlasSprite(context.lightDarkMode());
        ColorValue color = getColor();
        context.fillTexturedRect(
                getBounds(),
                sprite.atlasLocation(),
                color,
                color,
                color,
                color,
                sprite.getU1(),
                sprite.getV0(),
                sprite.getU0(),
                sprite.getV1());
    }
}
