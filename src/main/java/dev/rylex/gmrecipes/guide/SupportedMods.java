package dev.rylex.gmrecipes.guide;

import dev.rylex.gmrecipes.compat.actuallyadditions.ActuallyAdditionsGuideRecipes;
import dev.rylex.gmrecipes.compat.advanced_ae.AdvancedAEGuideRecipes;
import dev.rylex.gmrecipes.compat.apotheosis.ApotheosisGuideRecipes;
import dev.rylex.gmrecipes.compat.apothic_enchanting.ApothicEnchantingGuideRecipes;
import dev.rylex.gmrecipes.compat.ars_nouveau.ArsNouveauGuideRecipes;
import dev.rylex.gmrecipes.compat.astralsorcery.AstralSorceryGuideRecipes;
import dev.rylex.gmrecipes.compat.cognition.CognitionGuideRecipes;
import dev.rylex.gmrecipes.compat.create.CreateGuideRecipes;
import dev.rylex.gmrecipes.compat.createaddition.CreateCraftsAdditionsGuideRecipes;
import dev.rylex.gmrecipes.compat.createbigcannons.CreateBigCannonsGuideRecipes;
import dev.rylex.gmrecipes.compat.draconicevolution.DraconicEvolutionGuideRecipes;
import dev.rylex.gmrecipes.compat.enderio.EnderIOGuideRecipes;
import dev.rylex.gmrecipes.compat.eternal_starlight.EternalStarlightGuideRecipes;
import dev.rylex.gmrecipes.compat.extended_industrialization.ExtendedIndustrializationGuideRecipes;
import dev.rylex.gmrecipes.compat.extendedae.ExtendedAEGuideRecipes;
import dev.rylex.gmrecipes.compat.farmersdelight.FarmersDelightGuideRecipes;
import dev.rylex.gmrecipes.compat.forbidden_arcanus.ForbiddenArcanusGuideRecipes;
import dev.rylex.gmrecipes.compat.geneticsresequenced.GeneticsResequencedGuideRecipes;
import dev.rylex.gmrecipes.compat.hexerei.HexereiGuideRecipes;
import dev.rylex.gmrecipes.compat.immersiveengineering.ImmersiveEngineeringGuideRecipes;
import dev.rylex.gmrecipes.compat.industrialforegoing.IndustrialForegoingGuideRecipes;
import dev.rylex.gmrecipes.compat.integrateddynamics.IntegratedDynamicsGuideRecipes;
import dev.rylex.gmrecipes.compat.irons_spellbooks.IronsSpellbooksGuideRecipes;
import dev.rylex.gmrecipes.compat.justdirethings.JustDireThingsGuideRecipes;
import dev.rylex.gmrecipes.compat.malum.MalumGuideRecipes;
import dev.rylex.gmrecipes.compat.mekanism.MekanismGuideRecipes;
import dev.rylex.gmrecipes.compat.mob_grinding_utils.MobGrindingUtilsGuideRecipes;
import dev.rylex.gmrecipes.compat.modern_industrialization.ModernIndustrializationGuideRecipes;
import dev.rylex.gmrecipes.compat.morered.MoreRedGuideRecipes;
import dev.rylex.gmrecipes.compat.mysticalagriculture.MysticalAgricultureGuideRecipes;
import dev.rylex.gmrecipes.compat.mysticalautomation.MysticalAutomationGuideRecipes;
import dev.rylex.gmrecipes.compat.nautec.NauTecGuideRecipes;
import dev.rylex.gmrecipes.compat.neovitae.NeoVitaeGuideRecipes;
import dev.rylex.gmrecipes.compat.occultism.OccultismGuideRecipes;
import dev.rylex.gmrecipes.compat.oritech.OritechGuideRecipes;
import dev.rylex.gmrecipes.compat.paganbless.PaganBlessGuideRecipes;
import dev.rylex.gmrecipes.compat.pneumaticcraft.PneumaticCraftRepressurizedGuideRecipes;
import dev.rylex.gmrecipes.compat.powah.PowahGuideRecipes;
import dev.rylex.gmrecipes.compat.productivebees.ProductiveBeesGuideRecipes;
import dev.rylex.gmrecipes.compat.productivetrees.ProductiveTreesGuideRecipes;
import dev.rylex.gmrecipes.compat.pylons.PylonsGuideRecipes;
import dev.rylex.gmrecipes.compat.reliquary.ReliquaryReincarnationsGuideRecipes;
import dev.rylex.gmrecipes.compat.rftoolsutility.RFToolsUtilityGuideRecipes;
import dev.rylex.gmrecipes.compat.rootsclassic.RootsClassicGuideRecipes;
import dev.rylex.gmrecipes.compat.sfm.SfmGuideRecipes;
import dev.rylex.gmrecipes.compat.treetap.TreeTapGuideRecipes;
import dev.rylex.gmrecipes.compat.xycraft_machines.XyCraftMachinesGuideRecipes;
import java.util.List;
import java.util.function.Consumer;

public final class SupportedMods {

    /**
     * Every collector is a lambda, never a method reference, because a method reference links its
     * compat class when this list is built and would load a foreign class for a mod that is absent.
     */
    public static final List<SupportedMod> ALL = List.of(
            new SupportedMod(
                    "actuallyadditions",
                    "actuallyadditions:empowerer",
                    registrar -> ActuallyAdditionsGuideRecipes.collect(registrar)),
            new SupportedMod(
                    "advanced_ae",
                    "advanced_ae:reaction_chamber",
                    registrar -> AdvancedAEGuideRecipes.collect(registrar)),
            new SupportedMod(
                    "apotheosis", "apotheosis:salvaging_table", registrar -> ApotheosisGuideRecipes.collect(registrar)),
            new SupportedMod(
                    "apothic_enchanting",
                    "apothic_enchanting:library",
                    registrar -> ApothicEnchantingGuideRecipes.collect(registrar)),
            new SupportedMod(
                    "ars_nouveau",
                    "ars_nouveau:enchanting_apparatus",
                    registrar -> ArsNouveauGuideRecipes.collect(registrar)),
            new SupportedMod(
                    "astralsorcery",
                    "astralsorcery:altar_luminance",
                    registrar -> AstralSorceryGuideRecipes.collect(registrar)),
            new SupportedMod(
                    "cognition",
                    "cognition:molecular_metamorpher",
                    registrar -> CognitionGuideRecipes.collect(registrar)),
            new SupportedMod("create", "create:mechanical_crafter", registrar -> CreateGuideRecipes.collect(registrar)),
            new SupportedMod(
                    "createaddition",
                    "createaddition:rolling_mill",
                    registrar -> CreateCraftsAdditionsGuideRecipes.collect(registrar)),
            new SupportedMod(
                    "createbigcannons",
                    "createbigcannons:basin_foundry_lid",
                    registrar -> CreateBigCannonsGuideRecipes.collect(registrar)),
            new SupportedMod(
                    "draconicevolution",
                    "draconicevolution:crafting_core",
                    registrar -> DraconicEvolutionGuideRecipes.collect(registrar)),
            new SupportedMod("enderio", "enderio:sag_mill", registrar -> EnderIOGuideRecipes.collect(registrar)),
            new SupportedMod(
                    "eternal_starlight",
                    "eternal_starlight:alloy_furnace",
                    registrar -> EternalStarlightGuideRecipes.collect(registrar)),
            new SupportedMod(
                    "extended_industrialization",
                    "extended_industrialization:electric_bending_machine",
                    registrar -> ExtendedIndustrializationGuideRecipes.collect(registrar)),
            new SupportedMod(
                    "extendedae",
                    "extendedae:crystal_assembler",
                    registrar -> ExtendedAEGuideRecipes.collect(registrar)),
            new SupportedMod(
                    "farmersdelight",
                    "farmersdelight:cooking_pot",
                    registrar -> FarmersDelightGuideRecipes.collect(registrar)),
            new SupportedMod(
                    "forbidden_arcanus",
                    "forbidden_arcanus:clibano_core",
                    registrar -> ForbiddenArcanusGuideRecipes.collect(registrar)),
            new SupportedMod(
                    "geneticsresequenced",
                    "geneticsresequenced:incubator",
                    registrar -> GeneticsResequencedGuideRecipes.collect(registrar)),
            new SupportedMod("hexerei", "hexerei:mixing_cauldron", registrar -> HexereiGuideRecipes.collect(registrar)),
            new SupportedMod(
                    "immersiveengineering",
                    "immersiveengineering:crusher",
                    registrar -> ImmersiveEngineeringGuideRecipes.collect(registrar)),
            new SupportedMod(
                    "industrialforegoing",
                    "industrialforegoing:dissolution_chamber",
                    registrar -> IndustrialForegoingGuideRecipes.collect(registrar)),
            new SupportedMod(
                    "integrateddynamics",
                    "integrateddynamics:mechanical_squeezer",
                    registrar -> IntegratedDynamicsGuideRecipes.collect(registrar)),
            new SupportedMod(
                    "irons_spellbooks",
                    "irons_spellbooks:alchemist_cauldron",
                    registrar -> IronsSpellbooksGuideRecipes.collect(registrar)),
            new SupportedMod(
                    "justdirethings",
                    "justdirethings:gooblock_tier1",
                    registrar -> JustDireThingsGuideRecipes.collect(registrar)),
            new SupportedMod("malum", "malum:spirit_altar", registrar -> MalumGuideRecipes.collect(registrar)),
            new SupportedMod("mekanism", "mekanism:crusher", registrar -> MekanismGuideRecipes.collect(registrar)),
            new SupportedMod(
                    "mob_grinding_utils",
                    "mob_grinding_utils:saw",
                    registrar -> MobGrindingUtilsGuideRecipes.collect(registrar)),
            new SupportedMod(
                    "modern_industrialization",
                    "modern_industrialization:electric_blast_furnace",
                    registrar -> ModernIndustrializationGuideRecipes.collect(registrar)),
            new SupportedMod("morered", "morered:soldering_table", registrar -> MoreRedGuideRecipes.collect(registrar)),
            new SupportedMod(
                    "mysticalagriculture",
                    "mysticalagriculture:infusion_altar",
                    registrar -> MysticalAgricultureGuideRecipes.collect(registrar)),
            new SupportedMod(
                    "mysticalautomation",
                    "mysticalautomation:farmer",
                    registrar -> MysticalAutomationGuideRecipes.collect(registrar)),
            new SupportedMod("nautec", "nautec:mixer", registrar -> NauTecGuideRecipes.collect(registrar)),
            new SupportedMod("neovitae", "neovitae:ara_vitae", registrar -> NeoVitaeGuideRecipes.collect(registrar)),
            new SupportedMod(
                    "occultism", "occultism:spirit_attuned_gem", registrar -> OccultismGuideRecipes.collect(registrar)),
            new SupportedMod(
                    "oritech", "oritech:pulverizer_block", registrar -> OritechGuideRecipes.collect(registrar)),
            new SupportedMod(
                    "paganbless", "paganbless:runic_core", registrar -> PaganBlessGuideRecipes.collect(registrar)),
            new SupportedMod(
                    "pneumaticcraft",
                    "pneumaticcraft:thermopneumatic_processing_plant",
                    registrar -> PneumaticCraftRepressurizedGuideRecipes.collect(registrar)),
            new SupportedMod("powah", "powah:energizing_orb", registrar -> PowahGuideRecipes.collect(registrar)),
            new SupportedMod(
                    "productivebees",
                    "productivebees:centrifuge",
                    registrar -> ProductiveBeesGuideRecipes.collect(registrar)),
            new SupportedMod(
                    "productivetrees",
                    "productivetrees:sawmill",
                    registrar -> ProductiveTreesGuideRecipes.collect(registrar)),
            new SupportedMod("pylons", "pylons:harvester_pylon", registrar -> PylonsGuideRecipes.collect(registrar)),
            new SupportedMod(
                    "reliquary",
                    "reliquary:alkahestry_tome",
                    registrar -> ReliquaryReincarnationsGuideRecipes.collect(registrar)),
            new SupportedMod(
                    "rftoolsutility",
                    "rftoolsutility:spawner",
                    registrar -> RFToolsUtilityGuideRecipes.collect(registrar)),
            new SupportedMod(
                    "rootsclassic", "rootsclassic:altar", registrar -> RootsClassicGuideRecipes.collect(registrar)),
            new SupportedMod("sfm", "sfm:printing_press", registrar -> SfmGuideRecipes.collect(registrar)),
            new SupportedMod("treetap", "treetap:tap", registrar -> TreeTapGuideRecipes.collect(registrar)),
            new SupportedMod(
                    "xycraft_machines",
                    "xycraft_machines:extractor",
                    registrar -> XyCraftMachinesGuideRecipes.collect(registrar)));

    private SupportedMods() {}

    public record SupportedMod(String modId, String iconId, Consumer<MappingRegistrar> guideRecipes) {}
}
