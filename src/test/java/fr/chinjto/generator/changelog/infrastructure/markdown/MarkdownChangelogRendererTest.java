package fr.chinjto.generator.changelog.infrastructure.markdown;

import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.chinjto.generator.changelog.business.changelog.Changelog;
import fr.chinjto.generator.changelog.business.git.GitCommit;
import fr.chinjto.generator.changelog.business.release.ReleaseRange;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@DisplayName("[TU] Markdown changelog renderer")
@Tag("infrastructure")
@Tag("unit")
final class MarkdownChangelogRendererTest {
    private final MarkdownChangelogRenderer renderer = new MarkdownChangelogRenderer();

    @Test
    @DisplayName("renders release boundaries and commits")
    void rendersReleaseBoundariesAndCommits() {
        final String markdown = renderer.render(new Changelog(
                new ReleaseRange("v1.0.0", "v1.1.0"),
                List.of(
                        new GitCommit("feat: add markdown output"),
                        new GitCommit("fix: keep range configurable")
                )
        ));

        assertTrue(markdown.contains("# Changelog"));
        assertTrue(markdown.contains("v1.0.0 to v1.1.0"));
        assertTrue(markdown.contains("- feat: add markdown output"));
        assertTrue(markdown.contains("- fix: keep range configurable"));
    }

    @Test
    @DisplayName("renders an explicit empty state")
    void rendersExplicitEmptyState() {
        final String markdown = renderer.render(new Changelog(new ReleaseRange("v1.0.0", "v1.1.0"), List.of()));

        assertTrue(markdown.contains("- No changes"));
    }
}
