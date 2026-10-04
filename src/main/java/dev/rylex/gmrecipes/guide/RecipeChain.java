package dev.rylex.gmrecipes.guide;

import guideme.document.LytSize;
import guideme.document.block.AlignItems;
import guideme.document.block.LytBlock;
import guideme.document.block.LytGuiSprite;
import guideme.document.block.LytHBox;
import guideme.render.GuiAssets;

public final class RecipeChain {

    private static final int GAP = 4;

    private RecipeChain() {}

    public static LytBlock of(LytBlock... steps) {
        LytHBox chain = new LytHBox();
        chain.setGap(GAP);
        chain.setAlignItems(AlignItems.CENTER);
        for (int i = 0; i < steps.length; i++) {
            if (i > 0) {
                chain.append(
                        new LytGuiSprite(GuiAssets.ARROW, new LytSize(LytCanvas.ARROW_WIDTH, LytCanvas.ARROW_HEIGHT)));
            }
            chain.append(steps[i]);
        }
        return chain;
    }
}
