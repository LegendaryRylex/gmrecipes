package dev.rylex.gmrecipes.compat.morered;

import commoble.morered.MoreRed;
import commoble.morered.soldering.SolderingRecipe;
import dev.rylex.gmrecipes.guide.LytIngredientGrid;
import dev.rylex.gmrecipes.guide.MappingRegistrar;
import dev.rylex.gmrecipes.guide.SlotContent;
import dev.rylex.gmrecipes.guide.Translations;
import guideme.document.block.LytBlock;
import guideme.document.block.recipes.LytStandardRecipeBox;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import org.jetbrains.annotations.Nullable;

public final class MoreRedGuideRecipes {

    private static final int INPUT_COLUMNS = 3;

    private MoreRedGuideRecipes() {}

    public static void collect(MappingRegistrar registrar) {
        registrar.add(MoreRed.get().solderingRecipeType.get(), MoreRedGuideRecipes::soldering);
    }

    private static LytBlock soldering(RecipeHolder<SolderingRecipe> holder) {
        SolderingRecipe recipe = holder.value();
        List<SlotContent> inputs = new ArrayList<>();
        for (SizedIngredient ingredient : recipe.ingredients()) {
            inputs.add(SlotContent.of(ingredient));
        }
        LytStandardRecipeBox.Builder builder =
                box("soldering", MoreRed.get().solderingTableBlock.get());
        if (inputs.stream().anyMatch(content -> !content.isEmpty())) {
            builder.input(LytIngredientGrid.wrapped(inputs, INPUT_COLUMNS));
        }
        ItemStack result = resultOf(recipe);
        if (!result.isEmpty()) {
            builder.output(LytIngredientGrid.row(List.of(SlotContent.of(result))));
        }
        return builder.build(holder);
    }

    private static ItemStack resultOf(SolderingRecipe recipe) {
        ItemStack stack = recipe.result();
        if (stack != null && !stack.isEmpty()) {
            return stack;
        }
        RegistryAccess access = registryAccess();
        return access == null ? ItemStack.EMPTY : recipe.getResultItem(access);
    }

    @Nullable
    private static RegistryAccess registryAccess() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.level == null ? null : minecraft.level.registryAccess();
    }

    private static LytStandardRecipeBox.Builder box(String name, ItemLike icon) {
        String translated = Translations.resolve("gmrecipes.recipe.morered." + name);
        return LytStandardRecipeBox.builder()
                .icon(icon)
                .title(
                        translated != null
                                ? translated
                                : new ItemStack(icon).getHoverName().getString());
    }
}
