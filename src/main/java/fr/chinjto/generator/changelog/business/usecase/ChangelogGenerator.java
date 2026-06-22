package fr.chinjto.generator.changelog.business.usecase;

import fr.chinjto.generator.changelog.business.changelog.Changelog;
import fr.chinjto.generator.changelog.business.changelog.ChangelogRequest;
import fr.chinjto.generator.changelog.business.changelog.ChangelogRenderer;
import fr.chinjto.generator.changelog.business.changelog.ChangelogWriter;
import fr.chinjto.generator.changelog.business.git.GitHistoryReader;
import fr.chinjto.generator.changelog.business.release.ReleaseRange;

public final class ChangelogGenerator {
    private final GitHistoryReader gitHistoryReader;
    private final ChangelogRenderer changelogRenderer;
    private final ChangelogWriter changelogWriter;

    public ChangelogGenerator(
            final GitHistoryReader gitHistoryReader,
            final ChangelogRenderer changelogRenderer,
            final ChangelogWriter changelogWriter
    ) {
        this.gitHistoryReader = gitHistoryReader;
        this.changelogRenderer = changelogRenderer;
        this.changelogWriter = changelogWriter;
    }

    public void generate(final ChangelogRequest request) {
        final ReleaseRange releaseRange = new ReleaseRange(request.fromRelease(), request.toRelease());
        final Changelog changelog = new Changelog(releaseRange, gitHistoryReader.read(request.repository(), releaseRange));
        changelogWriter.write(request.output(), changelogRenderer.render(changelog));
    }
}
