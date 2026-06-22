package fr.chinjto.generator.changelog.infrastructure.markdown;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@DisplayName("[TU] Markdown changelog writer")
@Tag("infrastructure")
@Tag("unit")
final class MarkdownChangelogWriterTest {
    @TempDir
    private Path tempDir;

    private final MarkdownChangelogWriter writer = new MarkdownChangelogWriter();

    @Test
    @DisplayName("creates parent directories and writes Markdown content")
    void createsParentDirectoriesAndWritesMarkdownContent() throws Exception {
        final Path output = tempDir.resolve("nested").resolve("release-notes.md");

        writer.write(output, "# Changelog\n");

        assertTrue(Files.isRegularFile(output));
        assertEquals("# Changelog\n", Files.readString(output));
    }
}
