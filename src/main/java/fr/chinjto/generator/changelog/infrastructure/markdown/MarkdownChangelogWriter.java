package fr.chinjto.generator.changelog.infrastructure.markdown;

import fr.chinjto.generator.changelog.business.changelog.ChangelogWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class MarkdownChangelogWriter implements ChangelogWriter {
    @Override
    public void write(final Path output, final String markdown) {
        try {
            final Path parent = output.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(output, markdown);
        } catch (final IOException exception) {
            throw new IllegalStateException("Unable to write changelog to %s.".formatted(output), exception);
        }
    }
}
