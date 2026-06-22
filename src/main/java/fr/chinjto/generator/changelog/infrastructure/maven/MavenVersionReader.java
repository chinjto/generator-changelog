package fr.chinjto.generator.changelog.infrastructure.maven;

import fr.chinjto.generator.changelog.business.version.ProjectVersion;
import fr.chinjto.generator.changelog.business.version.ProjectVersionReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;

public final class MavenVersionReader implements ProjectVersionReader {
    @Override
    public ProjectVersion read(final Path repository) {
        final Path pom = repository.resolve("pom.xml");

        if (!Files.isRegularFile(pom)) {
            throw new IllegalArgumentException("No pom.xml found in repository: " + repository);
        }

        try (Reader reader = Files.newBufferedReader(pom)) {
            final Model model = new MavenXpp3Reader().read(reader);

            if (model.getVersion() == null || model.getVersion().isBlank()) {
                throw new IllegalArgumentException("No project version found in: " + pom);
            }

            return new ProjectVersion(model.getVersion());
        } catch (final IOException exception) {
            throw new IllegalArgumentException("Unable to read pom.xml: " + pom, exception);
        } catch (final Exception exception) {
            throw new IllegalStateException("Unable to parse pom.xml: " + pom, exception);
        }
    }
}
