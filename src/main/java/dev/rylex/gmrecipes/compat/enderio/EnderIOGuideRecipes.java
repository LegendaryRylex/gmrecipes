package dev.rylex.gmrecipes.compat.enderio;

import com.enderio.enderio.api.soul.Soul;
import com.enderio.enderio.api.soul.SoulBoundUtils;
import com.enderio.enderio.content.enchanter.EnchanterRecipe;
import com.enderio.enderio.content.fire_crafting.FireCraftingRecipe;
import com.enderio.enderio.content.machines.alloy.AlloySmeltingRecipe;
import com.enderio.enderio.content.machines.obelisks.weather.WeatherChangeRecipe;
import com.enderio.enderio.content.machines.painting.PaintingRecipe;
import com.enderio.enderio.content.machines.sag_mill.SagMillingRecipe;
import com.enderio.enderio.content.machines.slicer.SlicingRecipe;
import com.enderio.enderio.content.machines.soul_binder.SoulBindingRecipe;
import com.enderio.enderio.content.machines.vat.FermentingRecipe;
import com.enderio.enderio.content.storage.fluid_tank.TankRecipe;
import com.enderio.enderio.foundation.souldata.SoulDataReloadListener;
import com.enderio.enderio.foundation.util.EntityCaptureUtils;
import com.enderio.enderio.foundation.util.SizedFluidIngredientHelper;
import com.enderio.enderio.init.EIOBlocks;
import com.enderio.enderio.init.EIODataComponents;
import dev.rylex.gmrecipes.guide.LytIngredientGrid;
import dev.rylex.gmrecipes.guide.LytLabel;
import dev.rylex.gmrecipes.guide.LytTextureSlot;
import dev.rylex.gmrecipes.guide.MappingRegistrar;
import dev.rylex.gmrecipes.guide.RecipeBoxStyle;
import dev.rylex.gmrecipes.guide.SlotContent;
import dev.rylex.gmrecipes.guide.Translations;
import guideme.document.block.LytBlock;
import guideme.document.block.recipes.LytStandardRecipeBox;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.stream.Collectors;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

public final class EnderIOGuideRecipes {

    private static final ResourceLocation FIRE_WATER_BUCKET =
            ResourceLocation.fromNamespaceAndPath("enderio", "fire_water_bucket");
    private static final ResourceLocation SOUL_VIAL = ResourceLocation.fromNamespaceAndPath("enderio", "soul_vial");
    private static final DecimalFormat DECIMAL =
            new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.ROOT));

    private EnderIOGuideRecipes() {}

    public static void collect(MappingRegistrar registrar) {
        add(registrar, "alloy_smelting", EnderIOGuideRecipes::alloySmelting);
        add(registrar, "enchanting", EnderIOGuideRecipes::enchanting);
        add(registrar, "sag_milling", EnderIOGuideRecipes::sagMilling);
        add(registrar, "slicing", EnderIOGuideRecipes::slicing);
        add(registrar, "soul_binding", EnderIOGuideRecipes::soulBinding);
        add(registrar, "tank", EnderIOGuideRecipes::tank);
        add(registrar, "vat_fermenting", EnderIOGuideRecipes::vatFermenting);
        add(registrar, "weather_change", EnderIOGuideRecipes::weatherChange);
        add(registrar, "fire_crafting", EnderIOGuideRecipes::fireCrafting);
        add(registrar, "painting", EnderIOGuideRecipes::painting);
    }

    /** Ender IO holds its recipe types in a jar-in-jar library class, so they are taken from the registry by id instead. */
    @SuppressWarnings("unchecked")
    private static <T extends Recipe<C>, C extends RecipeInput> void add(
            MappingRegistrar registrar, String path, Function<RecipeHolder<T>, LytBlock> renderer) {
        RecipeType<?> type = BuiltInRegistries.RECIPE_TYPE.get(ResourceLocation.fromNamespaceAndPath("enderio", path));
        if (type != null) {
            registrar.add((RecipeType<T>) type, renderer);
        }
    }

    private static LytBlock alloySmelting(RecipeHolder<AlloySmeltingRecipe> holder) {
        AlloySmeltingRecipe recipe = holder.value();
        return withEnergy(
                        box("alloy_smelting", EIOBlocks.ALLOY_SMELTER.get())
                                .input(LytIngredientGrid.row(recipe.inputs().stream()
                                        .map(SlotContent::of)
                                        .toList()))
                                .output(LytIngredientGrid.row(List.of(SlotContent.of(recipe.output())))),
                        recipe::getBaseEnergyCost)
                .build(holder);
    }

    private static LytBlock enchanting(RecipeHolder<EnchanterRecipe> holder) {
        EnchanterRecipe recipe = holder.value();
        int maxLevel = recipe.enchantment().value().getMaxLevel();
        ItemStack[] inputs = recipe.input().getItems();
        ItemStack[] lapis = Ingredient.of(Tags.Items.GEMS_LAPIS).getItems();
        List<ItemStack> inputByLevel = new ArrayList<>();
        List<ItemStack> lapisByLevel = new ArrayList<>();
        List<ItemStack> books = new ArrayList<>();
        for (int level = 1; level <= maxLevel; level++) {
            int lapisCount = lapisForLevel(recipe, level);
            for (ItemStack input : inputs) {
                inputByLevel.add(input.copyWithCount(input.getCount() * level));
                lapisByLevel.add(lapis.length == 0 ? ItemStack.EMPTY : lapis[0].copyWithCount(lapisCount));
                books.add(recipe.getBookForLevel(level));
            }
        }
        LytStandardRecipeBox.Builder builder = box("enchanting", EIOBlocks.ENCHANTER.get())
                .addTop(label(Enchantment.getFullname(recipe.enchantment(), maxLevel)))
                .input(LytIngredientGrid.row(List.of(
                        SlotContent.of(new ItemStack(Items.WRITABLE_BOOK)),
                        SlotContent.ofItems(inputByLevel),
                        SlotContent.ofItems(lapisByLevel))))
                .output(LytIngredientGrid.row(List.of(SlotContent.ofItems(books))));
        String costs = xpCosts(recipe, maxLevel);
        if (costs != null) {
            builder.addBottom(label(Component.translatable("gmrecipes.enderio.enchant_cost", costs)));
        }
        return builder.build(holder);
    }

    private static int lapisForLevel(EnchanterRecipe recipe, int level) {
        try {
            return recipe.getLapisForLevel(level);
        } catch (IllegalStateException e) {
            return level;
        }
    }

    private static String xpCosts(EnchanterRecipe recipe, int maxLevel) {
        try {
            List<String> costs = new ArrayList<>();
            for (int level = 1; level <= maxLevel; level++) {
                costs.add(Integer.toString(recipe.getXPCostForLevel(level)));
            }
            return String.join(", ", costs);
        } catch (IllegalStateException e) {
            return null;
        }
    }

    private static LytBlock sagMilling(RecipeHolder<SagMillingRecipe> holder) {
        SagMillingRecipe recipe = holder.value();
        List<SlotContent> outputs = new ArrayList<>();
        for (SagMillingRecipe.OutputItem output : recipe.outputs()) {
            ItemStack stack = output.getItemStack();
            if (!stack.isEmpty()) {
                outputs.add(SlotContent.of(stack).withChance(output.chance()));
            }
        }
        return withEnergy(
                        box("sag_milling", EIOBlocks.SAG_MILL.get())
                                .input(LytIngredientGrid.row(List.of(SlotContent.of(recipe.input()))))
                                .output(LytIngredientGrid.wrapped(outputs, 4)),
                        recipe::getBaseEnergyCost)
                .build(holder);
    }

    private static LytBlock slicing(RecipeHolder<SlicingRecipe> holder) {
        SlicingRecipe recipe = holder.value();
        return withEnergy(
                        box("slicing", EIOBlocks.SLICE_AND_SPLICE.get())
                                .addTop(LytIngredientGrid.row(List.of(
                                        SlotContent.of(Ingredient.of(ItemTags.AXES)),
                                        SlotContent.of(Ingredient.of(Tags.Items.TOOLS_SHEAR)))))
                                .input(LytIngredientGrid.wrapped(
                                        recipe.inputs().stream()
                                                .map(SlotContent::of)
                                                .toList(),
                                        3))
                                .output(LytIngredientGrid.row(List.of(SlotContent.of(recipe.output())))),
                        recipe::getBaseEnergyCost)
                .build(holder);
    }

    private static LytBlock soulBinding(RecipeHolder<SoulBindingRecipe> holder) {
        SoulBindingRecipe recipe = holder.value();
        List<ItemStack> vials = new ArrayList<>();
        Component soul = null;
        if (recipe.entityType().isPresent()) {
            ResourceLocation id = recipe.entityType().get();
            vials.add(vial(Soul.of(id)));
            soul = BuiltInRegistries.ENTITY_TYPE.get(id).getDescription();
        } else if (recipe.mobCategory().isPresent()) {
            MobCategory category = recipe.mobCategory().get();
            for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
                if (type.getCategory() == category) {
                    vials.add(vial(Soul.of(type)));
                }
            }
            soul = Component.translatable("gmrecipes.enderio.soul_category", category.getName());
        } else if (recipe.soulData().isPresent()) {
            SoulDataReloadListener<?> data =
                    SoulDataReloadListener.fromString(recipe.soulData().get());
            if (data != null) {
                for (ResourceLocation id : data.map.keySet()) {
                    vials.add(vial(Soul.of(id)));
                }
            }
        }
        if (vials.isEmpty()) {
            for (EntityType<?> type : EntityCaptureUtils.getCapturableEntityTypes()) {
                vials.add(vial(Soul.of(type)));
            }
        }
        List<ItemStack> results = new ArrayList<>();
        for (ItemStack vial : vials) {
            ItemStack result = recipe.output().copy();
            if (SoulBoundUtils.canBindSoul(result)) {
                SoulBoundUtils.tryBindSoul(result, SoulBoundUtils.getBoundSoul(vial));
            }
            results.add(result);
        }
        LytStandardRecipeBox.Builder builder = box("soul_binding", EIOBlocks.SOUL_BINDER.get())
                .input(LytIngredientGrid.row(List.of(SlotContent.ofItems(vials), SlotContent.of(recipe.input()))))
                .output(LytIngredientGrid.row(List.of(
                        SlotContent.of(new ItemStack(BuiltInRegistries.ITEM.get(SOUL_VIAL))),
                        SlotContent.ofItems(results))));
        if (soul != null) {
            builder.addTop(label(Component.translatable("gmrecipes.enderio.soul", soul)));
        }
        if (recipe.experience() > 0) {
            builder.addBottom(label(Component.translatable("gmrecipes.enderio.xp_cost", recipe.experience())));
        }
        return withEnergy(builder, recipe::getBaseEnergyCost).build(holder);
    }

    private static ItemStack vial(Soul soul) {
        ItemStack vial = new ItemStack(BuiltInRegistries.ITEM.get(SOUL_VIAL));
        vial.set(EIODataComponents.SOUL, soul);
        return vial;
    }

    private static LytBlock tank(RecipeHolder<TankRecipe> holder) {
        TankRecipe recipe = holder.value();
        SlotContent fluid = SlotContent.ofFluids(fluids(recipe.fluid()));
        SlotContent item = SlotContent.of(recipe.input());
        SlotContent result = SlotContent.of(recipe.output());
        LytStandardRecipeBox.Builder builder = box("tank", EIOBlocks.FLUID_TANK.get());
        if (recipe.mode() == TankRecipe.Mode.FILL) {
            builder.input(LytIngredientGrid.row(List.of(fluid, item))).output(LytIngredientGrid.row(List.of(result)));
        } else {
            builder.input(LytIngredientGrid.row(List.of(item))).output(LytIngredientGrid.row(List.of(result, fluid)));
        }
        return builder.build(holder);
    }

    private static List<FluidStack> fluids(SizedFluidIngredient ingredient) {
        return SizedFluidIngredientHelper.getFluidStacksInPreferredOrder(ingredient);
    }

    private static LytBlock vatFermenting(RecipeHolder<FermentingRecipe> holder) {
        FermentingRecipe recipe = holder.value();
        return box("vat_fermenting", EIOBlocks.VAT.get())
                .input(LytIngredientGrid.row(List.of(
                        SlotContent.of(recipe.input()),
                        SlotContent.of(Ingredient.of(recipe.firstReagent())),
                        SlotContent.of(Ingredient.of(recipe.secondReagent())))))
                .output(LytIngredientGrid.row(List.of(SlotContent.of(recipe.output()))))
                .addBottom(label(Component.translatable(
                        "gmrecipes.enderio.time",
                        Component.translatable(
                                "gmrecipes.stat.ticks", recipe.ticks(), DECIMAL.format(recipe.ticks() / 20.0)))))
                .build(holder);
    }

    private static LytBlock weatherChange(RecipeHolder<WeatherChangeRecipe> holder) {
        WeatherChangeRecipe recipe = holder.value();
        String mode = recipe.mode().getSerializedName();
        Component name = Component.translatable("gmrecipes.enderio.weather." + mode);
        return box("weather_change", EIOBlocks.WEATHER_OBELISK.get())
                .addTop(label(name))
                .input(LytIngredientGrid.row(
                        List.of(SlotContent.of(recipe.fluid()), SlotContent.of(new ItemStack(Items.FIREWORK_ROCKET)))))
                .output(LytIngredientGrid.of(new LytTextureSlot(weatherIcon(mode), name)))
                .build(holder);
    }

    private static ResourceLocation weatherIcon(String mode) {
        String icon =
                switch (mode) {
                    case "rain" -> "weather_rain";
                    case "lightning" -> "weather_thunder";
                    default -> "weather_sun";
                };
        return ResourceLocation.fromNamespaceAndPath("enderio", "textures/gui/icons/" + icon + ".png");
    }

    private static LytBlock fireCrafting(RecipeHolder<FireCraftingRecipe> holder) {
        FireCraftingRecipe recipe = holder.value();
        List<ItemStack> bases = recipe.getAllBaseBlocks().stream()
                .map(Block::asItem)
                .filter(item -> item != Items.AIR)
                .map(ItemStack::new)
                .toList();
        List<ItemStack> igniters = new ArrayList<>();
        igniters.add(new ItemStack(Items.FLINT_AND_STEEL));
        Item fireWater = BuiltInRegistries.ITEM.get(FIRE_WATER_BUCKET);
        if (fireWater != Items.AIR) {
            igniters.add(new ItemStack(fireWater));
        }
        List<SlotContent> outputs = new ArrayList<>();
        LytStandardRecipeBox.Builder builder = box("fire_crafting", Items.FLINT_AND_STEEL)
                .input(LytIngredientGrid.row(List.of(SlotContent.ofItems(bases), SlotContent.ofItems(igniters))));
        for (FireCraftingRecipe.Result result : recipe.results()) {
            outputs.add(SlotContent.of(result.result().copyWithCount(Math.max(1, result.maxCount())))
                    .withChance(result.chance()));
            String count = result.minCount() == result.maxCount()
                    ? Integer.toString(result.minCount())
                    : result.minCount() + "-" + result.maxCount();
            builder.addBottom(label(Component.translatable(
                    "gmrecipes.enderio.fire_drop", result.result().getHoverName(), count)));
        }
        builder.output(LytIngredientGrid.wrapped(outputs, 3));
        if (!recipe.dimensions().isEmpty()) {
            builder.addBottom(label(Component.translatable(
                    "gmrecipes.enderio.dimensions",
                    recipe.dimensions().stream()
                            .map(ResourceKey::location)
                            .map(ResourceLocation::toString)
                            .collect(Collectors.joining(", ")))));
        }
        recipe.blockAfterBurning()
                .ifPresent(block -> builder.addBottom(
                        label(Component.translatable("gmrecipes.enderio.leaves_block", block.getName()))));
        return builder.build(holder);
    }

    private static LytBlock painting(RecipeHolder<PaintingRecipe> holder) {
        PaintingRecipe recipe = holder.value();
        List<ItemStack> paints = List.of(
                new ItemStack(Items.OAK_PLANKS),
                new ItemStack(Items.STONE),
                new ItemStack(Items.BRICKS),
                new ItemStack(Items.QUARTZ_BLOCK));
        return withEnergy(
                        box("painting", EIOBlocks.PAINTING_MACHINE.get())
                                .input(LytIngredientGrid.row(
                                        List.of(SlotContent.of(recipe.input()), SlotContent.ofItems(paints))))
                                .output(LytIngredientGrid.row(List.of(SlotContent.of(recipe.output())))),
                        recipe::getBaseEnergyCost)
                .build(holder);
    }

    private static LytStandardRecipeBox.Builder withEnergy(LytStandardRecipeBox.Builder builder, IntSupplier energy) {
        try {
            int amount = energy.getAsInt();
            if (amount > 0) {
                builder.addBottom(label(Component.translatable(
                        "gmrecipes.enderio.energy",
                        NumberFormat.getIntegerInstance(Locale.ENGLISH).format(amount))));
            }
        } catch (IllegalStateException e) {
            return builder;
        }
        return builder;
    }

    private static LytStandardRecipeBox.Builder box(String name, ItemLike icon) {
        String translated = Translations.resolve("gmrecipes.recipe.enderio." + name);
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
