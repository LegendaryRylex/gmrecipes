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
            Map.entry("advanced_ae", List.of("net.pedroksl.advanced_ae")),
            Map.entry("apotheosis", List.of("dev.shadowsoffire.apotheosis")),
            Map.entry("apothic_enchanting", List.of("dev.shadowsoffire.apothic_enchanting")),
            Map.entry("enderio", List.of("com.enderio")),
            Map.entry("extendedae", List.of("com.glodblock.github")),
            Map.entry("integrateddynamics", List.of("org.cyclops")),
            Map.entry("justdirethings", List.of("com.direwolf20.justdirethings")),
            Map.entry("morered", List.of("commoble.morered")),
            Map.entry("mysticalagriculture", List.of("com.blakebr0.mysticalagriculture", "com.blakebr0.cucumber")),
            Map.entry("mysticalautomation", List.of("com.blakebr0.mysticalautomation")),
            Map.entry("neovitae", List.of("com.breakinblocks.neovitae")),
            Map.entry("occultism", List.of("com.klikli_dev.occultism")),
            Map.entry("oritech", List.of("rearth.oritech")),
            Map.entry("powah", List.of("owmii.powah")),
            Map.entry("productivebees", List.of("cy.jdkdigital.productivebees")),
            Map.entry("pylons", List.of("net.permutated.pylons")),
            Map.entry("reliquary", List.of("import reliquary.")),
            Map.entry("rootsclassic", List.of("elucent.rootsclassic")),
            Map.entry("sfm", List.of("ca.teamdman.sfm")),
            Map.entry("xycraft_machines", List.of("tv.soaryn")));

    private static final List<String> EXTRA_ENTRY_POINTS = List.of();

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
