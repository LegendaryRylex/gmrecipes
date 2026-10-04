package dev.rylex.gmrecipes.compat.mob_grinding_utils;

import dev.rylex.gmrecipes.guide.LytIngredientGrid;
import dev.rylex.gmrecipes.guide.LytLabel;
import dev.rylex.gmrecipes.guide.MappingRegistrar;
import dev.rylex.gmrecipes.guide.RecipeBoxStyle;
import dev.rylex.gmrecipes.guide.SlotContent;
import dev.rylex.gmrecipes.guide.Translations;
import guideme.document.block.LytBlock;
import guideme.document.block.recipes.LytStandardRecipeBox;
import guideme.internal.util.Platform;
import java.util.ArrayList;
import java.util.List;
import mob_grinding_utils.MobGrindingUtils;
import mob_grinding_utils.ModBlocks;
import mob_grinding_utils.ModItems;
import mob_grinding_utils.recipe.BeheadingRecipe;
import mob_grinding_utils.recipe.SolidifyRecipe;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

public final class MobGrindingUtilsGuideRecipes {

    private MobGrindingUtilsGuideRecipes() {}

    public static void collect(MappingRegistrar registrar) {
        registrar.add(MobGrindingUtils.SOLIDIFIER_TYPE.get(), MobGrindingUtilsGuideRecipes::solidify);
        registrar.add(MobGrindingUtils.BEHEADING_TYPE.get(), MobGrindingUtilsGuideRecipes::beheading);
    }

    private static LytBlock solidify(RecipeHolder<SolidifyRecipe> holder) {
        SolidifyRecipe recipe = holder.value();
        List<SlotContent> inputs = new ArrayList<>();
        inputs.add(SlotContent.of(recipe.mould()));
        inputs.add(SlotContent.of(new FluidStack(ModBlocks.FLUID_XP.get(), recipe.fluidAmount())));
        return box("solidify", ModBlocks.XPSOLIDIFIER.getItem())
                .input(LytIngredientGrid.row(inputs))
                .output(LytIngredientGrid.row(List.of(SlotContent.of(recipe.result()))))
                .build(holder);
    }

    private static LytBlock beheading(RecipeHolder<BeheadingRecipe> holder) {
        BeheadingRecipe recipe = holder.value();
        EntityType<?> entity = entityOf(recipe);
        LytStandardRecipeBox.Builder builder = box("beheading", ModItems.SAW_UPGRADE_BEHEADING.get());
        SpawnEggItem egg = entity == null ? null : SpawnEggItem.byId(entity);
        if (egg != null) {
            builder.input(LytIngredientGrid.row(List.of(SlotContent.of(new ItemStack(egg)))));
        } else if (entity != null) {
            builder.addTop(label(entity.getDescription()));
        }
        return builder.output(LytIngredientGrid.row(List.of(SlotContent.of(resultOf(recipe)))))
                .build(holder);
    }

    @Nullable
    private static EntityType<?> entityOf(BeheadingRecipe recipe) {
        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            if (recipe.matches(type)) {
                return type;
            }
        }
        return null;
    }

    private static ItemStack resultOf(BeheadingRecipe recipe) {
        RegistryAccess access = registryAccess();
        return access == null ? ItemStack.EMPTY : recipe.getResultItem(access);
    }

    @Nullable
    private static RegistryAccess registryAccess() {
        try {
            return Platform.getClientRegistryAccess();
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static LytStandardRecipeBox.Builder box(String name, ItemLike icon) {
        String translated = Translations.resolve("gmrecipes.recipe.mob_grinding_utils." + name);
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
