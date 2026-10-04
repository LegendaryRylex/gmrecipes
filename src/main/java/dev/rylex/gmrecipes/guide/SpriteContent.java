package dev.rylex.gmrecipes.guide;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * @param sprite an atlas location in the block atlas, where a mod stitching its own textures puts them
 * @param tint packed ARGB multiplied over the sprite
 * @param amount in millibuckets, or 0 when the content has no amount to show
 */
public record SpriteContent(ResourceLocation sprite, int tint, long amount, List<Component> tooltip) {

    public SpriteContent {
        tooltip = List.copyOf(tooltip);
    }

    public String key() {
        return sprite + "@" + Integer.toHexString(tint) + "@"
                + (tooltip.isEmpty() ? "" : tooltip.getFirst().getString()) + "x" + amount;
    }
}
