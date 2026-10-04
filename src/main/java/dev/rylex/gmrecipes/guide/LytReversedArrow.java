package dev.rylex.gmrecipes.guide;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import guideme.document.LytRect;
import guideme.document.LytSize;
import guideme.document.block.LytGuiSprite;
import guideme.render.GuiAssets;
import guideme.render.RenderContext;

public final class LytReversedArrow extends LytGuiSprite {

    public LytReversedArrow() {
        super(GuiAssets.ARROW, new LytSize(LytCanvas.ARROW_WIDTH, LytCanvas.ARROW_HEIGHT));
    }

    /** Mirroring flips the quad's winding, so culling has to be off or the arrow is not drawn. */
    @Override
    public void render(RenderContext context) {
        LytRect bounds = getBounds();
        PoseStack pose = context.poseStack();
        pose.pushPose();
        pose.translate(2 * bounds.x() + bounds.width(), 0, 0);
        pose.scale(-1, 1, 1);
        RenderSystem.disableCull();
        super.render(context);
        RenderSystem.enableCull();
        pose.popPose();
    }
}
