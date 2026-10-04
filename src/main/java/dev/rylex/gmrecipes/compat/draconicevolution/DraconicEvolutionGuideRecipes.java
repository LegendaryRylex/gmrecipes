package dev.rylex.gmrecipes.compat.draconicevolution;

import com.brandon3055.draconicevolution.api.DraconicAPI;
import com.brandon3055.draconicevolution.api.crafting.IFusionRecipe;
import dev.rylex.gmrecipes.guide.LytCanvas;
import dev.rylex.gmrecipes.guide.LytLabel;
import dev.rylex.gmrecipes.guide.MappingRegistrar;
import dev.rylex.gmrecipes.guide.RecipeBoxStyle;
import dev.rylex.gmrecipes.guide.SlotContent;
import dev.rylex.gmrecipes.guide.Translations;
import guideme.document.block.recipes.LytStandardRecipeBox;
import guideme.internal.util.Platform;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

public final class DraconicEvolutionGuideRecipes {

    private static final NumberFormat INTEGER = NumberFormat.getIntegerInstance(Locale.ENGLISH);

    private static final int PITCH = 18;
    private static final int CENTRE_X = 42;
    private static final int RIGHT_X = 84;
    private static final ResourceLocation CORE_ID =
            ResourceLocation.fromNamespaceAndPath("draconicevolution", "crafting_core");
    private static final String TIER_KEY = "gui.draconicevolution.fusion_craft.tier.";

    private DraconicEvolutionGuideRecipes() {}

    public static void collect(MappingRegistrar registrar) {
        registrar.add(DraconicAPI.FUSION_RECIPE_TYPE.get(), DraconicEvolutionGuideRecipes::fusion);
    }

    private static LytStandardRecipeBox fusion(RecipeHolder<IFusionRecipe> holder) {
        IFusionRecipe recipe = holder.value();
        List<Ingredient> ingredients = recipe.getIngredients();
        int rows = Math.max(2, (ingredients.size() + 1) / 2);

        LytCanvas canvas = new LytCanvas();
        for (int i = 0; i < ingredients.size(); i++) {
            int x = i % 2 == 0 ? 0 : RIGHT_X;
            canvas.slot(x, i / 2 * PITCH, SlotContent.of(ingredients.get(i)));
        }
        canvas.slot(CENTRE_X, 0, SlotContent.of(recipe.getCatalyst()));
        canvas.slot(CENTRE_X, (rows - 1) * PITCH, SlotContent.of(result(recipe)));

        LytStandardRecipeBox.Builder builder =
                box("fusion_crafting", BuiltInRegistries.ITEM.get(CORE_ID)).customBody(canvas);
        builder.addBottom(tier(recipe));
        builder.addBottom(label(
                Component.translatable("gmrecipes.draconicevolution.energy", INTEGER.format(recipe.getEnergyCost()))));
        return builder.build(holder);
    }

    private static LytLabel tier(IFusionRecipe recipe) {
        String name = recipe.getRecipeTier().name().toLowerCase(Locale.ROOT);
        String key = TIER_KEY + name;
        if (Translations.resolve(key) != null) {
            return label(Component.translatable(key));
        }
        return label(Component.translatable("gmrecipes.draconicevolution.tier", name));
    }

    private static ItemStack result(IFusionRecipe recipe) {
        try {
            ItemStack stack = recipe.getResultItem(registryAccess());
            return stack == null ? ItemStack.EMPTY : stack;
        } catch (RuntimeException e) {
            return ItemStack.EMPTY;
        }
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
        String translated = Translations.resolve("gmrecipes.recipe.draconicevolution." + name);
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
