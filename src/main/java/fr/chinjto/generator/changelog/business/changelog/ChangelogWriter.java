package fr.chinjto.generator.changelog.business.changelog;

import java.nio.file.Path;

public interface ChangelogWriter {
    void write(Path output, String content);
}
