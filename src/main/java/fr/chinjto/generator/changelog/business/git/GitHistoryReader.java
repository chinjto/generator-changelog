package fr.chinjto.generator.changelog.business.git;

import fr.chinjto.generator.changelog.business.release.ReleaseRange;
import java.nio.file.Path;
import java.util.List;

public interface GitHistoryReader {
    List<GitCommit> read(Path repository, ReleaseRange releaseRange);
}
