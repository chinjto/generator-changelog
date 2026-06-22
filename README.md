# generator-changelog

Java changelog generator. The goal is to read a project's Git history and produce a Markdown and/or LaTeX changelog.

The project is an executable jar without Spring. It is driven by Maven through `make`.

## Requirements

- JDK 26 or newer
- Maven
- GNU Make
- Local `toolbox-make` checkout exposed as `~/.make`
- `xmllint`, used by the deployment script to read `pom.xml`

## Configuration

The `.env` file is local and only contains machine-specific paths, for example:

```shell
JAVA_HOME=/home/chinjto/.jdks/openjdk-26.0.1
MVN=/snap/intellij-idea-community/current/plugins/maven/lib/maven3/bin/mvn
DEPLOY_ROOT=/home/chinjto/.generator
```

The project identity stays in `pom.xml`:

- `artifactId` : `generator-changelog`
- `version` : managed by Maven
- `main.class` : jar entry point

## Commands

```shell
make build
make run
make test
make clean
make version VERSION=0.2.0
```

- `make build` generates the executable jar in `target/`
- `make run` runs the generated jar
- `make test` runs Maven tests
- `make clean` removes Maven build outputs
- `make version VERSION=x.y.z` updates the `pom.xml` version through Maven

Generic Git, release, and deploy commands come from `~/.make/git.mk`, provided by the local `toolbox-make` repository.

## Deployment

The `Makefile` configures:

```make
DEPLOY_SCRIPT = ./.scripts/deploy.sh
```

The `deploy` goal defined in `~/.make/git.mk` therefore runs `.scripts/deploy.sh` after the build.

The script copies the generated jar to `DEPLOY_ROOT`, defaulting to `~/.generator`, with a stable name that removes both the application version and the `generator-` prefix:

```text
target/generator-changelog-0.1.0-SNAPSHOT.jar
~/.generator/changelog.jar
```

This keeps the generator command stable across releases.

## Structure

```text
.
├── Makefile
├── pom.xml
├── .scripts/
│   └── deploy.sh
└── src/
    └── main/
        └── java/
            └── fr.chinjto.generator.changelog/
```
