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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayName("Generator changelog CLI")
final class GeneratorChangelogApplicationTest {
    @TempDir
    private Path tempDir;

    @Test
    @DisplayName("generates a Markdown changelog from a custom Git repository between two custom releases")
    void generatesMarkdownChangelogFromCustomGitRepositoryAndCustomReleaseRange() throws Exception {
        final Path repository = createRepositoryWithReleaseHistory();
        final Path output = tempDir.resolve("release-notes").resolve("generated-changelog.md");

        GeneratorChangelogApplication.main(new String[]{
                "--repo", repository.toString(),
                "--from", "v1.0.0",
                "--to", "v1.1.0",
                "--output", output.toString()
        });

        assertTrue(
                Files.isRegularFile(output),
                "Expected a Markdown file to be generated at the exact --output path: " + output
        );

        final String markdown = Files.readString(output);
        assertTrue(markdown.contains("# Changelog"), "Expected the output file to start a Markdown changelog.");
        assertTrue(markdown.contains("v1.0.0"), "Expected the lower release boundary v1.0.0 to appear in the changelog.");
        assertTrue(markdown.contains("v1.1.0"), "Expected the upper release boundary v1.1.0 to appear in the changelog.");
        assertTrue(markdown.contains("feat: add markdown changelog output"), "Expected the feature commit to be listed.");
        assertTrue(markdown.contains("fix: keep release range configurable"), "Expected the fix commit to be listed.");
    }

    @Test
    @DisplayName("keeps only commits included in the requested release range")
    void onlyIncludesCommitsBetweenRequestedReleases() throws Exception {
        final Path repository = createRepositoryWithReleaseHistory();
        final Path output = tempDir.resolve("changelog.md");

        GeneratorChangelogApplication.main(new String[]{
                "--repo", repository.toString(),
                "--from", "v1.0.0",
                "--to", "v1.1.0",
                "--output", output.toString()
        });

        assertTrue(
                Files.isRegularFile(output),
                "Expected the changelog to be generated before checking included and excluded commits."
        );

        final String markdown = Files.readString(output);
        assertFalse(markdown.contains("chore: bootstrap project"), "Expected commits before --from to be excluded.");
        assertFalse(markdown.contains("feat: start next release"), "Expected commits after --to to be excluded.");
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
                        "--from", "v1.0.0",
                        "--to", "v1.1.0",
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
                Arguments.of("--repo", new String[]{"--from", "v1.0.0", "--to", "v1.1.0", "--output", "changelog.md"}),
                Arguments.of("--from", new String[]{"--repo", ".", "--to", "v1.1.0", "--output", "changelog.md"}),
                Arguments.of("--to", new String[]{"--repo", ".", "--from", "v1.0.0", "--output", "changelog.md"}),
                Arguments.of("--output", new String[]{"--repo", ".", "--from", "v1.0.0", "--to", "v1.1.0"})
        );
    }

    private Path createRepositoryWithReleaseHistory() throws Exception {
        final Path repository = tempDir.resolve("source-repository");
        Files.createDirectories(repository);

        git(repository, "init", "-b", "main");
        git(repository, "config", "user.name", "Test User");
        git(repository, "config", "user.email", "test@example.com");

        commit(repository, "README.md", "# Fixture\n", "chore: bootstrap project");
        git(repository, "tag", "v1.0.0");

        commit(repository, "feature.md", "markdown changelog\n", "feat: add markdown changelog output");
        commit(repository, "range.md", "configurable releases\n", "fix: keep release range configurable");
        git(repository, "tag", "v1.1.0");

        commit(repository, "next.md", "next release\n", "feat: start next release");

        return repository;
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
