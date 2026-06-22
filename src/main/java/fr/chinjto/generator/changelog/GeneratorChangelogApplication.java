package fr.chinjto.generator.changelog;

import fr.chinjto.generator.changelog.business.changelog.ChangelogRequest;
import fr.chinjto.generator.changelog.business.usecase.ChangelogGenerator;
import fr.chinjto.generator.changelog.infrastructure.cli.CommandLineArgumentsParser;
import fr.chinjto.generator.changelog.infrastructure.git.GitCliHistoryReader;
import fr.chinjto.generator.changelog.infrastructure.markdown.MarkdownChangelogRenderer;
import fr.chinjto.generator.changelog.infrastructure.markdown.MarkdownChangelogWriter;

public final class GeneratorChangelogApplication {
    private GeneratorChangelogApplication() {
    }

    public static void main(final String[] args) {
        final ChangelogRequest request = new CommandLineArgumentsParser().parse(args);
        new ChangelogGenerator(
                new GitCliHistoryReader(),
                new MarkdownChangelogRenderer(),
                new MarkdownChangelogWriter()
        ).generate(request);
    }
}
