package fr.chinjto.generator.changelog.business.version;

public record ProjectVersion(String version) {
    private static final String SNAPSHOT_SUFFIX = "-SNAPSHOT";

    public String asReleaseVersion() {
        final String releaseVersion = version.endsWith(SNAPSHOT_SUFFIX)
                ? version.substring(0, version.length() - SNAPSHOT_SUFFIX.length())
                : version;

        return "v" + releaseVersion;
    }
}
