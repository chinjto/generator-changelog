package fr.chinjto.generator.changelog;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayName("[TDD] Generator changelog CLI")
@Tag("cli")
@Tag("contract")
final class GeneratorChangelogApplicationTest {
    @TempDir
    private Path tempDir;

    @Test
    @DisplayName("generates a complete Markdown changelog from all Git releases and current snapshot version")
    void generatesCompleteMarkdownChangelogFromAllGitReleasesAndCurrentSnapshotVersion() throws Exception {
        final Path repository = createRepositoryWithReleaseHistory();
        final Path output = tempDir.resolve("release-notes").resolve("CHANGELOG.md");

        GeneratorChangelogApplication.main(new String[]{
                "--repo", repository.toString(),
                "--output", output.toString()
        });

        assertTrue(Files.isRegularFile(output), "Expected a Markdown file to be generated at: " + output);

        final String markdown = Files.readString(output);

        assertTrue(markdown.contains("# Changelog"), "Expected the output file to start a Markdown changelog.");

        assertTrue(markdown.contains("## v1.2.0"), "Expected current Maven snapshot version to be used as next release section.");
        assertTrue(markdown.contains("feat: start next release"), "Expected commits after the latest tag to appear in current release section.");

        assertTrue(markdown.contains("## v1.1.0"), "Expected v1.1.0 release section.");
        assertTrue(markdown.contains("feat: add markdown changelog output"), "Expected v1.1.0 feature commit.");
        assertTrue(markdown.contains("fix: keep release range configurable"), "Expected v1.1.0 fix commit.");

        assertTrue(markdown.contains("## v1.0.0"), "Expected v1.0.0 release section.");
        assertTrue(markdown.contains("chore: bootstrap project"), "Expected initial v1.0.0 commit.");

        assertFalse(markdown.contains("## Unreleased"), "Expected no Unreleased section.");
        assertFalse(markdown.contains("SNAPSHOT"), "Expected snapshot suffix to be removed from release title.");
    }

    @Test
    @DisplayName("orders changelog sections from current snapshot version to oldest release")
    void ordersChangelogSectionsFromCurrentSnapshotVersionToOldestRelease() throws Exception {
        final Path repository = createRepositoryWithReleaseHistory();
        final Path output = tempDir.resolve("CHANGELOG.md");

        GeneratorChangelogApplication.main(new String[]{
                "--repo", repository.toString(),
                "--output", output.toString()
        });

        final String markdown = Files.readString(output);

        assertTrue(
                markdown.indexOf("## v1.2.0") < markdown.indexOf("## v1.1.0"),
                "Expected current snapshot version to appear before latest tagged release."
        );

        assertTrue(
                markdown.indexOf("## v1.1.0") < markdown.indexOf("## v1.0.0"),
                "Expected newest tagged release to appear before oldest tagged release."
        );
    }

    @Test
    @DisplayName("refreshes the whole output file instead of appending to it")
    void refreshesWholeOutputFileInsteadOfAppendingToIt() throws Exception {
        final Path repository = createRepositoryWithReleaseHistory();
        final Path output = tempDir.resolve("CHANGELOG.md");

        Files.writeString(output, "# Old changelog\n\n## obsolete\n\n- stale entry\n", StandardCharsets.UTF_8);

        GeneratorChangelogApplication.main(new String[]{
                "--repo", repository.toString(),
                "--output", output.toString()
        });

        final String markdown = Files.readString(output);

        assertFalse(markdown.contains("obsolete"), "Expected previous output content to be replaced.");
        assertFalse(markdown.contains("stale entry"), "Expected previous output content to be replaced.");
        assertTrue(markdown.contains("## v1.2.0"), "Expected regenerated changelog content.");
        assertTrue(markdown.contains("## v1.1.0"), "Expected regenerated changelog content.");
        assertTrue(markdown.contains("## v1.0.0"), "Expected regenerated changelog content.");
    }

    @ParameterizedTest(name = "rejects command without {0}")
    @MethodSource("missingRequiredOptions")
    @DisplayName("rejects commands with missing required options")
    void rejectsCommandsWithMissingRequiredOption(final String missingOption, final String[] arguments) {
        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> GeneratorChangelogApplication.main(arguments)
        );

        assertTrue(
                exception.getMessage().contains(missingOption),
                "Expected the error message to mention the missing option " + missingOption
        );
    }

    @Test
    @DisplayName("rejects output paths that are not Markdown files")
    void rejectsNonMarkdownOutputPath() throws Exception {
        final Path repository = createRepositoryWithReleaseHistory();
        final Path output = tempDir.resolve("changelog.txt");

        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> GeneratorChangelogApplication.main(new String[]{
                        "--repo", repository.toString(),
                        "--output", output.toString()
                })
        );

        assertTrue(
                exception.getMessage().contains(".md"),
                "Expected the error message to explain that --output must target a .md file."
        );
    }

    private static Stream<Arguments> missingRequiredOptions() {
        return Stream.of(
                Arguments.of("--repo", new String[]{"--output", "CHANGELOG.md"}),
                Arguments.of("--output", new String[]{"--repo", "."})
        );
    }

    private Path createRepositoryWithReleaseHistory() throws Exception {
        final Path repository = tempDir.resolve("source-repository");
        Files.createDirectories(repository);

        git(repository, "init", "-b", "main");
        git(repository, "config", "user.name", "Test User");
        git(repository, "config", "user.email", "test@example.com");

        writePom(repository, "1.2.0-SNAPSHOT");
        commit(repository, "README.md", "# Fixture\n", "chore: bootstrap project");
        git(repository, "tag", "v1.0.0");

        commit(repository, "feature.md", "markdown changelog\n", "feat: add markdown changelog output");
        commit(repository, "range.md", "configurable releases\n", "fix: keep release range configurable");
        git(repository, "tag", "v1.1.0");

        commit(repository, "next.md", "next release\n", "feat: start next release");

        return repository;
    }

    private static void writePom(final Path repository, final String version) throws IOException {
        Files.writeString(repository.resolve("pom.xml"), """
                <project>
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>fr.chinjto</groupId>
                    <artifactId>fixture</artifactId>
                    <version>%s</version>
                </project>
                """.formatted(version), StandardCharsets.UTF_8);
    }

    private static void commit(
            final Path repository,
            final String fileName,
            final String content,
            final String message
    ) throws Exception {
        Files.writeString(repository.resolve(fileName), content, StandardCharsets.UTF_8);
        git(repository, "add", fileName);
        git(repository, "commit", "-m", message);
    }

    private static void git(final Path repository, final String... arguments) throws Exception {
        final String[] command = new String[arguments.length + 1];
        command[0] = "git";
        System.arraycopy(arguments, 0, command, 1, arguments.length);

        final Process process = new ProcessBuilder(command)
                .directory(repository.toFile())
                .redirectErrorStream(true)
                .start();

        final String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        final int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("Git command failed: %s%n%s".formatted(String.join(" ", command), output));
        }
    }
}
