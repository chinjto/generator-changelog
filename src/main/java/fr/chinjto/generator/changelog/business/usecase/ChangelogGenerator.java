package fr.chinjto.generator.changelog.business.usecase;

import fr.chinjto.generator.changelog.business.changelog.Changelog;
import fr.chinjto.generator.changelog.business.changelog.ChangelogRequest;
import fr.chinjto.generator.changelog.business.changelog.ChangelogRenderer;
import fr.chinjto.generator.changelog.business.changelog.ChangelogWriter;
import fr.chinjto.generator.changelog.business.git.GitHistory;
import fr.chinjto.generator.changelog.business.git.GitHistoryReader;
import fr.chinjto.generator.changelog.business.git.GitRelease;
import fr.chinjto.generator.changelog.business.version.ProjectVersion;
import fr.chinjto.generator.changelog.business.version.ProjectVersionReader;

import java.util.stream.Stream;

public final class ChangelogGenerator {
    private final GitHistoryReader gitHistoryReader;
    private final ProjectVersionReader projectVersionReader;
    private final ChangelogRenderer changelogRenderer;
    private final ChangelogWriter changelogWriter;

    public ChangelogGenerator(
            final GitHistoryReader gitHistoryReader,
            final ProjectVersionReader projectVersionReader,
            final ChangelogRenderer changelogRenderer,
            final ChangelogWriter changelogWriter
    ) {
        this.gitHistoryReader = gitHistoryReader;
        this.projectVersionReader = projectVersionReader;
        this.changelogRenderer = changelogRenderer;
        this.changelogWriter = changelogWriter;
    }

    public void generate(final ChangelogRequest request) {
        // Détecte la version courante du projet
        final ProjectVersion currentVersion = projectVersionReader.read(request.repository());

        // Récupère l'ensemble de l'historique Git
        final GitHistory gitHistory = gitHistoryReader.read(request.repository());

        // Génère les informations de la future release
        final GitRelease currentRelease = new GitRelease(currentVersion.asReleaseVersion(), gitHistory.currentCommits());

        // Génère tout le contenu du nouveau changelog
        final Changelog changelog = new Changelog(
                Stream.concat(Stream.of(currentRelease), gitHistory.releases().stream()).toList()
        );

        // Génère le rendu du changelog
        final String render = changelogRenderer.render(changelog);

        // Persiste le changelog dans le fichier
        changelogWriter.write(request.output(), render);
    }
}
