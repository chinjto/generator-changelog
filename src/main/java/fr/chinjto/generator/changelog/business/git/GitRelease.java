package fr.chinjto.generator.changelog.business.git;

import java.util.List;

public record GitRelease(String version, List<GitCommit> commits) {
}
