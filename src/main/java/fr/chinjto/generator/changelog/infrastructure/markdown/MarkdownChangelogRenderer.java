package fr.chinjto.generator.changelog.infrastructure.markdown;

import fr.chinjto.generator.changelog.business.changelog.Changelog;
import fr.chinjto.generator.changelog.business.changelog.ChangelogRenderer;
import fr.chinjto.generator.changelog.business.git.GitCommit;
import fr.chinjto.generator.changelog.business.git.GitRelease;

public final class MarkdownChangelogRenderer implements ChangelogRenderer {
    @Override
    public String render(final Changelog changelog) {
        final StringBuilder markdown = new StringBuilder();

        markdown.append("# Changelog")
                .append(System.lineSeparator())
                .append(System.lineSeparator());

        for (final GitRelease release : changelog.releases()) {
            renderRelease(markdown, release);
        }

        return markdown.toString();
    }

    private static void renderRelease(final StringBuilder markdown, final GitRelease release) {
        markdown.append("## ")
                .append(release.version())
                .append(System.lineSeparator())
                .append(System.lineSeparator());

        if (release.commits().isEmpty()) {
            markdown.append("- No changes")
                    .append(System.lineSeparator())
                    .append(System.lineSeparator());
            return;
        }

        for (final GitCommit commit : release.commits()) {
            markdown.append("- ")
                    .append(commit.message())
                    .append(System.lineSeparator());
        }

        markdown.append(System.lineSeparator());
    }
}
