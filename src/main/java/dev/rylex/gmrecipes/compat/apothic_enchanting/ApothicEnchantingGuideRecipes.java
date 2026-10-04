package dev.rylex.gmrecipes.compat.apothic_enchanting;

import dev.rylex.gmrecipes.guide.LytIngredientGrid;
import dev.rylex.gmrecipes.guide.LytLabel;
import dev.rylex.gmrecipes.guide.MappingRegistrar;
import dev.rylex.gmrecipes.guide.RecipeBoxStyle;
import dev.rylex.gmrecipes.guide.SlotContent;
import dev.rylex.gmrecipes.guide.Translations;
import dev.shadowsoffire.apothic_enchanting.Ench;
import dev.shadowsoffire.apothic_enchanting.table.EnchantingStatRegistry.Stats;
import dev.shadowsoffire.apothic_enchanting.table.infusion.InfusionRecipe;
import guideme.document.block.LytBlock;
import guideme.document.block.recipes.LytStandardRecipeBox;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

public final class ApothicEnchantingGuideRecipes {

    private static final DecimalFormat DECIMAL =
            new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.ROOT));

    private ApothicEnchantingGuideRecipes() {}

    public static void collect(MappingRegistrar registrar) {
        registrar.add(Ench.RecipeTypes.INFUSION, ApothicEnchantingGuideRecipes::infusion);
    }

    private static LytBlock infusion(RecipeHolder<InfusionRecipe> holder) {
        InfusionRecipe recipe = holder.value();
        LytStandardRecipeBox.Builder builder = box("infusion", Blocks.ENCHANTING_TABLE)
                .input(LytIngredientGrid.row(List.of(SlotContent.of(recipe.getInput()))))
                .output(LytIngredientGrid.row(List.of(SlotContent.of(recipe.getOutput()))));
        Stats min = recipe.getRequirements();
        Stats max = recipe.getMaxRequirements();
        stat(builder, "eterna", false, min.eterna(), max.eterna());
        stat(builder, "quanta", true, min.quanta(), max.quanta());
        stat(builder, "arcana", true, min.arcana(), max.arcana());
        return builder.build(holder);
    }

    private static void stat(LytStandardRecipeBox.Builder builder, String stat, boolean percent, float min, float max) {
        if (min <= 0 && max <= 0) {
            return;
        }
        Component name = Component.translatable("gui.apothic_enchanting.enchant." + stat);
        String low = value(min, percent);
        Component line;
        if (max < 0) {
            line = Component.translatable("gmrecipes.apothic_enchanting.stat.min", name, low);
        } else if (max == min) {
            line = Component.translatable("gmrecipes.apothic_enchanting.stat.exact", name, low);
        } else {
            line = Component.translatable("gmrecipes.apothic_enchanting.stat.range", name, low, value(max, percent));
        }
        builder.addBottom(label(line));
    }

    private static String value(float amount, boolean percent) {
        String text = DECIMAL.format(amount);
        return percent
                ? Component.translatable("gmrecipes.apothic_enchanting.percent", text)
                        .getString()
                : text;
    }

    private static LytStandardRecipeBox.Builder box(String name, ItemLike icon) {
        String translated = Translations.resolve("gmrecipes.recipe.apothic_enchanting." + name);
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
