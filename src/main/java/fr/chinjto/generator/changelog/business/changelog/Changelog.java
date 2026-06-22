package fr.chinjto.generator.changelog.business.changelog;

import fr.chinjto.generator.changelog.business.git.GitRelease;

import java.util.List;

public record Changelog(List<GitRelease> releases) {
}
