package fr.chinjto.generator.changelog.business.changelog;

import fr.chinjto.generator.changelog.business.git.GitCommit;
import fr.chinjto.generator.changelog.business.release.ReleaseRange;
import java.util.List;

public record Changelog(ReleaseRange releaseRange, List<GitCommit> commits) {
    public Changelog {
        commits = List.copyOf(commits);
    }
}
