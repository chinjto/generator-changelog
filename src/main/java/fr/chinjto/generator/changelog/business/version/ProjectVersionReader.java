package fr.chinjto.generator.changelog.business.version;

import java.nio.file.Path;

public interface ProjectVersionReader {
    ProjectVersion read(Path repository);
}
