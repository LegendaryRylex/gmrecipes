package dev.rylex.gmrecipes.guide;

import guideme.document.LytRect;
import guideme.document.block.LytBlock;
import guideme.document.block.LytSlot;
import guideme.document.interaction.GuideTooltip;
import guideme.document.interaction.InteractiveElement;
import guideme.layout.LayoutContext;
import guideme.render.GuiAssets;
import guideme.render.RenderContext;
import guideme.siteexport.ExportableResourceProvider;
import guideme.siteexport.ResourceExporter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import net.neoforged.neoforge.fluids.FluidStack;

public final class LytFluidSlot extends LytBlock implements InteractiveElement, ExportableResourceProvider {

    private static final int FLUID_SIZE = 16;
    private static final int PADDING = 1;
    private static final int CYCLE_TIME = 2000;

    private final List<FluidStack> variants;

    public LytFluidSlot(List<FluidStack> variants) {
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
        FluidStack displayed = displayed();
        if (displayed.isEmpty()) {
            return;
        }
        context.renderFluid(displayed, getBounds().x() + PADDING, getBounds().y() + PADDING, FLUID_SIZE, FLUID_SIZE);
        SlotAmount.draw(context, getBounds(), displayed.getAmount());
    }

    @Override
    public Optional<GuideTooltip> getTooltip(float x, float y) {
        FluidStack displayed = displayed();
        return displayed.isEmpty() ? Optional.empty() : Optional.of(new FluidTooltip(displayed));
    }

    @Override
    public void exportResources(ResourceExporter exporter) {
        for (FluidStack variant : variants) {
            exporter.referenceFluid(variant.getFluid());
        }
    }

    private FluidStack displayed() {
        if (variants.isEmpty()) {
            return FluidStack.EMPTY;
        }
        long cycle = System.nanoTime() / TimeUnit.MILLISECONDS.toNanos(CYCLE_TIME);
        return variants.get((int) (cycle % variants.size()));
    }
}
