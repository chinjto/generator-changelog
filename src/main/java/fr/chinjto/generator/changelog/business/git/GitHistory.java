package fr.chinjto.generator.changelog.business.git;

import java.util.List;

public record GitHistory(List<GitRelease> releases, List<GitCommit> currentCommits) {
}
