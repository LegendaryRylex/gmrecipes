package dev.rylex.gmrecipes;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;

class CompatIsolationTest {

    private static final Path COMPAT_DIR = ProjectFiles.PACKAGE.resolve("compat");
    private static final Path SUPPORTED_MODS =
            ProjectFiles.PACKAGE.resolve("guide").resolve("SupportedMods.java");

    private static final Map<String, List<String>> FOREIGN_PACKAGES = Map.ofEntries(
            Map.entry("actuallyadditions", List.of("de.ellpeck.actuallyadditions")),
            Map.entry("advanced_ae", List.of("net.pedroksl.advanced_ae")),
            Map.entry("apotheosis", List.of("dev.shadowsoffire.apotheosis")),
            Map.entry("apothic_enchanting", List.of("dev.shadowsoffire.apothic_enchanting")),
            Map.entry("ars_nouveau", List.of("com.hollingsworth.arsnouveau")),
            Map.entry("astralsorcery", List.of("hellfirepvp.astralsorcery")),
            Map.entry("cognition", List.of("com.cyanogen.cognition")),
            Map.entry("create", List.of("com.simibubi.create", "net.createmod", "com.tterrag.registrate")),
            Map.entry("createaddition", List.of("com.mrh0.createaddition")),
            Map.entry("createbigcannons", List.of("rbasamoyai.createbigcannons")),
            Map.entry("draconicevolution", List.of("com.brandon3055.draconicevolution")),
            Map.entry("enderio", List.of("com.enderio")),
            Map.entry("eternal_starlight", List.of("cn.leolezury.eternalstarlight")),
            Map.entry("extended_industrialization", List.of("net.swedz.extended_industrialization")),
            Map.entry("extendedae", List.of("com.glodblock.github")),
            Map.entry("farmersdelight", List.of("vectorwing.farmersdelight")),
            Map.entry("forbidden_arcanus", List.of("com.stal111.forbidden_arcanus")),
            Map.entry("geneticsresequenced", List.of("dev.aaronhowser.mods.geneticsresequenced")),
            Map.entry("hexerei", List.of("net.joefoxe.hexerei")),
            Map.entry("immersiveengineering", List.of("blusunrize.immersiveengineering")),
            Map.entry("industrialforegoing", List.of("com.buuz135.industrial")),
            Map.entry("integrateddynamics", List.of("org.cyclops")),
            Map.entry("irons_spellbooks", List.of("io.redspace.ironsspellbooks")),
            Map.entry("justdirethings", List.of("com.direwolf20.justdirethings")),
            Map.entry("malum", List.of("com.sammy.malum")),
            Map.entry("mekanism", List.of("mekanism.api", "mekanism.common", "mekanism.client")),
            Map.entry("mob_grinding_utils", List.of("import mob_grinding_utils.", "import static mob_grinding_utils.")),
            Map.entry("modern_industrialization", List.of("aztech.modern_industrialization")),
            Map.entry("morered", List.of("commoble.morered")),
            Map.entry("mysticalagriculture", List.of("com.blakebr0.mysticalagriculture", "com.blakebr0.cucumber")),
            Map.entry("mysticalautomation", List.of("com.blakebr0.mysticalautomation")),
            Map.entry("nautec", List.of("com.portingdeadmods.nautec")),
            Map.entry("neovitae", List.of("com.breakinblocks.neovitae")),
            Map.entry("occultism", List.of("com.klikli_dev.occultism")),
            Map.entry("oritech", List.of("rearth.oritech")),
            Map.entry("paganbless", List.of("com.pigdad.paganbless")),
            Map.entry("pneumaticcraft", List.of("me.desht.pneumaticcraft")),
            Map.entry("powah", List.of("owmii.powah")),
            Map.entry("productivebees", List.of("cy.jdkdigital.productivebees")),
            Map.entry("productivetrees", List.of("cy.jdkdigital.productivetrees")),
            Map.entry("pylons", List.of("net.permutated.pylons")),
            Map.entry("reliquary", List.of("import reliquary.")),
            Map.entry("rftoolsutility", List.of("mcjty.rftoolsutility")),
            Map.entry("rootsclassic", List.of("elucent.rootsclassic")),
            Map.entry("sfm", List.of("ca.teamdman.sfm")),
            Map.entry("treetap", List.of("cy.jdkdigital.treetap")),
            Map.entry("xycraft_machines", List.of("tv.soaryn.xycraft")));

    private static final List<String> EXTRA_ENTRY_POINTS = List.of(
            "dev.rylex.gmrecipes.compat.create.CreatePlaceholderRecipes",
            "dev.rylex.gmrecipes.compat.mekanism.MekanismChemicals");

    @Test
    void everyCompatPackageDeclaresItsForeignPackages() {
        TreeSet<String> packages = new TreeSet<>(ProjectFiles.childDirectories(ProjectFiles.JAVA.resolve(COMPAT_DIR)));
        TreeSet<String> undeclared = new TreeSet<>(packages);
        undeclared.removeAll(FOREIGN_PACKAGES.keySet());
        TreeSet<String> stale = new TreeSet<>(FOREIGN_PACKAGES.keySet());
        stale.removeAll(packages);
        assertTrue(
                undeclared.isEmpty() && stale.isEmpty(),
                "FOREIGN_PACKAGES must name exactly the compat packages; without an entry nothing stops that mod's "
                        + "classes leaking into core code. Missing: " + undeclared + ", no such package: " + stale);
    }

    @Test
    void foreignClassesStayInsideTheirCompatPackage() {
        List<String> violations = new ArrayList<>();
        forEachSourceFile((relative, source) -> {
            FOREIGN_PACKAGES.forEach((compatPackage, prefixes) -> {
                if (relative.startsWith(COMPAT_DIR.resolve(compatPackage))) {
                    return;
                }
                for (String prefix : prefixes) {
                    if (source.contains(prefix)) {
                        violations.add(relative + " references '" + prefix + "'");
                    }
                }
            });
        });
        assertTrue(
                violations.isEmpty(),
                "Foreign mod classes leaked outside their compat package (breaks lazy loading; crashes when the "
                        + "integration is absent):\n  " + String.join("\n  ", violations));
    }

    @Test
    void coreReachesCompatOnlyThroughGuardedEntryPoints() {
        List<String> violations = new ArrayList<>();
        forEachSourceFile((relative, source) -> {
            if (relative.startsWith(COMPAT_DIR)) {
                return;
            }
            boolean table = relative.equals(SUPPORTED_MODS);
            compatImports(source).stream()
                    .filter(fqn -> table ? !fqn.endsWith("GuideRecipes") : !EXTRA_ENTRY_POINTS.contains(fqn))
                    .forEach(fqn -> violations.add(relative + " imports '" + fqn + "'"));
        });
        assertTrue(
                violations.isEmpty(),
                "Core code names a compat class outside the SupportedMods table, which only runs a mod's "
                        + "GuideRecipes collector once that mod is loaded. Anything else goes in EXTRA_ENTRY_POINTS, "
                        + "and only if it is guarded by ModList.isLoaded:\n  " + String.join("\n  ", violations));
    }

    private static List<String> compatImports(String source) {
        return source.lines()
                .map(String::trim)
                .filter(line -> line.startsWith("import dev.rylex.gmrecipes.compat."))
                .map(line ->
                        line.substring("import ".length(), line.indexOf(';')).trim())
                .toList();
    }

    private interface SourceVisitor {
        void visit(Path relative, String source);
    }

    private static void forEachSourceFile(SourceVisitor visitor) {
        Path sourceRoot = ProjectFiles.resolve(ProjectFiles.JAVA);
        for (Path file : ProjectFiles.walk(ProjectFiles.JAVA, ".java")) {
            visitor.visit(sourceRoot.relativize(file), ProjectFiles.read(file));
        }
    }
}
