package dev.rylex.gmrecipes.guide;

import dev.rylex.gmrecipes.GmRecipes;
import dev.rylex.gmrecipes.compat.create.CreatePlaceholderRecipes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;

/**
 * Stand-ins for registered recipe types that never carry a loaded recipe, because the machine reads
 * another type's recipes at runtime or the type only parents others.
 */
public final class PlaceholderRecipes {

    private static final Map<ResourceLocation, Supplier<RecipeHolder<?>>> SUPPLIERS = suppliers();

    private PlaceholderRecipes() {}

    @Nullable
    public static RecipeHolder<?> forType(ResourceLocation typeId) {
        Supplier<RecipeHolder<?>> supplier = SUPPLIERS.get(typeId);
        if (supplier == null) {
            return null;
        }
        try {
            return supplier.get();
        } catch (RuntimeException | LinkageError e) {
            GmRecipes.LOGGER.warn("could not build a placeholder recipe for {}", typeId, e);
            return null;
        }
    }

    private static Map<ResourceLocation, Supplier<RecipeHolder<?>>> suppliers() {
        Map<ResourceLocation, Supplier<RecipeHolder<?>>> suppliers = new HashMap<>();
        put(
                suppliers,
                "mekanism:smelting",
                () -> simple("mekanism:smelting", List.of("minecraft:iron_ore"), "minecraft:iron_ingot"));
        put(
                suppliers,
                "modern_industrialization:furnace",
                () -> simple(
                        "modern_industrialization:furnace", List.of("minecraft:iron_ore"), "minecraft:iron_ingot"));
        put(
                suppliers,
                "ars_nouveau:book_upgrade",
                () -> simple(
                        "ars_nouveau:book_upgrade",
                        List.of(
                                "ars_nouveau:novice_spell_book",
                                "#c:obsidians",
                                "#c:gems/diamond",
                                "#c:gems/diamond",
                                "#c:gems/diamond",
                                "minecraft:quartz_block",
                                "minecraft:quartz_block",
                                "#c:rods/blaze",
                                "#c:rods/blaze"),
                        "ars_nouveau:apprentice_spell_book"));
        put(
                suppliers,
                "ars_nouveau:dye",
                () -> simple(
                        "ars_nouveau:dye",
                        List.of("#c:dyes", "ars_nouveau:apprentice_spell_book"),
                        "ars_nouveau:apprentice_spell_book"));
        put(
                suppliers,
                "ars_nouveau:potion_flask",
                () -> simple(
                        "ars_nouveau:potion_flask",
                        List.of("ars_nouveau:potion_flask", "minecraft:potion"),
                        "ars_nouveau:potion_flask"));
        if (ModList.get().isLoaded("pneumaticcraft")) {
            put(
                    suppliers,
                    "pneumaticcraft:assembly_drill_laser",
                    () -> simple(
                            "pneumaticcraft:assembly_drill_laser",
                            List.of("pneumaticcraft:printed_circuit_board"),
                            "pneumaticcraft:printed_circuit_board"));
        }
        if (ModList.get().isLoaded("hexerei")) {
            put(
                    suppliers,
                    "hexerei:add_to_candle",
                    () -> simple(
                            "hexerei:add_to_candle",
                            List.of("hexerei:candle", "hexerei:mindful_trance_blend"),
                            "hexerei:candle"));
            put(
                    suppliers,
                    "hexerei:add_base_to_candle",
                    () -> simple(
                            "hexerei:add_base_to_candle",
                            List.of("hexerei:candle", "minecraft:redstone_block"),
                            "hexerei:candle"));
            put(
                    suppliers,
                    "hexerei:fill_waxing_kit",
                    () -> simple(
                            "hexerei:fill_waxing_kit",
                            List.of("hexerei:waxing_kit", "hexerei:wax_blend"),
                            "hexerei:waxing_kit"));
            put(
                    suppliers,
                    "hexerei:book_of_shadows_dye",
                    () -> simple(
                            "hexerei:book_of_shadows_dye",
                            List.of("hexerei:book_of_shadows", "minecraft:red_dye"),
                            "hexerei:book_of_shadows"));
            put(
                    suppliers,
                    "hexerei:crow_amulet_apply",
                    () -> simple(
                            "hexerei:crow_amulet_apply",
                            List.of("hexerei:crow_blank_amulet", "minecraft:diamond"),
                            "hexerei:crow_blank_amulet"));
            put(
                    suppliers,
                    "hexerei:crow_amulet_undo",
                    () -> simple(
                            "hexerei:crow_amulet_undo", List.of("hexerei:crow_blank_amulet"), "minecraft:diamond"));
            put(
                    suppliers,
                    "hexerei:crow_flute_dye",
                    () -> simple(
                            "hexerei:crow_flute_dye",
                            List.of(
                                    "#c:dyes",
                                    "#c:dyes",
                                    "hexerei:mahogany_planks",
                                    "hexerei:mahogany_planks",
                                    "hexerei:mahogany_planks"),
                            "hexerei:crow_flute"));
            put(
                    suppliers,
                    "hexerei:cut_candle",
                    () -> simple(
                            "hexerei:cut_candle", List.of("minecraft:iron_sword", "hexerei:candle"), "hexerei:candle"));
            put(
                    suppliers,
                    "hexerei:keychain_apply",
                    () -> simple(
                            "hexerei:keychain_apply",
                            List.of("hexerei:broom_keychain", "minecraft:diamond"),
                            "hexerei:broom_keychain"));
            put(
                    suppliers,
                    "hexerei:keychain_undo",
                    () -> simple("hexerei:keychain_undo", List.of("hexerei:broom_keychain"), "minecraft:diamond"));
            put(
                    suppliers,
                    "hexerei:whistle_bind",
                    () -> simple(
                            "hexerei:whistle_bind",
                            List.of("hexerei:broom_whistle", "hexerei:mahogany_broom"),
                            "hexerei:broom_whistle"));
        }
        if (ModList.get().isLoaded("createbigcannons")) {
            put(
                    suppliers,
                    "createbigcannons:cartridge_assembly",
                    () -> simple(
                            "createbigcannons:cartridge_assembly",
                            List.of(
                                    "createbigcannons:ap_autocannon_round",
                                    "createbigcannons:filled_autocannon_cartridge"),
                            "createbigcannons:filled_autocannon_cartridge"));
            put(
                    suppliers,
                    "createbigcannons:big_cartridge_filling",
                    () -> simple(
                            "createbigcannons:big_cartridge_filling",
                            List.of("createbigcannons:big_cartridge", "#createbigcannons:nitropowder"),
                            "createbigcannons:big_cartridge"));
            put(
                    suppliers,
                    "createbigcannons:munition_fuzing",
                    () -> simple(
                            "createbigcannons:munition_fuzing",
                            List.of("createbigcannons:flak_autocannon_round", "createbigcannons:impact_fuze"),
                            "createbigcannons:flak_autocannon_round"));
            put(
                    suppliers,
                    "createbigcannons:fuze_removal",
                    () -> simple(
                            "createbigcannons:fuze_removal",
                            List.of("createbigcannons:flak_autocannon_round"),
                            "createbigcannons:impact_fuze"));
            put(
                    suppliers,
                    "createbigcannons:tracer_application",
                    () -> simple(
                            "createbigcannons:tracer_application",
                            List.of("createbigcannons:ap_autocannon_round", "createbigcannons:tracer_tip"),
                            "createbigcannons:ap_autocannon_round"));
            put(
                    suppliers,
                    "createbigcannons:tracer_removal",
                    () -> simple(
                            "createbigcannons:tracer_removal",
                            List.of("createbigcannons:ap_autocannon_round"),
                            "createbigcannons:tracer_tip"));
            put(
                    suppliers,
                    "createbigcannons:munition_fuzing_deployer",
                    () -> simple(
                            "createbigcannons:munition_fuzing_deployer",
                            List.of("createbigcannons:flak_autocannon_round", "createbigcannons:impact_fuze"),
                            "createbigcannons:flak_autocannon_round"));
            put(
                    suppliers,
                    "createbigcannons:tracer_application_deployer",
                    () -> simple(
                            "createbigcannons:tracer_application_deployer",
                            List.of("createbigcannons:ap_autocannon_round", "createbigcannons:tracer_tip"),
                            "createbigcannons:ap_autocannon_round"));
            put(
                    suppliers,
                    "createbigcannons:autocannon_ammo_container_filling_deployer",
                    () -> simple(
                            "createbigcannons:autocannon_ammo_container_filling_deployer",
                            List.of("createbigcannons:autocannon_ammo_container", "createbigcannons:machine_gun_round"),
                            "createbigcannons:autocannon_ammo_container"));
        }
        if (ModList.get().isLoaded("treetap")) {
            put(
                    suppliers,
                    "treetap:tap_extract",
                    () -> simple("treetap:tap_extract", List.of("minecraft:oak_log"), "minecraft:water_bucket"));
        }
        if (ModList.get().isLoaded("create")) {
            put(suppliers, "create:basin", CreatePlaceholderRecipes::basin);
            put(suppliers, "create:conversion", CreatePlaceholderRecipes::conversion);
        }
        return Map.copyOf(suppliers);
    }

    private static void put(
            Map<ResourceLocation, Supplier<RecipeHolder<?>>> suppliers,
            String typeId,
            Supplier<RecipeHolder<?>> supplier) {
        ResourceLocation parsed = ResourceLocation.tryParse(typeId);
        if (parsed == null) {
            GmRecipes.LOGGER.warn("ignoring malformed placeholder recipe type id {}", typeId);
            return;
        }
        suppliers.put(parsed, supplier);
    }

    /** Null when the type or the result item is not registered. */
    @Nullable
    private static RecipeHolder<?> simple(String typeId, List<String> inputs, String result) {
        ResourceLocation parsed = ResourceLocation.parse(typeId);
        RecipeType<?> type = BuiltInRegistries.RECIPE_TYPE.get(parsed);
        ItemStack resultStack = stack(result);
        if (type == null || resultStack.isEmpty()) {
            return null;
        }
        NonNullList<Ingredient> ingredients = NonNullList.create();
        for (String input : inputs) {
            Ingredient ingredient = ingredient(input);
            if (!ingredient.isEmpty()) {
                ingredients.add(ingredient);
            }
        }
        PlaceholderRecipe recipe = new PlaceholderRecipe(type, ingredients, resultStack);
        return new RecipeHolder<>(
                GmRecipes.id("placeholder/" + parsed.getNamespace() + "/" + parsed.getPath()), recipe);
    }

    private static Ingredient ingredient(String id) {
        if (id.startsWith("#")) {
            ResourceLocation tagId = ResourceLocation.tryParse(id.substring(1));
            return tagId == null ? Ingredient.EMPTY : Ingredient.of(TagKey.create(Registries.ITEM, tagId));
        }
        ItemStack stack = stack(id);
        return stack.isEmpty() ? Ingredient.EMPTY : Ingredient.of(stack);
    }

    private static ItemStack stack(String id) {
        ResourceLocation itemId = ResourceLocation.tryParse(id);
        if (itemId == null) {
            return ItemStack.EMPTY;
        }
        Item item = BuiltInRegistries.ITEM.get(itemId);
        return item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
    }
}
