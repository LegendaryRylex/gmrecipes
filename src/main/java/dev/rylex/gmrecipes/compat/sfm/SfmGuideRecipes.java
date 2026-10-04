package dev.rylex.gmrecipes.compat.sfm;

import ca.teamdman.sfm.common.item.FormItem;
import ca.teamdman.sfm.common.recipe.PrintingPressRecipe;
import ca.teamdman.sfm.common.registry.registration.SFMBlocks;
import ca.teamdman.sfm.common.registry.registration.SFMRecipeTypes;
import dev.rylex.gmrecipes.guide.LytIngredientGrid;
import dev.rylex.gmrecipes.guide.MappingRegistrar;
import dev.rylex.gmrecipes.guide.SlotContent;
import dev.rylex.gmrecipes.guide.Translations;
import guideme.document.block.LytBlock;
import guideme.document.block.recipes.LytStandardRecipeBox;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ItemLike;

public final class SfmGuideRecipes {

    private SfmGuideRecipes() {}

    public static void collect(MappingRegistrar registrar) {
        registrar.add(SFMRecipeTypes.PRINTING_PRESS.get(), SfmGuideRecipes::printingPress);
    }

    private static LytBlock printingPress(RecipeHolder<PrintingPressRecipe> holder) {
        PrintingPressRecipe recipe = holder.value();
        List<SlotContent> inputs = new ArrayList<>();
        inputs.add(SlotContent.ofItems(forms(recipe)));
        inputs.add(SlotContent.of(recipe.ink()));
        inputs.add(SlotContent.of(recipe.paper()));
        return box("printing_press", SFMBlocks.PRINTING_PRESS.get())
                .input(LytIngredientGrid.column(inputs))
                .output(LytIngredientGrid.row(List.of(SlotContent.of(recipe.form()))))
                .build(holder);
    }

    private static List<ItemStack> forms(PrintingPressRecipe recipe) {
        List<ItemStack> forms = new ArrayList<>();
        for (ItemStack stack : recipe.form().getItems()) {
            forms.add(FormItem.createFormFromReference(stack));
        }
        return forms;
    }

    private static LytStandardRecipeBox.Builder box(String name, ItemLike icon) {
        String translated = Translations.resolve("gmrecipes.recipe.sfm." + name);
        return LytStandardRecipeBox.builder()
                .icon(icon)
                .title(
                        translated != null
                                ? translated
                                : new ItemStack(icon).getHoverName().getString());
    }
}
