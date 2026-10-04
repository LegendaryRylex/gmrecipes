package dev.rylex.gmrecipes.compat.xycraft_machines;

import dev.rylex.gmrecipes.guide.SlotContent;
import dev.rylex.gmrecipes.guide.SpriteContent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import tv.soaryn.contextmatters.rules.BlockRule;
import tv.soaryn.contextmatters.rules.BlockStateRule;
import tv.soaryn.contextmatters.rules.BlockTagRule;
import tv.soaryn.contextmatters.rules.FluidTagRule;
import tv.soaryn.contextmatters.rules.FluidTypeRule;
import tv.soaryn.contextmatters.rules.FuzzyBlockStateRule;
import tv.soaryn.contextmatters.rules.IRule;
import tv.soaryn.contextmatters.rules.LavaRule;
import tv.soaryn.contextmatters.rules.PropertyPresenceRule;
import tv.soaryn.contextmatters.rules.PropertyRule;
import tv.soaryn.contextmatters.rules.WaterRule;

final class XyRules {

    private static final int PROBE_AMOUNT = 1000;
    private static final int MAX_PROPERTY_BLOCKS = 64;

    private XyRules() {}

    static SlotContent content(IRule rule) {
        try {
            return switch (rule) {
                case BlockRule block -> block(block.block());
                case BlockStateRule state -> block(state.state().getBlock());
                case FuzzyBlockStateRule fuzzy -> block(fuzzy.block());
                case BlockTagRule tag -> blocks(tag);
                case FluidTypeRule type -> fluids(type.fluidType());
                case FluidTagRule tag -> fluidsOf(tag);
                case PropertyRule property -> properties(property.property(), property.value());
                case PropertyPresenceRule property -> properties(property.property(), null);
                case WaterRule water -> content(FluidTypeRule.water());
                case LavaRule lava -> content(FluidTypeRule.lava());
                default -> SlotContent.empty();
            };
        } catch (RuntimeException e) {
            return SlotContent.empty();
        }
    }

    private static SlotContent block(Block block) {
        return SlotContent.of(new ItemStack(block));
    }

    private static SlotContent blocks(BlockTagRule rule) {
        List<ItemStack> stacks = new ArrayList<>();
        for (var holder : BuiltInRegistries.BLOCK.getTagOrEmpty(rule.tag())) {
            stacks.add(new ItemStack(holder.value()));
        }
        return SlotContent.ofItems(stacks);
    }

    private static SlotContent properties(String name, String value) {
        List<ItemStack> stacks = new ArrayList<>();
        for (Block block : BuiltInRegistries.BLOCK) {
            if (stacks.size() >= MAX_PROPERTY_BLOCKS) {
                break;
            }
            Property<?> property = block.getStateDefinition().getProperty(name);
            if (property != null
                    && (value == null
                            || property.getPossibleValues().stream()
                                    .anyMatch(v -> valueName(property, v).equals(value)))) {
                stacks.add(new ItemStack(block));
            }
        }
        return SlotContent.ofItems(stacks);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static String valueName(Property property, Object value) {
        return property.getName((Comparable) value);
    }

    private static SlotContent fluidsOf(FluidTagRule rule) {
        List<Fluid> fluids = new ArrayList<>();
        for (var holder : BuiltInRegistries.FLUID.getTagOrEmpty(rule.tag())) {
            fluids.add(holder.value());
        }
        return sprites(fluids);
    }

    private static SlotContent fluids(FluidType type) {
        List<Fluid> fluids = new ArrayList<>();
        for (Fluid fluid : BuiltInRegistries.FLUID) {
            if (fluid.getFluidType() == type) {
                fluids.add(fluid);
            }
        }
        return sprites(fluids);
    }

    /**
     * Rules state no fluid amount, so the fluids are drawn as amount-less sprites rather than as
     * stacks, whose slot always prints a quantity.
     */
    private static SlotContent sprites(List<Fluid> fluids) {
        Set<FluidType> seen = new HashSet<>();
        List<SpriteContent> sprites = new ArrayList<>();
        for (Fluid fluid : fluids) {
            if (!fluid.isSource(fluid.defaultFluidState()) || !seen.add(fluid.getFluidType())) {
                continue;
            }
            FluidStack probe = new FluidStack(fluid, PROBE_AMOUNT);
            FluidModel model = Minecraft.getInstance()
                    .getModelManager()
                    .getFluidStateModelSet()
                    .get(fluid.defaultFluidState());
            int tint = model.fluidTintSource() == null
                    ? -1
                    : model.fluidTintSource().colorAsStack(probe);
            sprites.add(new SpriteContent(
                    model.stillMaterial().sprite().contents().name(),
                    tint,
                    0,
                    List.<Component>of(probe.getHoverName())));
        }
        return SlotContent.ofSprites(sprites);
    }
}
