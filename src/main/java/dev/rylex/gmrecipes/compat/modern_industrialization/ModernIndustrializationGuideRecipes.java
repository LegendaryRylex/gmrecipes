package dev.rylex.gmrecipes.compat.modern_industrialization;

import aztech.modern_industrialization.MIBlock;
import aztech.modern_industrialization.MIRegistries;
import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.MITooltips;
import aztech.modern_industrialization.blocks.forgehammer.ForgeHammerRecipe;
import aztech.modern_industrialization.compat.rei.machines.MachineCategoryParams;
import aztech.modern_industrialization.compat.rei.machines.ReiMachineRecipes;
import aztech.modern_industrialization.items.ForgeTool;
import aztech.modern_industrialization.machines.init.MachineTier;
import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import aztech.modern_industrialization.machines.recipe.condition.MachineProcessCondition;
import aztech.modern_industrialization.util.TextHelper;
import dev.rylex.gmrecipes.guide.LytIngredientGrid;
import dev.rylex.gmrecipes.guide.LytLabel;
import dev.rylex.gmrecipes.guide.MappingRegistrar;
import dev.rylex.gmrecipes.guide.RecipeBoxStyle;
import dev.rylex.gmrecipes.guide.SlotContent;
import dev.rylex.gmrecipes.guide.Translations;
import guideme.document.block.AlignItems;
import guideme.document.block.LytBlock;
import guideme.document.block.LytHBox;
import guideme.document.block.recipes.LytStandardRecipeBox;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import org.jetbrains.annotations.Nullable;

public final class ModernIndustrializationGuideRecipes {

    private static final int GAP = 4;
    private static final int GRID_COLUMNS = 3;
    private static final ResourceLocation FORGE_HAMMER =
            ResourceLocation.parse("modern_industrialization:forge_hammer");

    private ModernIndustrializationGuideRecipes() {}

    public static void collect(MappingRegistrar registrar) {
        collectMachineTypes(registrar, "modern_industrialization");
        registrar.add(MIRegistries.FORGE_HAMMER_RECIPE_TYPE.get(), ModernIndustrializationGuideRecipes::forgeHammer);
    }

    public static void collectMachineTypes(MappingRegistrar registrar, String namespace) {
        for (Map.Entry<ResourceKey<RecipeType<?>>, RecipeType<?>> entry : BuiltInRegistries.RECIPE_TYPE.entrySet()) {
            if (entry.getValue() instanceof MachineRecipeType type
                    && namespace.equals(entry.getKey().location().getNamespace())) {
                registrar.add(type, ModernIndustrializationGuideRecipes::machine);
            }
        }
    }

    private static LytBlock machine(RecipeHolder<MachineRecipe> holder) {
        MachineRecipe recipe = holder.value();
        MachineCategoryParams category = category(recipe);

        List<SlotContent> inputs = new ArrayList<>();
        for (MachineRecipe.ItemInput input : recipe.itemInputs) {
            inputs.add(
                    SlotContent.of(input.ingredient()).withCount(input.amount()).withChance(input.probability()));
        }
        for (MachineRecipe.FluidInput input : recipe.fluidInputs) {
            inputs.add(fluids(input.fluid(), input.amount()).withChance(input.probability()));
        }
        List<SlotContent> outputs = new ArrayList<>();
        for (MachineRecipe.ItemOutput output : recipe.itemOutputs) {
            outputs.add(SlotContent.of(output.getStack()).withChance(output.probability()));
        }
        for (MachineRecipe.FluidOutput output : recipe.fluidOutputs) {
            outputs.add(SlotContent.of(new FluidStack(output.fluid(), clamp(output.amount())))
                    .withChance(output.probability()));
        }

        LytStandardRecipeBox.Builder builder = box(holder.value().getType(), icon(category));
        if (inputs.stream().anyMatch(content -> !content.isEmpty())) {
            builder.input(LytIngredientGrid.wrapped(inputs, GRID_COLUMNS));
        }
        if (outputs.stream().anyMatch(content -> !content.isEmpty())) {
            builder.output(LytIngredientGrid.wrapped(outputs, GRID_COLUMNS));
        }

        builder.addBottom(label(Component.translatable(
                "gmrecipes.modern_industrialization.energy",
                TextHelper.getEuText((long) recipe.duration * recipe.eu),
                TextHelper.getEuTextTick(recipe.eu))));

        boolean multiblock = category != null && category.isMultiblock;
        boolean steam = category != null && category.steamMode.steam;
        if (steam && multiblock && recipe.eu > MachineTier.BRONZE.getMaxEu()) {
            builder.addBottom(label(MIText.RequiresSteelHatch0.text()));
            builder.addBottom(label(MIText.RequiresSteelHatch1.text()));
        }
        int upgradeEu = recipe.eu - (multiblock ? MachineTier.MULTIBLOCK : MachineTier.LV).getMaxEu();
        boolean fusion = category != null && category.category.getPath().equals("fusion_reactor");
        if (upgradeEu > 0 && !fusion) {
            builder.addBottom(label(new MITooltips.Line(MIText.RequiresUpgrades)
                    .arg(upgradeEu, MITooltips.EU_PER_TICK_PARSER)
                    .build()));
        }
        List<Component> conditions = new ArrayList<>();
        for (MachineProcessCondition condition : recipe.conditions) {
            try {
                condition.appendDescription(conditions);
            } catch (RuntimeException e) {
                continue;
            }
        }
        conditions.forEach(line -> builder.addBottom(label(line)));
        return builder.build(holder);
    }

    private static LytBlock forgeHammer(RecipeHolder<ForgeHammerRecipe> holder) {
        ForgeHammerRecipe recipe = holder.value();
        LytStandardRecipeBox.Builder builder = box(FORGE_HAMMER, MIBlock.FORGE_HAMMER.asItem())
                .input(LytIngredientGrid.row(
                        List.of(SlotContent.of(recipe.ingredient()).withCount(recipe.count()))))
                .output(LytIngredientGrid.row(List.of(SlotContent.of(recipe.result()))));
        if (recipe.hammerDamage() > 0) {
            LytHBox tool = new LytHBox();
            tool.setGap(GAP);
            tool.setAlignItems(AlignItems.CENTER);
            tool.append(LytIngredientGrid.row(List.of(SlotContent.of(Ingredient.of(ForgeTool.TAG)))));
            tool.append(label(MIText.DurabilityCost.text(recipe.hammerDamage())));
            builder.addBottom(tool);
        } else {
            builder.addBottom(label(MIText.NoToolRequired.text()));
        }
        return builder.build(holder);
    }

    @Nullable
    private static MachineCategoryParams category(MachineRecipe recipe) {
        if (!(recipe.getType() instanceof MachineRecipeType type)) {
            return null;
        }
        for (MachineCategoryParams params : ReiMachineRecipes.categories.values()) {
            if (params.recipeType == type && accepts(params, recipe)) {
                return params;
            }
        }
        return null;
    }

    private static boolean accepts(MachineCategoryParams params, MachineRecipe recipe) {
        try {
            return params.recipePredicate.test(recipe);
        } catch (RuntimeException e) {
            return false;
        }
    }

    private static ItemLike icon(@Nullable MachineCategoryParams category) {
        if (category != null && !category.workstations.isEmpty()) {
            Item item = BuiltInRegistries.ITEM.get(category.workstations.getFirst());
            if (item != Items.AIR) {
                return item;
            }
        }
        return Items.BARRIER;
    }

    private static SlotContent fluids(FluidIngredient ingredient, long amount) {
        int sized = clamp(amount);
        return SlotContent.ofFluids(Arrays.stream(ingredient.getStacks())
                .map(stack -> stack.copyWithAmount(sized))
                .toList());
    }

    private static int clamp(long amount) {
        return (int) Math.min(amount, Integer.MAX_VALUE);
    }

    private static LytStandardRecipeBox.Builder box(RecipeType<?> type, ItemLike icon) {
        ResourceLocation id = BuiltInRegistries.RECIPE_TYPE.getKey(type);
        return box(id, icon);
    }

    private static LytStandardRecipeBox.Builder box(@Nullable ResourceLocation id, ItemLike icon) {
        String translated =
                id == null ? null : Translations.resolve("gmrecipes.recipe." + id.getNamespace() + "." + id.getPath());
        return LytStandardRecipeBox.builder()
                .icon(icon)
                .title(
                        translated != null
                                ? translated
                                : new ItemStack(icon).getHoverName().getString());
    }

    private static LytLabel label(Component text) {
        return new LytLabel(text.getString(), RecipeBoxStyle.TEXT);
    }
}
