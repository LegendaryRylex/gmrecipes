package dev.rylex.gmrecipes.compat.irons_spellbooks;

import dev.rylex.gmrecipes.guide.LytIngredientGrid;
import dev.rylex.gmrecipes.guide.MappingRegistrar;
import dev.rylex.gmrecipes.guide.SlotContent;
import dev.rylex.gmrecipes.guide.Translations;
import guideme.document.block.LytBlock;
import guideme.document.block.recipes.LytStandardRecipeBox;
import io.redspace.ironsspellbooks.recipe_types.alchemist_cauldron.BrewAlchemistCauldronRecipe;
import io.redspace.ironsspellbooks.recipe_types.alchemist_cauldron.EmptyAlchemistCauldronRecipe;
import io.redspace.ironsspellbooks.recipe_types.alchemist_cauldron.FillAlchemistCauldronRecipe;
import io.redspace.ironsspellbooks.registries.BlockRegistry;
import io.redspace.ironsspellbooks.registries.RecipeRegistry;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.fluids.FluidStack;

public final class IronsSpellbooksGuideRecipes {

    private IronsSpellbooksGuideRecipes() {}

    public static void collect(MappingRegistrar registrar) {
        registrar.add(RecipeRegistry.ALCHEMIST_CAULDRON_BREW_TYPE.get(), IronsSpellbooksGuideRecipes::brew);
        registrar.add(RecipeRegistry.ALCHEMIST_CAULDRON_EMPTY_TYPE.get(), IronsSpellbooksGuideRecipes::emptying);
        registrar.add(RecipeRegistry.ALCHEMIST_CAULDRON_FILL_TYPE.get(), IronsSpellbooksGuideRecipes::filling);
    }

    private static LytBlock brew(RecipeHolder<BrewAlchemistCauldronRecipe> holder) {
        BrewAlchemistCauldronRecipe recipe = holder.value();
        List<SlotContent> outputs = new ArrayList<>();
        for (FluidStack result : recipe.results()) {
            outputs.add(SlotContent.of(result));
        }
        recipe.byproduct().ifPresent(stack -> outputs.add(SlotContent.of(stack)));
        return build(
                holder,
                "alchemist_cauldron_brew",
                List.of(SlotContent.of(recipe.reagent()), SlotContent.of(recipe.fluidIn())),
                outputs);
    }

    private static LytBlock emptying(RecipeHolder<EmptyAlchemistCauldronRecipe> holder) {
        EmptyAlchemistCauldronRecipe recipe = holder.value();
        return build(
                holder,
                "alchemist_cauldron_empty",
                List.of(SlotContent.of(recipe.input()), SlotContent.of(recipe.fluid())),
                List.of(SlotContent.of(recipe.result())));
    }

    private static LytBlock filling(RecipeHolder<FillAlchemistCauldronRecipe> holder) {
        FillAlchemistCauldronRecipe recipe = holder.value();
        ItemStack returned = recipe.returned();
        return build(
                holder,
                "alchemist_cauldron_fill",
                List.of(SlotContent.of(recipe.input())),
                List.of(SlotContent.of(recipe.result()), SlotContent.of(returned)));
    }

    private static LytBlock build(
            RecipeHolder<?> holder, String name, List<SlotContent> inputs, List<SlotContent> outputs) {
        LytStandardRecipeBox.Builder builder = box(name, BlockRegistry.ALCHEMIST_CAULDRON.get());
        if (inputs.stream().anyMatch(content -> !content.isEmpty())) {
            builder.input(LytIngredientGrid.row(inputs));
        }
        if (outputs.stream().anyMatch(content -> !content.isEmpty())) {
            builder.output(LytIngredientGrid.row(outputs));
        }
        return builder.build(holder);
    }

    private static LytStandardRecipeBox.Builder box(String name, ItemLike icon) {
        String translated = Translations.resolve("gmrecipes.recipe.irons_spellbooks." + name);
        return LytStandardRecipeBox.builder()
                .icon(icon)
                .title(
                        translated != null
                                ? translated
                                : new ItemStack(icon).getHoverName().getString());
    }
}
