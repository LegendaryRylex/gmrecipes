package dev.rylex.gmrecipes.compat.justdirethings;

import com.direwolf20.justdirethings.datagen.recipes.BlockOrTagInput;
import com.direwolf20.justdirethings.datagen.recipes.FluidDropRecipe;
import com.direwolf20.justdirethings.datagen.recipes.GooSpreadRecipe;
import com.direwolf20.justdirethings.setup.JDTRegistration;
import dev.rylex.gmrecipes.guide.LytIngredientGrid;
import dev.rylex.gmrecipes.guide.LytLabel;
import dev.rylex.gmrecipes.guide.MappingRegistrar;
import dev.rylex.gmrecipes.guide.RecipeBoxStyle;
import dev.rylex.gmrecipes.guide.SlotContent;
import dev.rylex.gmrecipes.guide.Translations;
import guideme.document.block.recipes.LytStandardRecipeBox;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

public final class JustDireThingsGuideRecipes {

    private static final int BUCKET = 1000;
    private static final DecimalFormat DECIMAL =
            new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.ROOT));

    private JustDireThingsGuideRecipes() {}

    public static void collect(MappingRegistrar registrar) {
        registrar.add(JDTRegistration.GOO_SPREAD_RECIPE_TYPE.get(), JustDireThingsGuideRecipes::gooSpread);
        registrar.add(JDTRegistration.FLUID_DROP_RECIPE_TYPE.get(), JustDireThingsGuideRecipes::fluidDrop);
    }

    private static LytStandardRecipeBox gooSpread(RecipeHolder<GooSpreadRecipe> holder) {
        GooSpreadRecipe recipe = holder.value();
        BlockOrTagInput input = recipe.getInput();
        return goo(
                input.asTag() != null ? "goospread_tag" : "goospread",
                inputContent(input),
                recipe.getOutput(),
                recipe.getTierRequirement(),
                recipe.getCraftingDuration(),
                holder);
    }

    private static SlotContent inputContent(BlockOrTagInput input) {
        TagKey<Block> tag = input.asTag();
        if (tag == null) {
            return SlotContent.of(stateStack(input.asState()));
        }
        List<ItemStack> inputs = new ArrayList<>();
        for (Holder<Block> block : BuiltInRegistries.BLOCK.getTagOrEmpty(tag)) {
            ItemStack stack = stateStack(block.value().defaultBlockState());
            if (!stack.isEmpty()) {
                inputs.add(stack);
            }
        }
        return SlotContent.ofItems(inputs);
    }

    private static LytStandardRecipeBox goo(
            String name, SlotContent input, BlockState output, int tier, int duration, RecipeHolder<?> holder) {
        List<SlotContent> inputs = new ArrayList<>();
        inputs.add(input);
        inputs.add(SlotContent.ofItems(gooTiers(tier)));
        LytStandardRecipeBox.Builder builder = box(name, JDTRegistration.GooBlock_Tier1_ITEM.get())
                .input(LytIngredientGrid.row(inputs))
                .output(LytIngredientGrid.row(List.of(stateContent(output))));
        if (duration > 0) {
            builder.addBottom(label(Component.translatable(
                    "gmrecipes.justdirethings.duration",
                    Component.translatable("gmrecipes.stat.ticks", duration, DECIMAL.format(duration / 20.0)))));
        }
        return builder.build(holder);
    }

    private static LytStandardRecipeBox fluidDrop(RecipeHolder<FluidDropRecipe> holder) {
        FluidDropRecipe recipe = holder.value();
        List<SlotContent> inputs = new ArrayList<>();
        inputs.add(stateContent(recipe.getInput()));
        inputs.add(SlotContent.of(new ItemStack(recipe.getCatalyst())));
        return box("fluiddrop", JDTRegistration.PolymorphicCatalyst.get())
                .input(LytIngredientGrid.row(inputs))
                .output(LytIngredientGrid.row(List.of(stateContent(recipe.getOutput()))))
                .build(holder);
    }

    private static List<ItemStack> gooTiers(int required) {
        List<ItemStack> tiers = new ArrayList<>();
        List<ItemLike> goos = List.of(
                JDTRegistration.GooBlock_Tier1.get(),
                JDTRegistration.GooBlock_Tier2.get(),
                JDTRegistration.GooBlock_Tier3.get(),
                JDTRegistration.GooBlock_Tier4.get());
        for (int tier = Math.max(required, 1); tier <= goos.size(); tier++) {
            tiers.add(new ItemStack(goos.get(tier - 1)));
        }
        return tiers;
    }

    private static ItemStack stateStack(BlockState state) {
        return state.getBlock().asItem() == Items.AIR ? ItemStack.EMPTY : new ItemStack(state.getBlock());
    }

    private static SlotContent stateContent(BlockState state) {
        ItemStack stack = stateStack(state);
        if (!stack.isEmpty()) {
            return SlotContent.of(stack);
        }
        Fluid fluid = state.getFluidState().getType();
        return fluid == Fluids.EMPTY
                ? SlotContent.empty()
                : SlotContent.of(new FluidStack(fluid.builtInRegistryHolder(), BUCKET));
    }

    private static LytStandardRecipeBox.Builder box(String name, ItemLike icon) {
        String translated = Translations.resolve("gmrecipes.recipe.justdirethings." + name);
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
