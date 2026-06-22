package fr.chinjto.generator.changelog.infrastructure.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.chinjto.generator.changelog.business.changelog.ChangelogRequest;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@DisplayName("[TU] Command line arguments parser")
@Tag("cli")
@Tag("infrastructure")
@Tag("unit")
final class CommandLineArgumentsParserTest {
    private final CommandLineArgumentsParser parser = new CommandLineArgumentsParser();

    @Test
    @DisplayName("parses all supported CLI options")
    void parsesAllSupportedCliOptions() {
        final ChangelogRequest request = parser.parse(new String[]{
                "--repo", "/workspace/project",
                "--from", "v1.0.0",
                "--to", "v1.1.0",
                "--output", "build/changelog.md"
        });

        assertEquals(Path.of("/workspace/project"), request.repository());
        assertEquals(Path.of("build/changelog.md"), request.output());
    }

    @Test
    @DisplayName("rejects missing required options with a readable message")
    void rejectsMissingRequiredOptionsWithReadableMessage() {
        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> parser.parse(new String[]{"--output", "changelog.md"})
        );

        assertTrue(exception.getMessage().contains("--repo"));
    }

    @Test
    @DisplayName("rejects non Markdown output files")
    void rejectsNonMarkdownOutputFiles() {
        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> parser.parse(new String[]{
                        "--repo", ".",
                        "--from", "v1.0.0",
                        "--to", "v1.1.0",
                        "--output", "changelog.txt"
                })
        );

        assertTrue(exception.getMessage().contains(".md"));
    }
}
