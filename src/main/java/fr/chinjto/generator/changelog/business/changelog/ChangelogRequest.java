package fr.chinjto.generator.changelog.business.changelog;

import java.nio.file.Path;

public record ChangelogRequest(Path repository, Path output) {
}
