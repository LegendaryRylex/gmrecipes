package dev.rylex.gmrecipes.guide;

import dev.rylex.gmrecipes.guide.SupportedMods.SupportedMod;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

/**
 * A hand-written renderer registered earlier keeps the type, since the sweep skips whatever the
 * registrar already covers.
 */
public final class SimpleModRecipeMappings {

    private static final Map<String, String> TYPE_ICONS = Map.ofEntries(
            Map.entry("actuallyadditions:coffee_ingredient", "actuallyadditions:coffee_machine"),
            Map.entry("actuallyadditions:color_change", "actuallyadditions:atomic_reconstructor"),
            Map.entry("actuallyadditions:crushing", "actuallyadditions:crusher"),
            Map.entry("actuallyadditions:empower", "actuallyadditions:empowerer"),
            Map.entry("actuallyadditions:fermenting", "actuallyadditions:fermenting_barrel"),
            Map.entry("actuallyadditions:laser", "actuallyadditions:atomic_reconstructor"),
            Map.entry("actuallyadditions:liquid_fuel", "actuallyadditions:oil_generator"),
            Map.entry("actuallyadditions:mining_lens", "actuallyadditions:lens_of_the_miner"),
            Map.entry("actuallyadditions:pressing", "actuallyadditions:canola_press"),
            Map.entry("ars_nouveau:alakarkinos_conversion", "ars_nouveau:alakarkinos_charm"),
            Map.entry("ars_nouveau:armor_upgrade", "ars_nouveau:alteration_table"),
            Map.entry("ars_nouveau:caster_tome", "ars_nouveau:caster_tome"),
            Map.entry("ars_nouveau:enchanting_apparatus", "ars_nouveau:enchanting_apparatus"),
            Map.entry("ars_nouveau:enchantment", "ars_nouveau:enchanting_apparatus"),
            Map.entry("ars_nouveau:glyph", "ars_nouveau:scribes_table"),
            Map.entry("ars_nouveau:imbuement", "ars_nouveau:imbuement_chamber"),
            Map.entry("ars_nouveau:potion_flask", "ars_nouveau:potion_flask"),
            Map.entry("ars_nouveau:reactive_enchantment", "ars_nouveau:enchanting_apparatus"),
            Map.entry("ars_nouveau:scry_ritual", "ars_nouveau:ritual_scrying"),
            Map.entry("ars_nouveau:spell_write", "ars_nouveau:scribes_table"),
            Map.entry("ars_nouveau:summon_ritual", "ars_nouveau:ritual_brazier"),
            Map.entry("astralsorcery:celestial_gateway_change_color", "astralsorcery:celestial_gateway"),
            Map.entry("astralsorcery:illumination_wand_change_color", "astralsorcery:illumination_wand"),
            Map.entry(
                    "createbigcannons:autocannon_ammo_container_filling_deployer",
                    "createbigcannons:autocannon_ammo_container"),
            Map.entry("createbigcannons:big_cartridge_filling", "createbigcannons:big_cartridge"),
            Map.entry("createbigcannons:big_cartridge_filling_deployer", "createbigcannons:big_cartridge"),
            Map.entry("createbigcannons:cartridge_assembly", "createbigcannons:empty_autocannon_cartridge"),
            Map.entry("createbigcannons:cartridge_assembly_deployer", "createbigcannons:empty_autocannon_cartridge"),
            Map.entry("createbigcannons:fuze_removal", "createbigcannons:impact_fuze"),
            Map.entry("createbigcannons:munition_fuzing", "createbigcannons:impact_fuze"),
            Map.entry("createbigcannons:munition_fuzing_deployer", "createbigcannons:impact_fuze"),
            Map.entry("createbigcannons:tracer_application", "createbigcannons:tracer_tip"),
            Map.entry("createbigcannons:tracer_application_deployer", "createbigcannons:tracer_tip"),
            Map.entry("createbigcannons:tracer_removal", "createbigcannons:tracer_tip"),
            Map.entry("enderio:alloy_smelting", "enderio:alloy_smelter"),
            Map.entry("enderio:enchanting", "enderio:enchanter"),
            Map.entry("enderio:fire_crafting", "minecraft:flint_and_steel"),
            Map.entry("enderio:painting", "enderio:painting_machine"),
            Map.entry("enderio:sag_milling", "enderio:sag_mill"),
            Map.entry("enderio:slicing", "enderio:slice_and_splice"),
            Map.entry("enderio:soul_binding", "enderio:soul_binder"),
            Map.entry("enderio:tank", "enderio:fluid_tank"),
            Map.entry("enderio:vat_fermenting", "enderio:vat"),
            Map.entry("enderio:weather_change", "enderio:weather_obelisk"),
            Map.entry("farmersdelight:dough", "farmersdelight:wheat_dough"),
            Map.entry("farmersdelight:food_serving", "farmersdelight:cooking_pot"),
            Map.entry("geneticsresequenced:incubator", "geneticsresequenced:incubator"),
            Map.entry("hexerei:add_base_to_candle", "hexerei:candle"),
            Map.entry("hexerei:add_to_candle", "hexerei:candle"),
            Map.entry("hexerei:book_of_shadows", "hexerei:book_of_shadows"),
            Map.entry("hexerei:book_of_shadows_dye", "hexerei:book_of_shadows"),
            Map.entry("hexerei:crow_amulet_apply", "hexerei:crow_blank_amulet"),
            Map.entry("hexerei:crow_amulet_undo", "hexerei:crow_blank_amulet"),
            Map.entry("hexerei:crow_flute_dye", "hexerei:crow_flute"),
            Map.entry("hexerei:cut_candle", "hexerei:candle"),
            Map.entry("hexerei:fill_waxing_kit", "hexerei:waxing_kit"),
            Map.entry("hexerei:keychain_apply", "hexerei:broom_keychain"),
            Map.entry("hexerei:keychain_undo", "hexerei:broom_keychain"),
            Map.entry("hexerei:whistle_bind", "hexerei:broom_whistle"),
            Map.entry("hexerei:woodcutting", "hexerei:willow_woodcutter"),
            Map.entry("immersiveengineering:alloy", "immersiveengineering:alloy_smelter"),
            Map.entry("immersiveengineering:arc_furnace", "immersiveengineering:arc_furnace"),
            Map.entry("immersiveengineering:blast_furnace", "immersiveengineering:blast_furnace"),
            Map.entry("immersiveengineering:blast_furnace_fuel", "immersiveengineering:blast_furnace"),
            Map.entry("immersiveengineering:blueprint", "immersiveengineering:workbench"),
            Map.entry("immersiveengineering:bottling_machine", "immersiveengineering:bottling_machine"),
            Map.entry("immersiveengineering:cloche", "immersiveengineering:cloche"),
            Map.entry("immersiveengineering:coke_oven", "immersiveengineering:coke_oven"),
            Map.entry("immersiveengineering:crusher", "immersiveengineering:crusher"),
            Map.entry("immersiveengineering:fermenter", "immersiveengineering:fermenter"),
            Map.entry("immersiveengineering:fertilizer", "immersiveengineering:cloche"),
            Map.entry("immersiveengineering:generator_fuel", "immersiveengineering:diesel_generator"),
            Map.entry("immersiveengineering:metal_press", "immersiveengineering:metal_press"),
            Map.entry("immersiveengineering:mineral_mix", "immersiveengineering:sample_drill"),
            Map.entry("immersiveengineering:mixer", "immersiveengineering:mixer"),
            Map.entry("immersiveengineering:refinery", "immersiveengineering:refinery"),
            Map.entry("immersiveengineering:sawmill", "immersiveengineering:sawmill"),
            Map.entry("immersiveengineering:squeezer", "immersiveengineering:squeezer"),
            Map.entry("immersiveengineering:thermoelectric", "immersiveengineering:thermoelectric_generator"),
            Map.entry("immersiveengineering:windmill_biome", "immersiveengineering:windmill"),
            Map.entry("industrialforegoing:crusher", "industrialforegoing:material_stonework_factory"),
            Map.entry("integrateddynamics:drying_basin", "integrateddynamics:drying_basin"),
            Map.entry("integrateddynamics:mechanical_drying_basin", "integrateddynamics:mechanical_drying_basin"),
            Map.entry("integrateddynamics:mechanical_squeezer", "integrateddynamics:mechanical_squeezer"),
            Map.entry("integrateddynamics:squeezer", "integrateddynamics:squeezer"),
            Map.entry("justdirethings:abilityrecipe", "justdirethings:blazegold_axe"),
            Map.entry("justdirethings:paxelrecipe", "justdirethings:celestigem_paxel"),
            Map.entry("malum:node_blasting", "malum:aluminum_node"),
            Map.entry("malum:node_smelting", "malum:aluminum_node"),
            Map.entry("mekanism:activating", "mekanism:solar_neutron_activator"),
            Map.entry("mekanism:centrifuging", "mekanism:isotopic_centrifuge"),
            Map.entry("mekanism:chemical_infusing", "mekanism:chemical_infuser"),
            Map.entry("mekanism:combining", "mekanism:combiner"),
            Map.entry("mekanism:compressing", "mekanism:osmium_compressor"),
            Map.entry("mekanism:crushing", "mekanism:crusher"),
            Map.entry("mekanism:crystallizing", "mekanism:chemical_crystallizer"),
            Map.entry("mekanism:dissolution", "mekanism:chemical_dissolution_chamber"),
            Map.entry("mekanism:energy_conversion", "mekanism:basic_energy_cube"),
            Map.entry("mekanism:enriching", "mekanism:enrichment_chamber"),
            Map.entry("mekanism:evaporating", "mekanism:thermal_evaporation_controller"),
            Map.entry("mekanism:injecting", "mekanism:chemical_injection_chamber"),
            Map.entry("mekanism:metallurgic_infusing", "mekanism:metallurgic_infuser"),
            Map.entry("mekanism:nucleosynthesizing", "mekanism:antiprotonic_nucleosynthesizer"),
            Map.entry("mekanism:oxidizing", "mekanism:chemical_oxidizer"),
            Map.entry("mekanism:painting", "mekanism:painting_machine"),
            Map.entry("mekanism:pigment_extracting", "mekanism:pigment_extractor"),
            Map.entry("mekanism:pigment_mixing", "mekanism:pigment_mixer"),
            Map.entry("mekanism:purifying", "mekanism:purification_chamber"),
            Map.entry("mekanism:reaction", "mekanism:pressurized_reaction_chamber"),
            Map.entry("mekanism:rotary", "mekanism:rotary_condensentrator"),
            Map.entry("mekanism:sawing", "mekanism:precision_sawmill"),
            Map.entry("mekanism:separating", "mekanism:electrolytic_separator"),
            Map.entry("mekanism:smelting", "mekanism:energized_smelter"),
            Map.entry("mekanism:washing", "mekanism:chemical_washer"),
            Map.entry("mysticalagriculture:awakening", "mysticalagriculture:awakening_altar"),
            Map.entry("mysticalagriculture:enchanter", "mysticalagriculture:enchanter"),
            Map.entry("mysticalagriculture:infusion", "mysticalagriculture:infusion_altar"),
            Map.entry("mysticalagriculture:reprocessor", "mysticalagriculture:seed_reprocessor"),
            Map.entry("mysticalagriculture:soul_extraction", "mysticalagriculture:soul_extractor"),
            Map.entry("mysticalagriculture:soulium_spawner", "mysticalagriculture:soulium_spawner"),
            Map.entry("occultism:miner", "occultism:dimensional_mineshaft"),
            Map.entry("occultism:ritual", "occultism:golden_sacrificial_bowl"),
            Map.entry("oritech:augment_data", "oritech:simple_augment_station"),
            Map.entry("pneumaticcraft:assembly_drill_laser", "pneumaticcraft:assembly_controller"),
            Map.entry("pneumaticcraft:compressor_upgrade_crafting", "pneumaticcraft:advanced_air_compressor"),
            Map.entry("pneumaticcraft:crafting_shaped_pressurizable", "pneumaticcraft:air_canister"),
            Map.entry("pneumaticcraft:drone_color_crafting", "pneumaticcraft:drone"),
            Map.entry("pneumaticcraft:drone_upgrade_crafting", "pneumaticcraft:drone"),
            Map.entry("pneumaticcraft:fuel_quality", "pneumaticcraft:liquid_compressor"),
            Map.entry("pneumaticcraft:gun_ammo_potion_crafting", "pneumaticcraft:gun_ammo"),
            Map.entry("productivebees:bee_nbt_changer", "productivebees:honey_treat"),
            Map.entry("productivetrees:log_stripping", "productivetrees:stripper"),
            Map.entry("productivetrees:tree_fruiting", "productivetrees:pollen_sifter"),
            Map.entry("reliquary:alkahestry_charging", "reliquary:alkahestry_tome"),
            Map.entry("reliquary:alkahestry_crafting", "reliquary:alkahestry_tome"),
            Map.entry("reliquary:alkahestry_drain", "reliquary:alkahestry_tome"),
            Map.entry("reliquary:fragment_to_spawn_egg", "reliquary:mob_charm_fragment"),
            Map.entry("reliquary:infernal_tear_value", "reliquary:infernal_tear"),
            Map.entry("reliquary:mob_charm", "reliquary:mob_charm"),
            Map.entry("reliquary:potion_effects", "reliquary:apothecary_cauldron"),
            Map.entry("rftoolsutility:syringe", "rftoolsutility:syringe"));

    private static final Map<String, Integer> INPUT_COLUMNS = Map.of("oritech:augment_data", 2);

    private SimpleModRecipeMappings() {}

    public static void collectAll(MappingRegistrar registrar) {
        for (Identifier typeId : BuiltInRegistries.RECIPE_TYPE.keySet()) {
            String iconId = modIcon(typeId.getNamespace());
            if (iconId == null || registrar.covers(typeId)) {
                continue;
            }
            registrar.add(typeId, renderer(typeId, iconId)::render);
        }
    }

    @Nullable
    static GenericRecipeRenderer rendererFor(Identifier typeId) {
        String iconId = modIcon(typeId.getNamespace());
        return iconId == null ? null : renderer(typeId, iconId);
    }

    private static GenericRecipeRenderer renderer(Identifier typeId, String iconId) {
        ItemStack typeIcon = icon(TYPE_ICONS.getOrDefault(typeId.toString(), iconId));
        GenericRecipeRenderer renderer =
                GenericRecipeRenderer.of(typeId, typeIcon.isEmpty() ? icon(iconId) : typeIcon, titleKey(typeId));
        Integer columns = INPUT_COLUMNS.get(typeId.toString());
        return columns == null ? renderer : renderer.withInputColumns(columns);
    }

    @Nullable
    private static String modIcon(String namespace) {
        for (SupportedMod mod : SupportedMods.ALL) {
            if (mod.modId().equals(namespace)) {
                return mod.iconId();
            }
        }
        return null;
    }

    /**
     * Keyed under this mod's own namespace because a supported mod rarely names its recipe types in
     * its own lang file.
     */
    private static String titleKey(Identifier typeId) {
        return "gmrecipes.recipe." + typeId.getNamespace() + "." + typeId.getPath();
    }

    static ItemStack icon(String id) {
        Identifier itemId = Identifier.parse(id);
        Item item = BuiltInRegistries.ITEM.getValue(itemId);
        return item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
    }
}
