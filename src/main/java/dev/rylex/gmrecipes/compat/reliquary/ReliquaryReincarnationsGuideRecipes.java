package dev.rylex.gmrecipes.compat.reliquary;

import dev.rylex.gmrecipes.guide.LytIngredientGrid;
import dev.rylex.gmrecipes.guide.LytLabel;
import dev.rylex.gmrecipes.guide.MappingRegistrar;
import dev.rylex.gmrecipes.guide.RecipeBoxStyle;
import dev.rylex.gmrecipes.guide.SlotContent;
import dev.rylex.gmrecipes.guide.Translations;
import guideme.document.block.LytBlock;
import guideme.document.block.recipes.LytStandardRecipeBox;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ItemLike;
import reliquary.crafting.InfernalTearValueRecipe;
import reliquary.init.ModItems;
import reliquary.util.XpHelper;

public final class ReliquaryReincarnationsGuideRecipes {

    private static final NumberFormat INTEGER = NumberFormat.getIntegerInstance(Locale.ENGLISH);

    private ReliquaryReincarnationsGuideRecipes() {}

    public static void collect(MappingRegistrar registrar) {
        registrar.add(ModItems.INFERNAL_TEAR_VALUE_TYPE.get(), ReliquaryReincarnationsGuideRecipes::infernalTear);
    }

    private static LytBlock infernalTear(RecipeHolder<InfernalTearValueRecipe> holder) {
        InfernalTearValueRecipe recipe = holder.value();
        int points = recipe.getExperiencePoints();
        LytStandardRecipeBox.Builder builder = box("infernal_tear_value", ModItems.INFERNAL_TEAR.get())
                .customBody(LytIngredientGrid.row(List.of(SlotContent.of(recipe.getIngredient()))));
        if (points > 0) {
            builder.addBottom(label(Component.translatable("gmrecipes.reliquary.experience", INTEGER.format(points))));
            builder.addBottom(label(Component.translatable(
                    "gmrecipes.reliquary.level", INTEGER.format(XpHelper.getLevelForExperience(points)))));
        }
        return builder.build(holder);
    }

    private static LytStandardRecipeBox.Builder box(String name, ItemLike icon) {
        String translated = Translations.resolve("gmrecipes.recipe.reliquary." + name);
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
