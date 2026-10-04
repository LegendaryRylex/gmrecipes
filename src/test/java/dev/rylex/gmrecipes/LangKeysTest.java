package dev.rylex.gmrecipes;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class LangKeysTest {

    private static final Path LANG =
            ProjectFiles.RESOURCES.resolve(Paths.get("assets", "gmrecipes", "lang", "en_us.json"));

    private static final Pattern LANG_KEY = Pattern.compile("(?m)^\s*\"([^\"]+)\"\s*:");
    private static final Pattern LITERAL = Pattern.compile("\"([^\"\\\\\\n]+)\"");
    private static final Pattern KEY_SHAPED = Pattern.compile("gmrecipes(\\.[A-Za-z0-9_]+)+");

    @Test
    void everyTranslationKeyInTheSourceExists() {
        Set<String> lang = LANG_KEY.matcher(ProjectFiles.read(LANG))
                .results()
                .map(m -> m.group(1))
                .collect(Collectors.toSet());
        List<String> missing = new ArrayList<>();
        Path sourceRoot = ProjectFiles.resolve(ProjectFiles.JAVA);
        for (Path source : ProjectFiles.walk(ProjectFiles.JAVA, ".java")) {
            Matcher literals = LITERAL.matcher(ProjectFiles.read(source));
            while (literals.find()) {
                String candidate = literals.group(1);
                if (!KEY_SHAPED.matcher(candidate).matches()) {
                    continue;
                }
                boolean known =
                        lang.contains(candidate) || lang.stream().anyMatch(key -> key.startsWith(candidate + "."));
                if (!known) {
                    missing.add(candidate + " (" + sourceRoot.relativize(source) + ")");
                }
            }
        }
        assertTrue(
                missing.isEmpty(),
                "these keys would render raw in game because en_us.json does not define them:\n  "
                        + String.join("\n  ", missing));
    }
}
