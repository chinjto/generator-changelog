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
make test-contract
make test-unit
make clean
make version VERSION=0.2.0
```

- `make build` generates the executable jar in `target/`
- `make run` runs the generated jar
- `make test` runs Maven tests
- `make test-contract` runs CLI contract tests
- `make test-unit` runs unit tests
- `make clean` removes Maven build outputs
- `make version VERSION=x.y.z` updates the `pom.xml` version through Maven

Additional targeted test aliases are available:

- `make test-cli` runs tests tagged `cli`
- `make test-infrastructure` runs tests tagged `infrastructure`
- `make test-tdd` aliases `make test-contract`
- `make test-tu` aliases `make test-unit`

Generic Git, release, and deploy commands come from `~/.make/git.mk`, provided by the local `toolbox-make` repository.

The executable jar accepts the following options:

```shell
java -jar target/generator-changelog-0.1.0-SNAPSHOT.jar \
  --repo /path/to/repository \
  --from v1.0.0 \
  --to v1.1.0 \
  --output /path/to/changelog.md
```

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

## Implementation

The code follows a simple ports-and-adapters split. `business` owns the model, use cases, and ports. `infrastructure` implements those ports with concrete adapters.

`business` must not depend on `infrastructure`; `GeneratorChangelogApplication` is the composition root that wires both sides together.

- business ports: `GitHistoryReader`, `ChangelogRenderer`, `ChangelogWriter`
- infrastructure adapters: CLI parser, Git CLI reader, Markdown renderer, Markdown writer

Tests use JUnit tags to keep feedback loops focused:

- `contract`: executable CLI behavior, driven from TDD scenarios
- `unit`: fast unit tests
- `cli`: command-line behavior
- `infrastructure`: infrastructure adapters

## Structure

```text
.
├── Makefile
├── pom.xml
├── .scripts/
│   └── deploy.sh
└── src/
    ├── main/java/fr.chinjto.generator.changelog/
    │   ├── GeneratorChangelogApplication
    │   ├── business/
    │   │   ├── changelog/
    │   │   ├── git/
    │   │   ├── release/
    │   │   └── usecase/
    │   └── infrastructure/
    │       ├── cli/
    │       ├── git/
    │       └── markdown/
    └── test/java/fr.chinjto.generator.changelog/
        ├── GeneratorChangelogApplicationTest
        └── infrastructure/
            ├── cli/
            └── markdown/
```
