package fr.chinjto.generator.changelog.business.changelog;

import java.nio.file.Path;

public record ChangelogRequest(Path repository, String fromRelease, String toRelease, Path output) {
}
