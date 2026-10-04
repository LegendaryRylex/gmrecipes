package dev.rylex.gmrecipes.guide;

import guideme.document.interaction.GuideTooltip;
import guideme.siteexport.ResourceExporter;
import java.util.List;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;

public final class SpriteTooltip implements GuideTooltip {

    private final SpriteContent content;

    public SpriteTooltip(SpriteContent content) {
        this.content = content;
    }

    @Override
    public List<ClientTooltipComponent> getLines() {
        return content.tooltip().stream()
                .map(Component::getVisualOrderText)
                .map(ClientTooltipComponent::create)
                .toList();
    }

    @Override
    public void exportResources(ResourceExporter exporter) {}
}
