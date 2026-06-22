package fr.chinjto.generator.changelog.infrastructure.git;

import fr.chinjto.generator.changelog.business.git.GitCommit;
import fr.chinjto.generator.changelog.business.git.GitHistory;
import fr.chinjto.generator.changelog.business.git.GitHistoryReader;
import fr.chinjto.generator.changelog.business.git.GitRelease;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public final class GitCliHistoryReader implements GitHistoryReader {
    @Override
    public GitHistory read(final Path repository) {
        final List<String> tags = readTags(repository);
        final List<GitRelease> releases = tags.stream()
                .sorted(Comparator.reverseOrder())
                .map(tag -> new GitRelease(tag, readCommitsForTag(repository, tags, tag)))
                .toList();
        final List<GitCommit> currentCommits = readCommitsWithoutTag(repository);
        return new GitHistory(releases, currentCommits);
    }

    private static List<String> readTags(final Path repository) {
        final String output = runGit(repository, "tag", "--sort=v:refname");
        if (output.isBlank()) {
            return List.of();
        }
        return Arrays.stream(output.split("\\R"))
                .filter(tag -> !tag.isBlank())
                .toList();
    }

    private static List<GitCommit> readCommitsForTag(
            final Path repository,
            final List<String> orderedTags,
            final String tag
    ) {
        final int tagIndex = orderedTags.indexOf(tag);
        final String revisionRange = tagIndex == 0
                ? tag
                : "%s..%s".formatted(orderedTags.get(tagIndex - 1), tag);
        final String output = runGit(repository, "log", "--reverse", "--pretty=format:%s", revisionRange);
        if (output.isBlank()) {
            return List.of();
        }
        return Arrays.stream(output.split("\\R"))
                .filter(message -> !message.isBlank())
                .map(GitCommit::new)
                .toList();
    }

    private static List<GitCommit> readCommitsWithoutTag(final Path repository) {
        final List<String> tags = readTags(repository);

        final String output = tags.isEmpty()
                ? runGit(repository, "log", "--reverse", "--pretty=format:%s")
                : runGit(repository, "log", "--reverse", "--pretty=format:%s", "%s..HEAD".formatted(tags.getLast()));

        if (output.isBlank()) {
            return List.of();
        }

        return Arrays.stream(output.split("\\R"))
                .filter(message -> !message.isBlank())
                .map(GitCommit::new)
                .toList();
    }

    private static String runGit(final Path repository, final String... arguments) {
        final String[] command = new String[arguments.length + 1];
        command[0] = "git";
        System.arraycopy(arguments, 0, command, 1, arguments.length);
        try (
                final Process process = new ProcessBuilder(command)
                        .directory(repository.toFile())
                        .redirectErrorStream(true)
                        .start()
        ){
            final String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            final int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IllegalArgumentException("Git command failed in %s: %s%n%s"
                        .formatted(repository, String.join(" ", command), output));
            }
            return output;
        } catch (final IOException exception) {
            throw new IllegalStateException("Unable to run git command.", exception);
        } catch (final InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Git command interrupted.", exception);
        }
    }
}
