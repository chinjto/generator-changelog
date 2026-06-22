package fr.chinjto.generator.changelog.infrastructure.cli;

import fr.chinjto.generator.changelog.business.changelog.ChangelogRequest;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class CommandLineArgumentsParser {
    private static final Set<String> REQUIRED_OPTIONS = Set.of("--repo", "--output");

    public ChangelogRequest parse(final String[] arguments) {
        final Map<String, String> options = parseOptions(arguments);
        REQUIRED_OPTIONS.forEach(option -> require(options, option));
        requireMarkdownOutput(options.get("--output"));

        return new ChangelogRequest(
                Path.of(options.get("--repo")),
                Path.of(options.get("--output"))
        );
    }

    private static Map<String, String> parseOptions(final String[] arguments) {
        if (arguments.length % 2 != 0) {
            throw new IllegalArgumentException("Options must be provided as '--option value' pairs.");
        }

        final Map<String, String> options = new HashMap<>();
        for (int index = 0; index < arguments.length; index += 2) {
            final String option = arguments[index];
            if (!option.startsWith("--")) {
                throw new IllegalArgumentException("Expected option name but got '%s'.".formatted(option));
            }
            options.put(option, arguments[index + 1]);
        }
        return options;
    }

    private static void require(final Map<String, String> options, final String option) {
        if (!options.containsKey(option) || options.get(option).isBlank()) {
            throw new IllegalArgumentException("Missing required option %s.".formatted(option));
        }
    }

    private static void requireMarkdownOutput(final String output) {
        if (!output.endsWith(".md")) {
            throw new IllegalArgumentException("--output must target a .md file.");
        }
    }
}
