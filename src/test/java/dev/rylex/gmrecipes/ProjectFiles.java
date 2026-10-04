package dev.rylex.gmrecipes;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

final class ProjectFiles {

    static final Path JAVA = Paths.get("src", "main", "java");
    static final Path RESOURCES = Paths.get("src", "main", "resources");
    static final Path PACKAGE = Paths.get("dev", "rylex", "gmrecipes");

    private static final Path ROOT = locateRoot();

    private ProjectFiles() {}

    static Path root() {
        return ROOT;
    }

    static Path resolve(Path relative) {
        return ROOT.resolve(relative);
    }

    static String read(Path file) {
        try {
            return Files.readString(file.isAbsolute() ? file : ROOT.resolve(file));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static List<Path> walk(Path dir, String suffix) {
        try (Stream<Path> files = Files.walk(ROOT.resolve(dir))) {
            return files.filter(p -> p.getFileName().toString().endsWith(suffix))
                    .sorted()
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static List<String> childDirectories(Path dir) {
        try (Stream<Path> children = Files.list(ROOT.resolve(dir))) {
            return children.filter(Files::isDirectory)
                    .map(p -> p.getFileName().toString())
                    .sorted()
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Path locateRoot() {
        String projectDir = System.getProperty("gmrecipes.projectDir");
        if (projectDir != null && Files.isDirectory(Paths.get(projectDir).resolve(JAVA))) {
            return Paths.get(projectDir).toAbsolutePath().normalize();
        }
        for (Path candidate : List.of(Paths.get(""), Paths.get("gmrecipes"))) {
            if (Files.isDirectory(candidate.resolve(JAVA))) {
                return candidate.toAbsolutePath().normalize();
            }
        }
        throw new IllegalStateException(
                "Could not locate the project root from " + Paths.get("").toAbsolutePath());
    }
}
