package dev.rylex.gmrecipes.guide;

import guideme.document.interaction.GuideTooltip;
import guideme.siteexport.ResourceExporter;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

public final class FluidTooltip implements GuideTooltip {

    private static final String AMOUNT_KEY = "gmrecipes.fluid.amount";

    private final FluidStack stack;

    public FluidTooltip(FluidStack stack) {
        this.stack = stack;
    }

    /**
     * The bucket is only the icon GuideMe draws beside the tooltip; a fluid without one drops it.
     */
    @Override
    public ItemStack getIcon() {
        return new ItemStack(stack.getFluid().getBucket());
    }

    @Override
    public List<ClientTooltipComponent> getLines() {
        List<Component> lines = new ArrayList<>();
        lines.add(stack.getHoverName());
        lines.add(Component.translatable(AMOUNT_KEY, String.format("%,d", stack.getAmount()))
                .withStyle(ChatFormatting.GRAY));
        String owner = modName();
        if (owner != null) {
            lines.add(Component.literal(owner).withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC));
        }
        return lines.stream()
                .map(Component::getVisualOrderText)
                .map(ClientTooltipComponent::create)
                .toList();
    }

    @Override
    public void exportResources(ResourceExporter exporter) {
        exporter.referenceFluid(stack.getFluid());
    }

    @Nullable
    private String modName() {
        var id = BuiltInRegistries.FLUID.getKey(stack.getFluid());
        if (id == null) {
            return null;
        }
        return ModList.get()
                .getModContainerById(id.getNamespace())
                .map(container -> container.getModInfo().getDisplayName())
                .orElse(null);
    }
}
