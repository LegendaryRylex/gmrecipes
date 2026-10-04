package dev.rylex.gmrecipes.guide;

import dev.rylex.gmrecipes.compat.advanced_ae.AdvancedAEGuideRecipes;
import dev.rylex.gmrecipes.compat.apotheosis.ApotheosisGuideRecipes;
import dev.rylex.gmrecipes.compat.apothic_enchanting.ApothicEnchantingGuideRecipes;
import dev.rylex.gmrecipes.compat.enderio.EnderIOGuideRecipes;
import dev.rylex.gmrecipes.compat.extendedae.ExtendedAEGuideRecipes;
import dev.rylex.gmrecipes.compat.integrateddynamics.IntegratedDynamicsGuideRecipes;
import dev.rylex.gmrecipes.compat.justdirethings.JustDireThingsGuideRecipes;
import dev.rylex.gmrecipes.compat.morered.MoreRedGuideRecipes;
import dev.rylex.gmrecipes.compat.mysticalagriculture.MysticalAgricultureGuideRecipes;
import dev.rylex.gmrecipes.compat.mysticalautomation.MysticalAutomationGuideRecipes;
import dev.rylex.gmrecipes.compat.neovitae.NeoVitaeGuideRecipes;
import dev.rylex.gmrecipes.compat.occultism.OccultismGuideRecipes;
import dev.rylex.gmrecipes.compat.oritech.OritechGuideRecipes;
import dev.rylex.gmrecipes.compat.powah.PowahGuideRecipes;
import dev.rylex.gmrecipes.compat.productivebees.ProductiveBeesGuideRecipes;
import dev.rylex.gmrecipes.compat.pylons.PylonsGuideRecipes;
import dev.rylex.gmrecipes.compat.reliquary.ReliquaryReincarnationsGuideRecipes;
import dev.rylex.gmrecipes.compat.rootsclassic.RootsClassicGuideRecipes;
import dev.rylex.gmrecipes.compat.sfm.SfmGuideRecipes;
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
                    "advanced_ae",
                    "advanced_ae:reaction_chamber",
                    registrar -> AdvancedAEGuideRecipes.collect(registrar)),
            new SupportedMod(
                    "apotheosis", "apotheosis:salvaging_table", registrar -> ApotheosisGuideRecipes.collect(registrar)),
            new SupportedMod(
                    "apothic_enchanting",
                    "apothic_enchanting:library",
                    registrar -> ApothicEnchantingGuideRecipes.collect(registrar)),
            new SupportedMod("enderio", "enderio:sag_mill", registrar -> EnderIOGuideRecipes.collect(registrar)),
            new SupportedMod(
                    "extendedae",
                    "extendedae:crystal_assembler",
                    registrar -> ExtendedAEGuideRecipes.collect(registrar)),
            new SupportedMod(
                    "integrateddynamics",
                    "integrateddynamics:mechanical_squeezer",
                    registrar -> IntegratedDynamicsGuideRecipes.collect(registrar)),
            new SupportedMod(
                    "justdirethings",
                    "justdirethings:gooblock_tier1",
                    registrar -> JustDireThingsGuideRecipes.collect(registrar)),
            new SupportedMod("morered", "morered:soldering_table", registrar -> MoreRedGuideRecipes.collect(registrar)),
            new SupportedMod(
                    "mysticalagriculture",
                    "mysticalagriculture:infusion_altar",
                    registrar -> MysticalAgricultureGuideRecipes.collect(registrar)),
            new SupportedMod(
                    "mysticalautomation",
                    "mysticalautomation:farmer",
                    registrar -> MysticalAutomationGuideRecipes.collect(registrar)),
            new SupportedMod("neovitae", "neovitae:ara_vitae", registrar -> NeoVitaeGuideRecipes.collect(registrar)),
            new SupportedMod(
                    "occultism", "occultism:spirit_attuned_gem", registrar -> OccultismGuideRecipes.collect(registrar)),
            new SupportedMod("oritech", "oritech:pulverizer", registrar -> OritechGuideRecipes.collect(registrar)),
            new SupportedMod("powah", "powah:energizing_orb", registrar -> PowahGuideRecipes.collect(registrar)),
            new SupportedMod(
                    "productivebees",
                    "productivebees:centrifuge",
                    registrar -> ProductiveBeesGuideRecipes.collect(registrar)),
            new SupportedMod("pylons", "pylons:harvester_pylon", registrar -> PylonsGuideRecipes.collect(registrar)),
            new SupportedMod(
                    "reliquary",
                    "reliquary:alkahestry_tome",
                    registrar -> ReliquaryReincarnationsGuideRecipes.collect(registrar)),
            new SupportedMod(
                    "rootsclassic", "rootsclassic:altar", registrar -> RootsClassicGuideRecipes.collect(registrar)),
            new SupportedMod("sfm", "sfm:printing_press", registrar -> SfmGuideRecipes.collect(registrar)),
            new SupportedMod(
                    "xycraft_machines",
                    "xycraft_machines:extractor",
                    registrar -> XyCraftMachinesGuideRecipes.collect(registrar)));

    private SupportedMods() {}

    public record SupportedMod(String modId, String iconId, Consumer<MappingRegistrar> guideRecipes) {}
}
