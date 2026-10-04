package dev.rylex.gmrecipes;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class SupportedModsTest {

    private static final Path TABLE =
            ProjectFiles.JAVA.resolve(ProjectFiles.PACKAGE).resolve("guide").resolve("SupportedMods.java");
    private static final Path EXAMPLES =
            ProjectFiles.RESOURCES.resolve(Paths.get("assets", "gmrecipes", "guides", "gmrecipes", "examples"));
    private static final Path FILTERS = ProjectFiles.RESOURCES.resolve("guide_filters");
    private static final Set<String> EXAMPLE_PAGES_WITHOUT_A_MOD = Set.of("index", "minecraft");

    private static final Pattern ENTRY = Pattern.compile("new SupportedMod\\(\\s*\"([a-z0-9_]+)\"");

    private static List<String> tableModIds() {
        List<String> ids = new ArrayList<>();
        Matcher entries = ENTRY.matcher(ProjectFiles.read(TABLE));
        while (entries.find()) {
            ids.add(entries.group(1));
        }
        return ids;
    }

    @Test
    void tableIsSortedAndNotEmpty() {
        List<String> ids = tableModIds();
        assertFalse(ids.isEmpty(), "no SupportedMod entries parsed from " + TABLE);
        assertTrue(ids.equals(ids.stream().sorted().distinct().toList()), "SupportedMods.ALL must be sorted: " + ids);
    }

    @Test
    void everyCompatPackageIsInTheTable() {
        TreeSet<String> missing = new TreeSet<>(
                ProjectFiles.childDirectories(ProjectFiles.JAVA.resolve(ProjectFiles.PACKAGE.resolve("compat"))));
        missing.removeAll(tableModIds());
        assertTrue(missing.isEmpty(), "compat packages whose collector never runs: " + missing);
    }

    @Test
    void everySupportedModHasAnExamplePageAndAFilterPack() {
        List<String> problems = new ArrayList<>();
        for (String modId : tableModIds()) {
            Path page = ProjectFiles.resolve(EXAMPLES.resolve(modId + ".md"));
            if (!Files.isRegularFile(page)) {
                problems.add(modId + ": no examples/" + modId + ".md");
            } else if (!ProjectFiles.read(page).contains("mod=\"" + modId + "\"")) {
                problems.add(modId + ": examples/" + modId + ".md does not list mod=\"" + modId + "\"");
            }
            Path filter = ProjectFiles.resolve(FILTERS.resolve("no_" + modId).resolve("pack.mcmeta"));
            if (!Files.isRegularFile(filter)) {
                problems.add(modId + ": no guide_filters/no_" + modId + "/pack.mcmeta");
            } else if (!ProjectFiles.read(filter).contains("examples/" + modId + "\\\\.md$")) {
                problems.add(modId + ": guide_filters/no_" + modId + " does not block examples/" + modId + ".md");
            }
        }
        assertTrue(problems.isEmpty(), "incomplete supported mods:\n  " + String.join("\n  ", problems));
    }

    @Test
    void everyExamplePageAndFilterPackBelongsToASupportedMod() {
        List<String> ids = tableModIds();
        List<String> orphans = new ArrayList<>();
        for (Path page : ProjectFiles.walk(EXAMPLES, ".md")) {
            String name = page.getFileName().toString().replaceFirst("\\.md$", "");
            if (!EXAMPLE_PAGES_WITHOUT_A_MOD.contains(name) && !ids.contains(name)) {
                orphans.add("examples/" + name + ".md");
            }
        }
        for (String filter : ProjectFiles.childDirectories(FILTERS)) {
            if (!filter.startsWith("no_") || !ids.contains(filter.substring("no_".length()))) {
                orphans.add("guide_filters/" + filter);
            }
        }
        assertTrue(orphans.isEmpty(), "not tied to any SupportedMods entry:\n  " + String.join("\n  ", orphans));
    }
}
