package fr.chinjto.generator.changelog.business.git;

import java.nio.file.Path;

public interface GitHistoryReader {
    GitHistory read(Path repository);
}
