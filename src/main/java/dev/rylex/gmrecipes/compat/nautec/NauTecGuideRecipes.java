package dev.rylex.gmrecipes.compat.nautec;

import com.portingdeadmods.nautec.NTRegistries;
import com.portingdeadmods.nautec.api.bacteria.Bacteria;
import com.portingdeadmods.nautec.content.recipes.AquaticCatalystChannelingRecipe;
import com.portingdeadmods.nautec.content.recipes.AugmentationRecipe;
import com.portingdeadmods.nautec.content.recipes.BacteriaIncubationRecipe;
import com.portingdeadmods.nautec.content.recipes.BacteriaMutationRecipe;
import com.portingdeadmods.nautec.content.recipes.ItemEtchingRecipe;
import com.portingdeadmods.nautec.content.recipes.ItemTransformationRecipe;
import com.portingdeadmods.nautec.content.recipes.MixingRecipe;
import com.portingdeadmods.nautec.content.recipes.utils.IngredientWithCount;
import com.portingdeadmods.nautec.registries.NTBlocks;
import com.portingdeadmods.nautec.registries.NTFluids;
import com.portingdeadmods.nautec.registries.NTItems;
import com.portingdeadmods.nautec.utils.BacteriaHelper;
import com.portingdeadmods.nautec.utils.Utils;
import dev.rylex.gmrecipes.guide.LytIngredientGrid;
import dev.rylex.gmrecipes.guide.LytLabel;
import dev.rylex.gmrecipes.guide.MappingRegistrar;
import dev.rylex.gmrecipes.guide.RecipeBoxStyle;
import dev.rylex.gmrecipes.guide.SlotContent;
import dev.rylex.gmrecipes.guide.Translations;
import guideme.document.block.LytBlock;
import guideme.document.block.recipes.LytStandardRecipeBox;
import guideme.internal.util.Platform;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

public final class NauTecGuideRecipes {

    private static final DecimalFormat DECIMAL =
            new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.ROOT));

    private static final String NAMESPACE = "nautec";

    private NauTecGuideRecipes() {}

    public static void collect(MappingRegistrar registrar) {
        registrar.add(id("bacteria_mutation"), BacteriaMutationRecipe.TYPE, NauTecGuideRecipes::mutation);
        registrar.add(id("bacteria_incubation"), BacteriaIncubationRecipe.TYPE, NauTecGuideRecipes::incubation);
        registrar.add(id("mixing"), MixingRecipe.Type.INSTANCE, NauTecGuideRecipes::mixing);
        registrar.add(
                id("item_transformation"), ItemTransformationRecipe.Type.INSTANCE, NauTecGuideRecipes::transformation);
        registrar.add(id("item_etching"), ItemEtchingRecipe.Type.INSTANCE, NauTecGuideRecipes::etching);
        registrar.add(id("augmentation"), AugmentationRecipe.Type.INSTANCE, NauTecGuideRecipes::augmentation);
        registrar.add(
                id("aquatic_catalyst_channeling"),
                AquaticCatalystChannelingRecipe.Type.INSTANCE,
                NauTecGuideRecipes::channeling);
    }

    private static LytBlock mutation(RecipeHolder<BacteriaMutationRecipe> holder) {
        BacteriaMutationRecipe recipe = holder.value();
        List<SlotContent> inputs = new ArrayList<>();
        inputs.add(SlotContent.of(dish(recipe.inputBacteria())));
        inputs.add(SlotContent.of(recipe.catalyst()));
        return box("bacteria_mutation", NTBlocks.MUTATOR.get())
                .input(LytIngredientGrid.row(inputs))
                .output(LytIngredientGrid.row(List.of(SlotContent.of(dish(recipe.resultBacteria())))))
                .addBottom(label(
                        Component.translatable("gmrecipes.nautec.mutation_chance", DECIMAL.format(recipe.chance()))))
                .build(holder);
    }

    private static LytBlock incubation(RecipeHolder<BacteriaIncubationRecipe> holder) {
        BacteriaIncubationRecipe recipe = holder.value();
        List<SlotContent> inputs = new ArrayList<>();
        inputs.add(SlotContent.of(dish(recipe.bacteria())));
        inputs.add(SlotContent.of(recipe.nutrient()));
        LytStandardRecipeBox.Builder builder =
                box("bacteria_incubation", NTBlocks.INCUBATOR.get()).customBody(LytIngredientGrid.row(inputs));
        builder.addBottom(label(Component.translatable("gmrecipes.nautec.growth", growth(recipe))));
        builder.addBottom(label(Component.translatable(
                "gmrecipes.nautec.consume_chance", DECIMAL.format(recipe.consumeChance() * 100.0))));
        return builder.build(holder);
    }

    private static LytBlock mixing(RecipeHolder<MixingRecipe> holder) {
        MixingRecipe recipe = holder.value();
        List<SlotContent> inputs = new ArrayList<>();
        for (IngredientWithCount ingredient : recipe.ingredients()) {
            inputs.add(counted(ingredient));
        }
        inputs.add(fluid(recipe.fluidIngredient()));
        List<SlotContent> outputs = new ArrayList<>();
        outputs.add(SlotContent.of(recipe.result()));
        outputs.add(fluid(recipe.fluidResult()));
        LytStandardRecipeBox.Builder builder = box("mixing", NTBlocks.MIXER.get());
        if (inputs.stream().anyMatch(content -> !content.isEmpty())) {
            builder.input(LytIngredientGrid.wrapped(inputs, 3));
        }
        if (outputs.stream().anyMatch(content -> !content.isEmpty())) {
            builder.output(LytIngredientGrid.wrapped(outputs, 2));
        }
        builder.addBottom(duration(recipe.duration()));
        return builder.build(holder);
    }

    private static LytBlock transformation(RecipeHolder<ItemTransformationRecipe> holder) {
        ItemTransformationRecipe recipe = holder.value();
        return box("item_transformation", NTBlocks.PRISMARINE_RELAY.get())
                .input(LytIngredientGrid.row(List.of(counted(recipe.ingredient()))))
                .output(LytIngredientGrid.row(List.of(SlotContent.of(recipe.result()))))
                .addBottom(duration(recipe.duration()))
                .addBottom(label(Component.translatable("gmrecipes.nautec.purity", DECIMAL.format(recipe.purity()))))
                .build(holder);
    }

    private static LytBlock etching(RecipeHolder<ItemEtchingRecipe> holder) {
        ItemEtchingRecipe recipe = holder.value();
        return box("item_etching", NTFluids.ETCHING_ACID.getBucket())
                .input(LytIngredientGrid.row(List.of(counted(recipe.ingredient()))))
                .output(LytIngredientGrid.row(List.of(SlotContent.of(recipe.result()))))
                .addBottom(duration(recipe.duration()))
                .build(holder);
    }

    private static LytBlock augmentation(RecipeHolder<AugmentationRecipe> holder) {
        AugmentationRecipe recipe = holder.value();
        List<SlotContent> inputs = new ArrayList<>();
        for (IngredientWithCount ingredient : recipe.ingredients()) {
            inputs.add(counted(ingredient));
        }
        LytStandardRecipeBox.Builder builder = box("augmentation", NTItems.CLAW_ROBOT_ARM.get());
        if (inputs.stream().anyMatch(content -> !content.isEmpty())) {
            builder.input(LytIngredientGrid.wrapped(inputs, 4));
        }
        builder.output(LytIngredientGrid.row(List.of(SlotContent.of(new ItemStack(recipe.augmentItem())))));
        Component name = augmentName(recipe);
        if (name != null) {
            builder.addTop(label(name));
        }
        return builder.build(holder);
    }

    private static LytBlock channeling(RecipeHolder<AquaticCatalystChannelingRecipe> holder) {
        AquaticCatalystChannelingRecipe recipe = holder.value();
        LytStandardRecipeBox.Builder builder = box("aquatic_catalyst_channeling", NTBlocks.AQUATIC_CATALYST.get())
                .customBody(LytIngredientGrid.row(List.of(SlotContent.of(recipe.ingredient()))));
        if (recipe.duration() > 0) {
            builder.addBottom(label(Component.translatable(
                    "gmrecipes.nautec.power_per_tick",
                    DECIMAL.format((double) recipe.powerAmount() / recipe.duration()))));
        }
        builder.addBottom(label(Component.translatable("gmrecipes.nautec.power_total", recipe.powerAmount())));
        builder.addBottom(duration(recipe.duration()));
        builder.addBottom(label(Component.translatable("gmrecipes.nautec.purity", DECIMAL.format(recipe.purity()))));
        return builder.build(holder);
    }

    private static SlotContent counted(IngredientWithCount ingredient) {
        return SlotContent.of(ingredient.ingredient()).withCount(ingredient.count());
    }

    private static SlotContent fluid(FluidStack stack) {
        return stack == null || stack.isEmpty() ? SlotContent.empty() : SlotContent.of(stack);
    }

    private static ItemStack dish(ResourceKey<Bacteria> bacteria) {
        RegistryAccess access = registryAccess();
        if (access == null) {
            return ItemStack.EMPTY;
        }
        try {
            return BacteriaHelper.getMaxStatDish(bacteria, access);
        } catch (RuntimeException e) {
            return ItemStack.EMPTY;
        }
    }

    private static String growth(BacteriaIncubationRecipe recipe) {
        int min = recipe.growth().getMin();
        int max = recipe.growth().getMax();
        return min == max ? String.valueOf(min) : min + " - " + max;
    }

    @Nullable
    private static Component augmentName(AugmentationRecipe recipe) {
        try {
            return Utils.registryTranslation(NTRegistries.AUGMENT_TYPE, recipe.resultAugment());
        } catch (RuntimeException e) {
            return null;
        }
    }

    @Nullable
    private static RegistryAccess registryAccess() {
        try {
            return Platform.getClientRegistryAccess();
        } catch (RuntimeException e) {
            Minecraft minecraft = Minecraft.getInstance();
            return minecraft.level == null ? null : minecraft.level.registryAccess();
        }
    }

    private static LytLabel duration(int ticks) {
        return label(Component.translatable(
                "gmrecipes.nautec.time",
                Component.translatable("gmrecipes.stat.ticks", ticks, DECIMAL.format(ticks / 20.0))));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(NAMESPACE, path);
    }

    private static LytStandardRecipeBox.Builder box(String name, ItemLike icon) {
        String translated = Translations.resolve("gmrecipes.recipe.nautec." + name);
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
