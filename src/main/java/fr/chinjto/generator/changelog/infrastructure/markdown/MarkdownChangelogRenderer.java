package fr.chinjto.generator.changelog.infrastructure.markdown;

import fr.chinjto.generator.changelog.business.changelog.Changelog;
import fr.chinjto.generator.changelog.business.changelog.ChangelogRenderer;
import fr.chinjto.generator.changelog.business.git.GitCommit;

public final class MarkdownChangelogRenderer implements ChangelogRenderer {
    @Override
    public String render(final Changelog changelog) {
        final StringBuilder markdown = new StringBuilder();
        markdown.append("# Changelog").append(System.lineSeparator());
        markdown.append(System.lineSeparator());
        markdown.append("## ")
                .append(changelog.releaseRange().from())
                .append(" to ")
                .append(changelog.releaseRange().to())
                .append(System.lineSeparator());
        markdown.append(System.lineSeparator());

        if (changelog.commits().isEmpty()) {
            markdown.append("- No changes").append(System.lineSeparator());
            return markdown.toString();
        }

        for (final GitCommit commit : changelog.commits()) {
            markdown.append("- ").append(commit.message()).append(System.lineSeparator());
        }
        return markdown.toString();
    }
}
